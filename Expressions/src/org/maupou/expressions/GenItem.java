/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */
public class GenItem {

    private String name;
    private TreeMap<String, String> map;
    private ArrayList<MatchExpr> matchExprs;
    private ArrayList<Result> resultExprs;
    private HashMap<String,String> freevars;
    private ArrayList<Expression> listvars;
    private HashMap<String, String> vars;
    private boolean local;
    private Scope scope;

    /**
     * @return the freevars
     */
    public HashMap<String,String> getFreevars() {
        return freevars;
    }

    /**
     * @return the listvars
     */
    public ArrayList<Expression> getListvars() {
        return listvars;
    }

    
    

    public enum Scope {

        root, all, left, right
    };

    /**
     * constructeur
     *
     * @param e élément de tagname "genrule" ou "discard"
     * @param syntax
     * @param map
     * @param freevars
     * @param listvars
     * @throws Exception
     */
    public GenItem(Element e, Syntax syntax, TreeMap<String, String> map, HashMap<String,String> freevars, 
            ArrayList<Expression> listvars) 
            throws Exception {
        name = e.getAttribute("name");
        scope = Scope.root;
        if (e.hasAttribute("scope")) {
            switch (e.getAttribute("scope")) {
                case "root":
                    scope = Scope.root;
                    break;
                case "all":
                    scope = Scope.all;
                    break;
                case "left":
                    scope = Scope.left;
                    break;
                case "right":
                    scope = Scope.right;
                    break;
                default:
                    throw new AssertionError();
            }
        }
        this.map = map;
        this.freevars = freevars;
        this.listvars = listvars;
        matchExprs = new ArrayList<>();
        NodeList nodelist = e.getElementsByTagName("match");
        vars = new HashMap<>();
        for (int i = 0; i < nodelist.getLength(); i++) {
            MatchExpr matchExpr = new MatchExpr((Element) nodelist.item(i), syntax);
            matchExprs.add(matchExpr);
            matchExpr.getSchema().addVars(vars, map);
        }
        resultExprs = new ArrayList<>();
        nodelist = e.getElementsByTagName("result");
        for (int i = 0; i < nodelist.getLength(); i++) {
            Result result = new Result((Element) nodelist.item(i), syntax);
            resultExprs.add(result);
        }
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
    public int[] generate(int n, int limit, int level, Syntax syntax, ExprNode en, HashMap<Expression, 
            Expression> vars, ArrayList<ExprNode> exprNodes, ArrayList<ExprNode> exprDiscards)
            throws Exception {
        int[] bounds = new int[matchExprs.size() - n + 1];
        bounds[0] = limit;
        if (exprNodes.size() < limit) {
            if (!matchExprs.isEmpty() && n < matchExprs.size()) {
                MatchExpr matchExpr = matchExprs.get(n);
                for (int i = 0; i < exprNodes.size(); i++) {
                    ExprNode en1 = exprNodes.get(i);
                    Expression e = en1.getE();
                    HashMap<Expression, Expression> nvars = new HashMap<>();
                    nvars.putAll(vars);
                    if (matchExpr.checkExprNode(en1, map, freevars, listvars, nvars, syntax)) {
                        ArrayList<int[]> parentList = new ArrayList<>();
                        int[] p = Arrays.copyOf(en.getParentList().get(0), en.getParentList().get(0).length);
                        p[n] = i;
                        parentList.add(p);
                        en1 = new ExprNode(e, en.getChildList(), parentList);
                        int[] sb = generate(n + 1, limit, level, syntax, en1, nvars, exprNodes, exprDiscards);
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
     * @throws Exception
     */
    private void addResults(ExprNode en, HashMap<Expression, Expression> vars, Syntax syntax, int level, 
            ArrayList<ExprNode> exprNodes, ArrayList<ExprNode> exprDiscards) 
            throws Exception {
        for (int i = 0; i < resultExprs.size(); i++) {
            Result result = resultExprs.get(i);
            if (result.getLevel() <= level) {
                ExprNode en1 = en.copy();
                result.addExpr(en1, vars, map, freevars, listvars, syntax, exprNodes, exprDiscards);
            }
        }
    }

    @Override
    public String toString() {
        String ret = map.toString() + "\n";
        for (int i = 0; i < matchExprs.size(); i++) {
            String cond = matchExprs.get(i).getRegex();
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
     * @return the map
     */
    public TreeMap<String, String> getMap() {
        return map;
    }

    /**
     * @return the vars
     */
    public HashMap<String, String> getVars() {
        return vars;
    }
    
    public Scope getScope() {
        return scope;
    }

    /**
     * @return the local
     */
    public boolean isLocal() {
        return local;
    }
}
