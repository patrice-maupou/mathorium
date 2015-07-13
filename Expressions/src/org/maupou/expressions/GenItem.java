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
  //ex : (propvar->prop)
  private final ArrayList<Expression> listvars; //  liste de référence des variables

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
      /* avant
      MatchExpr matchExpr = new MatchExpr((Element) nodelist.item(i), syntax, listvars);
      //*/
      //* modif
      MatchExpr matchExpr = new MatchExpr((Element) nodelist.item(i), listvars);
      //*/
      matchExprs.add(matchExpr);
    }
    resultExprs = new ArrayList<>();
    nodelist = e.getElementsByTagName("result");
    for (int i = 0; i < nodelist.getLength(); i++) {
      Result result = new Result((Element) nodelist.item(i));
      resultExprs.add(result);
    }
  }

  /**
   * Test de l'exprNode en de rang exprg par matchExpr de rang matchrg.
   * Retourne null si le test est négatif, sinon :
   * remplit la table vars{var->value} marque en comme parent,
   * renvoie en ou la la variable global si elle existe
   * 
   * @param level niveau à comparer à celui de genitem
   * @param matchrg rang de matchExpr
   * @param exprg rang de l'exprNode à tester
   * @param syntax
   * @param en patron pour la liste parents-enfants
   * @param vars table de variables des modèles
   * @param exprNodes
   * @return l'ExprNode transformée
   * @throws java.lang.Exception
   */
  public ExprNode genapply(int level, int matchrg, int exprg, Syntax syntax, ExprNode en,
          HashMap<Expression, Expression> vars, ArrayList<ExprNode> exprNodes)
          throws Exception {
    ExprNode ret = null;
    if (matchrg < matchExprs.size()) {
      MatchExpr matchExpr = matchExprs.get(matchrg);
      Expression e = exprNodes.get(exprg).getE().copy();
      if (matchExpr.checkExpr(e, vars, typesMap, listvars, syntax)) { // e convient
        ArrayList<int[]> parentList = new ArrayList<>();
        int[] p = new int[]{exprg};
        if (!en.getParentList().isEmpty()) { // le node de rang exprg est dont parent potentiel
          p = Arrays.copyOf(en.getParentList().get(0), en.getParentList().get(0).length);
          p[matchrg] = exprg;
        }
        parentList.add(p);
        if (matchExpr.getGlobal() != null) {
          Expression g = vars.get(matchExpr.getGlobal()); // la variable t par exemple
          e = (g == null) ? e : g;
        }
        ret = new ExprNode(e, en.getChildList(), parentList);
      }
    }
    return ret;
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
   * @return le nombre de résultats ajoutés
   * @throws Exception
   */
  public int addResults(ExprNode en, HashMap<Expression, Expression> vars, Syntax syntax,
          int level, ArrayList<ExprNode> exprNodes, ArrayList<ExprNode> exprDiscards)
          throws Exception {
    int ret = 0;
    if (resultExprs.isEmpty()) {
      if (exprNodes.indexOf(en) == -1) {
        en.setRange(exprNodes.size());
        exprNodes.add(en);
        ret = 1;
      }
    }
    for (Result result : resultExprs) {
      if (result.getLevel() <= level) {
        ExprNode en1 = en.copy();
        en1 = result.addExpr(en1, vars, typesMap, listvars, syntax, exprNodes, exprDiscards);
        if (en1 != null) {
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

  public HashMap<String, String> getTypesMap() {
    return typesMap;
  }

  public ArrayList<Expression> getListvars() {
    return listvars;
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

}
