/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Patrice Maupou
 */
public class MatchExpr {

    private String regex;
    private final String node; // à utiliser pour une sous-expression (non utilisé)
    private final String type;
    private Expression schema, change, global;
    private final LinkedHashMap<Expression, Expression> replaceMap;
    private final boolean recursive;
    private final HashMap<String, String> options;  // données par des égalités

    /**
     * Définition des paramètres à partir de l'élément
     *
     * @param match élément de référence
     * @param syntax
     * @param listvars
     * @throws Exception
     */
    public MatchExpr(Element match, Syntax syntax, ArrayList<Expression> listvars) throws Exception {
        options = new HashMap<>();
        node = match.getAttribute("node");
        type = match.getAttribute("name");
        String s = match.getAttribute("global");
        if (!s.isEmpty()) {
            global = new Expression(s, type, null, true, syntax);
        }
        String[] listopts = match.getAttribute("options").split(",");
        for (String option : listopts) {
            if (!option.isEmpty()) {
                String[] pair = option.split("=");
                if (pair.length == 2) {
                    options.put(pair[0], pair[1]);
                }
            }
        }
        recursive = "yes".equals(options.get("recursive"));
        replaceMap = new LinkedHashMap<>();
        Node txtNode = match.getFirstChild();
        /* avant
         int cnt = 0;
         while (txtNode != null) {
         if (txtNode.getNodeType() == Node.CDATA_SECTION_NODE) {
         if (cnt == 0) {
         regex = txtNode.getTextContent();
         schema = new Expression(regex, syntax);
         } else if (cnt == 1) {
         change = new Expression(txtNode.getTextContent(), syntax);
         }
         cnt++;
         }
         txtNode = txtNode.getNextSibling();
         }
         //*/
        //* modif
        Expression key = null;
        int cnt = 0;
        while (txtNode != null) {
            if (txtNode.getNodeType() == Node.CDATA_SECTION_NODE) {
                if (key == null) {
                    key = new Expression(txtNode.getTextContent(), syntax);
                    replaceMap.put(key, null);
                    if (cnt == 0) {
                        regex = txtNode.getTextContent();
                        schema = key;
                    }
                } else {
                    Expression value = new Expression(txtNode.getTextContent(), syntax);
                    key = replaceMap.put(key, value);
                    if (cnt == 1) {
                        change = value;
                    }
                }
                cnt++;
            }
            txtNode = txtNode.getNextSibling();
        }
        //*/
        String types = match.getAttribute("types");
        if (!types.isEmpty()) {
            String[] couples = types.split(",");
            for (String couple : couples) {
                String[] typeOf = couple.split(":");
                Expression e = new Expression(typeOf[0], syntax);
                e.setType(typeOf[1]);
                schema.updateType(e);
            }
        }
    }

    /**
     * vérifie si expr correspond au schema, et si oui, les nouvelles variables
     * de même nom que celles dans svars doivent aussi correspondre
     *
     * @param en l'expr examinée par rapport à schema
     * @param typesMap table de remplacement d'un type par un autre
     * (propvar->prop)
     * @param listvars liste de référence des variables
     * @param vars table des variables déjà connues, ex: A:=(A->B)->A
     * @param syntax
     * @return true si l'expression convient
     * @throws Exception
     */
    public boolean checkExprNode(ExprNode en, HashMap<String, String> typesMap,
            ArrayList<Expression> listvars, HashMap<Expression, Expression> vars, Syntax syntax)
            throws Exception {
        boolean ret = false;
        Expression expr = en.getE().copy();
        if (getSchema() != null) {
            ret = checkExpr(expr, vars, typesMap, listvars, syntax);
        }
        return ret;
    }

