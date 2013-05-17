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

  private String name;
  private ArrayList<MatchExpr> matchExprs;
  private ArrayList<Result> resultExprs;
  private boolean hasVars;

  /**
   * constructeur non utilisé
   *
   * @param e élément de tagname "if"
   * @param syntax
   * @throws Exception
   */
  public GenItem(Element e, Syntax syntax) throws Exception {
    name = e.getAttribute("name");
    hasVars = false;
    matchExprs = new ArrayList<>();
    NodeList nodelist = e.getElementsByTagName("match");
    for (int i = 0; i < nodelist.getLength(); i++) {
      MatchExpr match = new MatchExpr((Element) nodelist.item(i), syntax);
      matchExprs.add(match);
      hasVars = match.getSearches() != null;
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
   * @param n
   * @param limit
   * @param level
   * @param syntax
   * @param en
   * @param vars
   * @param exprNodes
   * @throws Exception 
   */
  public void generate(int n, int limit, int level, Syntax syntax, ExprNode en,
          HashMap<Expression, Expression> vars, ArrayList<ExprNode> exprNodes) 
          throws Exception {
    if (exprNodes.size() < limit) {
      if (!matchExprs.isEmpty() && n < matchExprs.size()) {
        MatchExpr matchExpr = matchExprs.get(n);
        for (int i = 0; i < exprNodes.size(); i++) {
          ExprNode en1 = exprNodes.get(i);
          Expression e = en1.getE();
          HashMap<Expression, Expression> nvars = new HashMap<>();
          nvars.putAll(vars);
          if (matchExpr.checkExpr(en1, nvars, syntax)) {
            ArrayList<int[]> parentList = new ArrayList<>();
            int[] p = Arrays.copyOf(en.getParentList().get(0), en.getParentList().get(0).length);
            p[n] = i;
            parentList.add(p);
            en1 = new ExprNode(e, en.getChildList(), parentList, exprNodes);
            generate(n + 1, limit, level, syntax, en1, nvars, exprNodes);
          }
        }
      }
      else {
        addResults(en, vars, syntax, level, exprNodes);
      }
    }
  }

  /**
   * 
   * @param en
   * @param vars
   * @param syntax
   * @param level
   * @param exprNodes liste d'expressions déjà obtenues
   * @throws Exception 
   */
  private void addResults(ExprNode en, HashMap<Expression, Expression> vars,
          Syntax syntax, int level, ArrayList<ExprNode> exprNodes) throws Exception {
    for (int i = 0; i < resultExprs.size(); i++) {
      Result result = resultExprs.get(i);
      if (result.getLevel() <= level) {
        ExprNode en1 = en.copy();
        result.addExpr(en1, vars, syntax, exprNodes);
      }
    }
  }


  @Override
  public String toString() {
    String ret = "";
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

  public boolean isHasVars() {
    return hasVars;
  }
}
