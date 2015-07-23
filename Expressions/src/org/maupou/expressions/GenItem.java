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
public class GenItem extends Schema {

  private final String name;
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
    setUserObject(name);
    allowsChildren = true;
    SyntaxWrite sw = syntax.getSyntaxWrite();
    this.typesMap = typesMap;
    this.listvars = listvars;
    NodeList nodelist = e.getElementsByTagName("match");
    for (int i = 0; i < nodelist.getLength(); i++) {
      if (e.isEqualNode(nodelist.item(i).getParentNode())) {
        MatchExpr matchExpr = new MatchExpr((Element) nodelist.item(i), 1, listvars, sw);
        matchExprTree(matchExpr, 1, (Element) nodelist.item(i), sw);
        add(matchExpr);
      }
    }
    if (nodelist.getLength() == 0) {
      nodelist = e.getElementsByTagName("result"); // résultats directs
      for (int i = 0; i < nodelist.getLength(); i++) {
        Result result = new Result((Element) nodelist.item(i), sw);
        result.setRgs(new int[0]);
        add(result);
      }
    }
    setParent(null);    
  }
  
  /**
   * 
   * @param matchExpr racine de l'arbre
   * @param depth indice de profondeur
   * @param e élément DOM
   * @throws Exception 
   */
  private void matchExprTree(MatchExpr matchExpr, int depth, Element e, SyntaxWrite sw) 
          throws Exception {
    NodeList nodelist = e.getElementsByTagName("match");
    for (int i = 0; i < nodelist.getLength(); i++) {
      if (e.isEqualNode(nodelist.item(i).getParentNode())) { // niveau immédiatement inférieur
        Element echild = (Element) nodelist.item(i);
        MatchExpr matchChild = new MatchExpr(echild, depth + 1, listvars, sw);
        matchExprTree(matchChild, depth + 1, echild, sw);
        matchExpr.add(matchChild);
      }      
    }
    if(nodelist.getLength() == 0) {
      nodelist = e.getElementsByTagName("result");
      for (int i = 0; i < nodelist.getLength(); i++) {
        Result result = new Result((Element) nodelist.item(i), sw);
        result.setRgs(new int[depth]);
        matchExpr.add(result);
      }
    }
  }


  @Override
  public String toString() {
    return name;
  }

  public HashMap<String, String> getTypesMap() {
    return typesMap;
  }

  public ArrayList<Expression> getListvars() {
    return listvars;
  }

  @Override
  public String log() {
    return name;    
  }

}
