/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Patrice Maupou
 */
public class MatchExpr {

    private String regex;
    private String node; // à utiliser pour une sous-expression
    private String type;
    private Expression schema;
    private HashMap<String, String> conds;  // donnée par une égalité (semble non utilisé)

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
        } else {
            String[] listconds = match.getAttribute("conds").split(",");
            for (int i = 0; i < listconds.length; i++) {
                if (!listconds[i].isEmpty()) {
                    String[] cond = listconds[i].split("=");
                    String key = cond[0];
                    String value = (cond.length == 2) ? cond[1] : null;
                    conds.put(key, value);
                }
            }
        }
        String types = match.getAttribute("types");
        if (!types.isEmpty()) {
            String[] couples = types.split(",");
            for (int i = 0; i < couples.length; i++) {
                String[] typeOf = couples[i].split(":");
                Expression e = new Expression(typeOf[0], syntax);
                e.setType(typeOf[1]);
                schema.updateType(e);
            }
        }
    }

    /**
     * liste des expressions de la liste exprs qui conviennent
     *
     * @param vars
     * @param syntax
     * @param map
     * @param exprNodes
     * @return
     * @throws Exception
     */
    public ArrayList<String> searchExprs(HashMap<Expression, Expression> vars,
            Syntax syntax, TreeMap<String, String> map,
            ArrayList<ExprNode> exprNodes) throws Exception {
        ArrayList<String> nexprs = new ArrayList<>();
        HashMap<Expression, Expression> nvars = new HashMap<>();
        for (ExprNode en : exprNodes) {
            nvars.clear();
            for (Map.Entry<Expression, Expression> entry : vars.entrySet()) {
                nvars.put(entry.getKey().copy(), entry.getValue().copy());
            }
            if (checkExpr(en, map, nvars, syntax)) {
                nexprs.add(en.getE().toString(syntax.getSyntaxWrite()));
            }
        }
        vars.clear();
        return nexprs;
    }

    /**
     * vérifie si expr correspond au schema, et si oui, les nouvelles variables
     * de même nom que celles dans vars doivent aussi correspondre
     *
     * @param en l'expr examinée par rapport à schema
     * @param map la table ordonnée des variables
     * @param vars table des variables déjà connues, ex: A:=(A->B)->A
     * @param syntax
     * @return true si l'expression convient
     * @throws Exception
     */
    public boolean checkExpr(ExprNode en, TreeMap<String, String> map,
            HashMap<Expression, Expression> vars, Syntax syntax)
            throws Exception {
        boolean ret = false;
        Expression expr = en.getE().copy();
        if (getSchema() == null) {
            ret = checkConditions(en, syntax);
        } else if (expr != null) { // ex : A->B type: prop
            HashMap<Expression, Expression> nvars = new HashMap<>();
            ret = expr.match(getSchema(), map, nvars, syntax.getSubtypes());
            if (vars.isEmpty()) { // ajouter les nouvelles variables
                vars.putAll(nvars);
            } else { // actualiser nvars  A:=(A->B)->C  B:=B->C par rapport à vars
                HashMap<Expression, Expression> aux = new HashMap<>();
                for (Map.Entry<Expression, Expression> entry : vars.entrySet()) {
                    Expression var = nvars.get(entry.getKey()); // A
                    Expression e = entry.getValue(); // (A->B)->C
                    if (ret && var != null) { // (A->B)->C  entry.getValue():=A->(B->A)
                        ret &= e.match(var, map, aux, syntax.getSubtypes());
                    }
                }
                if (ret) {
                    for (Map.Entry<Expression, Expression> entry : nvars.entrySet()) {
                        vars.put(entry.getKey(), entry.getValue().replace(aux)); // actualisation
                    }
                }
            }
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
}
