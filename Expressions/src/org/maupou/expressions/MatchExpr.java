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
                    String value = (cond.length == 2) ? cond[1] : null;
                    conds.put(key, value);
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
     * de même nom que celles dans vars doivent aussi correspondre
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
     * match l'expression expr, tient compte des variables de vars pour
     * actualiser vars
     *
     * @param expr l'expression examinée par rapport à schema, ex :
     * ((A->B)->C)->(B->C)
     * @param freevars table de remplacement d'un type par un autre
     * (propvar->prop)
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
            HashMap<Expression, Expression> nvars = new HashMap<>();
            ret = expr.match(getSchema(), freevars, listvars, nvars, syntax.getSubtypes());
            if (vars.isEmpty()) { // ajouter les nouvelles variables
                vars.putAll(nvars);
                for (Expression var : listvars) {
                    var.setSymbol(true);
                }
            } else { // actualiser nvars = {A:=A->B  B:=(B->C)->(A->C)}
                //  par rapport à vars = {A:=((A->B)->C}->(B->C)
                HashMap<Expression, Expression> aux = new HashMap<>();
                for (Map.Entry<Expression, Expression> entry : vars.entrySet()) {
                    Expression var = nvars.get(entry.getKey()); // A->B
                    Expression e = entry.getValue(); // (A->B)->C
                    if (ret && var != null) { // aux={A=(A->B)->C, B=B->C} mais pas le C de B:=
                        ret &= e.match(var, freevars, listvars, aux, syntax.getSubtypes());
                    }                    
                }
                if (ret) { // actualisation de vars par nvars et aux (étendre aux)
                    for (Expression e : nvars.values()) {
                        extendMap(e, aux, listvars);
                    }
                    for (Map.Entry<Expression, Expression> entry : nvars.entrySet()) {
                        vars.put(entry.getKey(), entry.getValue().replace(aux)); // actualisation
                    }
                }
            }
            expr.markUsedVars(listvars);
        } else { // vérifier si le schéma correspond au type
            Expression e = getSchema().replace(vars);
            ret = type.equals(e.getType());
        }
        return ret;
    }
    
    /**
     * ajoute à la table aux les variables de e déjà utilisées dans les valeurs de aux
     * @param e
     * @param aux
     * @param listvars 
     */
    public static void extendMap(Expression e, 
            HashMap<Expression, Expression> aux, ArrayList<Expression> listvars) {
        int index = listvars.indexOf(e);
        if(index != -1) {
            Expression evar = listvars.get(index);
            if(!evar.isSymbol() && !aux.containsKey(e)) { //déjà utilisée, à remplacer
                for (int i = 0; i < listvars.size(); i++) {
                    Expression var = listvars.get(i);
                    if(var.isSymbol() && var.getType().equals(e.getType())) {
                        var.setSymbol(false);
                        aux.put(e, var); 
                        break;
                    }
                    
                }
            }
        } else if(e.getChildren() != null) {
            for (Expression child : e.getChildren()) {
                extendMap(child, aux, listvars);
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
            String value = entry.getValue();
            try {
                int max = Integer.parseInt(value);
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
