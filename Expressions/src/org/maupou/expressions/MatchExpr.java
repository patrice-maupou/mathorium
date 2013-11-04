/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    private Expression schema, check;
    private boolean noExpr;
    private final HashMap<String, String> conds;  // donnée par une égalité (semble non utilisé)

    /**
     * Définition des paramètres à partir de l'élément
     *
     * @param match élément de référence
     * @param syntax
     * @throws Exception
     */
    public MatchExpr(Element match, Syntax syntax) throws Exception {
        conds = new HashMap<>();
        node = match.getAttribute("node");
        type = match.getAttribute("name");
        Node txtNode = match.getFirstChild();
        if (txtNode != null) {
            regex = txtNode.getTextContent();
            schema = new Expression(regex, syntax);
            txtNode = txtNode.getNextSibling();
            if(txtNode != null) {
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
     * @param map la table ordonnée des variables
     * @param freevars
     * @param listvars
     * @param vars table des variables déjà connues, ex: A:=(A->B)->A
     * @param syntax
     * @return true si l'expression convient
     * @throws Exception
     */
    public boolean checkExprNode(ExprNode en, TreeMap<String, String> map, 
            HashMap<String,String> freevars, ArrayList<Expression> listvars, 
            HashMap<Expression, Expression> vars, Syntax syntax)
            throws Exception {
        boolean ret;
        Expression expr = en.getE().copy();
        if (getSchema() == null) {
            ret = checkConditions(en, syntax);
        } else {
            ret = checkExpr(expr, map, freevars, listvars, vars, syntax);
        }
        return ret;
    }

    /**
     *
     * @param expr l'expression examinée par rapport à schema
     * @param map la table ordonnée des variables
     * @param freevars
     * @param listvars
     * @param vars table des variables déjà connues, ex: A:=(A->B)->A
     * @param syntax
     * @return true si l'expression convient
     * @throws Exception
     */
    public boolean checkExpr(Expression expr, TreeMap<String, String> map, HashMap<String,String> 
            freevars, ArrayList<Expression> listvars, HashMap<Expression, Expression> vars, 
            Syntax syntax) throws Exception {
        boolean ret;
        if (expr != null) { // ex : A->B type: prop
            HashMap<Expression, Expression> nvars = new HashMap<>();
            ret = expr.match(getSchema(), freevars, listvars, nvars, syntax.getSubtypes());
            if (vars.isEmpty()) { // ajouter les nouvelles variables
                vars.putAll(nvars);
            } else { // actualiser nvars  A:=(A->B)->C  B:=B->C par rapport à vars
                HashMap<Expression, Expression> aux = new HashMap<>();
                for (Map.Entry<Expression, Expression> entry : vars.entrySet()) {
                    Expression var = nvars.get(entry.getKey()); // A
                    Expression e = entry.getValue(); // (A->B)->C
                    if (ret && var != null) { // (A->B)->C  entry.getValue():=A->(B->A)
                        ret &= e.match(var, freevars, listvars, aux, syntax.getSubtypes());
                    }
                }
                if (ret) {
                    for (Map.Entry<Expression, Expression> entry : nvars.entrySet()) {
                        vars.put(entry.getKey(), entry.getValue().replace(aux)); // actualisation
                    }
                }
            }
        }
        else { // vérifier si le schéma coorespond au type
            Expression e = getSchema().replace(vars);
            ret = type.equals(e.getType());
        }
        return ret;
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

    public boolean isNoExpr() {
        return noExpr;
    }
    public void setNoExpr(boolean noExpr) {
        this.noExpr = noExpr;
    }
}
