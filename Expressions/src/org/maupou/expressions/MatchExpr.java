/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    private Expression schema, check;
    private final HashMap<String, String> conds;  // donnée par une égalité (semble non utilisé)

    /**
     * Définition des paramètres à partir de l'élément
     *
     * @param match élément de référence
     * @param syntax
     * @param listvars
     * @throws Exception
     */
    public MatchExpr(Element match, Syntax syntax, ArrayList<Expression> listvars) throws Exception {
        conds = new HashMap<>();
        node = match.getAttribute("node");
        type = match.getAttribute("name");
        Node txtNode = match.getFirstChild();
        if (txtNode != null) {
            regex = txtNode.getTextContent();
            schema = new Expression(regex, syntax);
            //schema.setType(type);
            txtNode = txtNode.getNextSibling();
            if (txtNode != null) {
                check = new Expression(txtNode.getTextContent(), syntax);
            }
        } else {
            String[] listconds = match.getAttribute("conds").split(",");
            for (String listcond : listconds) {
                if (!listcond.isEmpty()) {
                    String[] cond = listcond.split("=");
                    String key = cond[0];
                    String eue = (cond.length == 2) ? cond[1] : null;
                    conds.put(key, eue);
                }
            }
        }
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
 de même nom que celles dans svars doivent aussi correspondre
     *
     * @param en l'expr examinée par rapport à schema
     * @param freevars
     * @param listvars
     * @param vars table des variables déjà connues, ex: A:=(A->B)->A
     * @param syntax
     * @return true si l'expression convient
     * @throws Exception
     */
    public boolean checkExprNode(ExprNode en, HashMap<String, String> freevars, ArrayList<Expression> listvars,
            HashMap<Expression, Expression> vars, Syntax syntax)
            throws Exception {
        boolean ret;
        Expression expr = en.getE().copy();
        if (getSchema() == null) {
            ret = checkConditions(en, syntax);
        } else {
            ret = checkExpr(expr, vars, freevars, listvars, syntax);
        }
        return ret;
    }

    /**
     * match l'expression expr, tient compte des variables de ses pour
 actualiser ses
     *
     * @param expr l'expression examinée par rapport à schema, ex : ((A->B)->C)->(B->C)
     * @param freevars table de remplacement d'un type par un autre (propse->prop)
     * @param listvars liste des symboles à remplacer
     * @param vars table des variables déjà connues, ex: {A:=(A->B)->A}
     * @param syntax
     * @return true si l'expression convient
     * @throws Exception
     */
    public boolean checkExpr(Expression expr, HashMap<Expression, Expression> vars,
            HashMap<String, String> freevars, ArrayList<Expression> listvars, Syntax syntax)
            throws Exception {
        boolean ret;
        if (expr != null) { // schema : A->B type: prop
            HashMap<Expression, Expression> svars = new HashMap<>();
            ret = expr.match(getSchema(), freevars, listvars, svars, syntax.getSubtypes());
            if (vars.isEmpty()) { // ajouter les nouvelles variables
                vars.putAll(svars);
                for (Expression var : listvars) {
                    var.setSymbol(true);
                }
            } else { HashMap<Expression, Expression> nsvars = new HashMap<>(), nvars = new HashMap<>();
                for (Map.Entry<Expression, Expression> var : vars.entrySet()) {
                    Expression svar = svars.get(var.getKey()); // A->B
                    Expression e = var.getValue(); // (A->B)->C
                    if (ret && svar != null) { // nsvars={A=(A->B)->C, B=B->C} mais pas le C de B:=  
                        ret &= e.match(svar, freevars, listvars, nsvars, syntax.getSubtypes());
                        /*
                        ret &= e.matchBoth(svar, freevars, listvars, nvars, nsvars, syntax.getSubtypes());
                        //*/
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
                //*/
                /* pour match
                if (ret) { // actualisation de vars par svars et nsvars (étendre nsvars)
                    for (Expression e : svars.values()) { // renomme certaines variables
                        extendMap(e, nsvars, listvars);
                    }
                    for (Map.Entry<Expression, Expression> svar : svars.entrySet()) {
                        vars.put(svar.getKey(), svar.getValue().replace(nsvars)); // actualisation
                    }
                }
                //*/
            }
            expr.markUsedVars(listvars);
        } else { // vérifier si le schéma correspond au type
            Expression e = getSchema().replace(vars);
            ret = type.equals(e.getType());
        }
        return ret;
    }
    
    /**
     * ajoute à la table vars les variables de e déjà utilisées dans les valeurs de vars
     * @param e expression
     * @param vars table variable=valeur
     * @param listvars liste de référence des variables
     */
    public static void extendMap(Expression e, 
            HashMap<Expression, Expression> vars, ArrayList<Expression> listvars) {
        int index = listvars.indexOf(e);
        if(index != -1) {
            Expression evar = listvars.get(index);
            if(!evar.isSymbol() && !vars.containsKey(e)) { //déjà utilisée, à remplacer
                for (int i = 0; i < listvars.size(); i++) {
                    Expression var = listvars.get(i);
                    if(var.isSymbol() && var.getType().equals(e.getType())) {
                        var.setSymbol(false);
                        vars.put(e, var); // changement de variable
                        break;
                    }
                    
                }
            }
        } else if(e.getChildren() != null) {
            for (Expression child : e.getChildren()) {
                extendMap(child, vars, listvars);
            }
        }
    }

    /**
     * conditions sur la taille des parents, des enfants et profondeur
     *
     * @param en ExprNode à tester
     * @param syntax
     * @return true si les conditions sont satisfaites
     */
    public boolean checkConditions(ExprNode en, Syntax syntax) {
        boolean ret = false;
        for (Map.Entry<String, String> entry : conds.entrySet()) {
            String key = entry.getKey();
            String eue = entry.getValue();
            try {
                int max = Integer.parseInt(eue);
                switch (key) {
                    case "parents":
                        ret |= en.getParentList().size() <= max;
                        break;
                    case "childs":
                        ret |= en.getChildList().size() <= max;
                        break;
                    case "depth":
                        ret |= en.getE().depth() > max;
                        break;
                }
            } catch (NumberFormatException numberFormatException) {
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        String ret = "regex = " + regex + "   " + node + type + " " + conds;
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

    /**
     * @return the check
     */
    public Expression getCheck() {
        return check;
    }

}