    /**
     * match l'expression expr, tient compte des variables de ses pour
     * actualiser ses
     *
     * @param expr l'expression examinée par rapport à schema, ex :
     * ((A->B)->C)->(B->C)
     * @param typesMap table de remplacement d'un type par un autre
     * (propse->prop)
     * @param listvars liste des symboles à remplacer
     * @param vars table des variables déjà connues, ex: {A:=(A->B)->A}
     * @param syntax
     * @return true si l'expression convient
     * @throws Exception
     */
    public boolean checkExpr(Expression expr, HashMap<Expression, Expression> vars,
            HashMap<String, String> typesMap, ArrayList<Expression> listvars, Syntax syntax)
            throws Exception {
        boolean ret = false;
        if (expr != null) { // schema : A->B type: prop
            HashMap<Expression, Expression> svars = new HashMap<>();
            if (change == null) {
                ret = expr.match(getSchema(), typesMap, listvars, svars, syntax.getSubtypes());
            } else if (global != null) {
                Expression e = expr;
                boolean[] modifs = new boolean[1];
                do {
                    modifs[0] = false;
                    e = e.matchsubExpr(schema, change, modifs, typesMap, listvars, svars,
                            syntax.getSubtypes());
                } while (recursive && modifs[0]);
                svars.clear();
                svars.put(global, e);
                ret = true;
            }
            if (vars.isEmpty()) { // ajouter les nouvelles variables
                vars.putAll(svars);
                for (Expression var : listvars) {
                    var.setSymbol(true);
                }
            } else { // ce n'est pas le premier modèle
                HashMap<Expression, Expression> nsvars = new HashMap<>(), nvars = new HashMap<>();
                for (Map.Entry<Expression, Expression> var : vars.entrySet()) {
                    Expression svar = svars.get(var.getKey()); // A->B
                    Expression e = var.getValue(); // (A->B)->C
                    if (ret && svar != null) { // nsvars={A=(A->B)->C, B=B->C} mais pas le C de B:=  
                        ret &= e.match(svar, typesMap, listvars, nsvars, syntax.getSubtypes());
                    }
                }
                //* modif pour matchBoth
                if (ret) {
                    for (Expression e : svars.values()) { // renomme certaines variables
                        extendMap(e, nsvars, listvars);
                    }
                    for (Expression var : vars.keySet()) { // corrige vars avec svars
                        vars.put(var, vars.get(var).replace(nvars));
                    }
                    for (Expression svar : svars.keySet()) {
                        svars.put(svar, svars.get(svar).replace(nsvars));
                    }
                    vars.putAll(svars);
                }
            }
            expr.markUsedVars(listvars);
        } else { // expr est nulle : vérifier si le schéma correspond au type
            Expression e = getSchema().replace(vars);
            ret = type.equals(e.getType());
        }
        return ret;
    }

    /**
     * ajoute à la table vars les variables de e déjà utilisées dans les valeurs
     * de vars
     *
     * @param e expression
     * @param vars table variable=valeur
     * @param listvars liste de référence des variables
     */
    public static void extendMap(Expression e, HashMap<Expression, Expression> vars,
            ArrayList<Expression> listvars) {
        int index = listvars.indexOf(e);
        if (index != -1) {
            Expression evar = listvars.get(index);
            if (!evar.isSymbol() && !vars.containsKey(e)) {
                for (Expression var : listvars) {
                    if (var.isSymbol() && var.getType().equals(e.getType())) {
                        var.setSymbol(false);
                        vars.put(e, var); // changement de variable
                        break;
                    }
                }
            }
        } else if (e.getChildren() != null) {
            for (Expression child : e.getChildren()) {
                extendMap(child, vars, listvars);
            }
        }
    }

    @Override
    public String toString() {
        String ret = "regex = " + regex + "   " + node + type + " " + options;
        return ret;
    }

    public String getRegex() {
        return regex;
    }

    public String getNode() {
        return node;
    }

    public String getType() {
        return type;
    }

    public Expression getSchema() {
        return schema;
    }

    public Expression getChange() {
        return change;
    }

    public Expression getGlobal() {
        return global;
    }

}
