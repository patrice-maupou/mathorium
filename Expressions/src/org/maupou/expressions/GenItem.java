/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */
public class GenItem {

    private final String name;
    private final ArrayList<MatchExpr> matchExprs;
    private final ArrayList<Result> resultExprs;
    private final HashMap<String, String> typesMap; //table de remplacement d'un type par un autre 
    //(propvar->prop)
    private final ArrayList<Expression> listvars; //  liste de référence des variables
    private boolean local;

    public HashMap<String, String> getFreevars() {
        return typesMap;
    }

    public ArrayList<Expression> getListvars() {
        return listvars;
    }

    /**
     * constructeur
     *
     * @param e élément de tagname "genrule" ou "discard"
     * @param syntax
     * @param typesMap
     * @param listvars
     * @throws Exception
     */
    public GenItem(Element e, Syntax syntax, HashMap<String, String> typesMap,
            ArrayList<Expression> listvars) throws Exception {
        name = e.getAttribute("name");
        this.typesMap = typesMap;
        this.listvars = listvars;
        matchExprs = new ArrayList<>();
        NodeList nodelist = e.getElementsByTagName("match");
        for (int i = 0; i < nodelist.getLength(); i++) {
            MatchExpr matchExpr = new MatchExpr((Element) nodelist.item(i), syntax, listvars);
            matchExprs.add(matchExpr);
        }
        resultExprs = new ArrayList<>();
        nodelist = e.getElementsByTagName("result");
        for (int i = 0; i < nodelist.getLength(); i++) {
            Result result = new Result((Element) nodelist.item(i), syntax);
            resultExprs.add(result);
        }
    }

    /**
     *
     * @param level niveau à comparer à celui de genitem
     * @param matchrg rang de matchExpr
     * @param exprg rang de l'exprNode
     * @param syntax
     * @param en patron pour la liste parents-enfants
     * @param vars table de variables des modèles
     * @param exprNodes
     * @return
     * @throws java.lang.Exception
     */
    public ExprNode genapply(int level, int matchrg, int exprg, Syntax syntax, ExprNode en,
            HashMap<Expression, Expression> vars, ArrayList<ExprNode> exprNodes)
            throws Exception {
        ExprNode ret = null;
        if (!matchExprs.isEmpty() && matchrg < matchExprs.size()) {
            MatchExpr matchExpr = matchExprs.get(matchrg);
            Expression e = exprNodes.get(exprg).getE().copy();
            if (matchExpr.getGlobal() != null) {
                Expression g = vars.get(matchExpr.getGlobal());
                e = (g == null) ? e : g;
            }
            if (matchExpr.checkExpr(e, vars, typesMap, listvars, syntax)) {
                ArrayList<int[]> parentList = new ArrayList<>();
                int[] p = Arrays.copyOf(en.getParentList().get(0),
                        en.getParentList().get(0).length);
                p[matchrg] = exprg;
                parentList.add(p);
                ret = new ExprNode(e, en.getChildList(), parentList);
            }
        }
        return ret;
    }

    /**
     * génère un hypercube d'expressions de côté borné par limit
     *
     * @param n niveau de profondeur des MatchExprs
     * @param limit
     * @param level niveau de l'item
     * @param syntax
     * @param en
     * @param vars table des variables : variable -> value
     * @param exprNodes liste courante
     * @param exprDiscards
     * @return
     * @throws Exception
     */
    public int[] generate(int n, int limit, int level, Syntax syntax, ExprNode en,
            HashMap<Expression, Expression> vars, ArrayList<ExprNode> exprNodes,
            ArrayList<ExprNode> exprDiscards)
            throws Exception {
        int[] bounds = new int[matchExprs.size() - n + 1];
        bounds[0] = limit;
        if (exprNodes.size() < limit) {
            if (!matchExprs.isEmpty() && n < matchExprs.size()) {
                MatchExpr matchExpr = matchExprs.get(n);
                for (int i = 0; i < exprNodes.size(); i++) {
                    ExprNode en1 = exprNodes.get(i);
                    Expression e = en1.getE().copy();
                    HashMap<Expression, Expression> nvars = new HashMap<>();
                    nvars.putAll(vars);
                    if (matchExpr.getGlobal() != null) {
                        Expression g = vars.get(matchExpr.getGlobal());
                        e = (g == null) ? e : g;
                    }
                    if (matchExpr.checkExpr(e, nvars, typesMap, listvars, syntax)) {
                        ArrayList<int[]> parentList = new ArrayList<>();
                        int[] p = Arrays.copyOf(en.getParentList().get(0),
                                en.getParentList().get(0).length);
                        p[n] = i;
                        parentList.add(p);
                        en1 = new ExprNode(e, en.getChildList(), parentList);
                        int[] sb = generate(n + 1, limit, level, syntax, en1, nvars,
                                exprNodes, exprDiscards);
                        System.arraycopy(sb, 0, bounds, 1, matchExprs.size() - n);
                        bounds[0] = (i < bounds[0]) ? i : bounds[0];
                    }
                }
            } else {
                addResults(en, vars, syntax, level, exprNodes, exprDiscards);
            }
        }
        return bounds;
    }

    /**
     * ajoute les nouveaux résultats à la liste exprNodes
     *
     * @param en
     * @param vars
     * @param syntax
     * @param level
     * @param exprNodes liste d'expressions déjà obtenues
     * @param exprDiscards
     * @return 
     * @throws Exception
     */
    public int addResults(ExprNode en, HashMap<Expression, Expression> vars, Syntax syntax, 
            int level, ArrayList<ExprNode> exprNodes, ArrayList<ExprNode> exprDiscards)
            throws Exception {
        int ret = 0;
        for (Result result : resultExprs) {
            if (result.getLevel() <= level) {
                ExprNode en1 = en.copy();
                en1 = result.addExpr(en1, vars, typesMap, listvars, syntax, exprNodes, exprDiscards);
                if(en1 != null) {
                    ret++;
                }
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        String ret = "";
        for (MatchExpr matchExpr : matchExprs) {
            String cond = matchExpr.toString();
            ret += "   " + cond;
        }
        ret += "  ->  ";
        for (Result result : resultExprs) {
            ret += "  " + result;
        }
        return ret;
    }

    public String getName() {
        return name;
    }

    public ArrayList<MatchExpr> getMatchExprs() {
        return matchExprs;
    }

    public ArrayList<Result> getResultExprs() {
        return resultExprs;
    }

    /**
     * @return the local
     */
    public boolean isLocal() {
        return local;
    }
}
