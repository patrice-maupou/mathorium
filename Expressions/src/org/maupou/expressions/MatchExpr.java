/* 
 * Copyright (C) 2015 Patrice.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */
public class MatchExpr extends Schema {

  private final boolean bidir;
  private GenItem genItemParent;

  public MatchExpr(Element match, int depth, ArrayList<Expression> listvars, SyntaxWrite sw)
          throws Exception {
    allowsChildren = true;
    rgs = new int[depth];
    HashMap<String, String> options = new HashMap<>();
    NodeList patterns = match.getElementsByTagName("pattern");
    if (patterns.getLength() == 0) {
      setPattern(match);
    } else {
      setPattern((Element) patterns.item(0));
    }
    NodeList nodelist = match.getElementsByTagName("match");
    for (int i = 0; i < nodelist.getLength(); i++) {
      if (match.isEqualNode(nodelist.item(i).getParentNode())) { // niveau immédiatement inférieur
        Element echild = (Element) nodelist.item(i);
        MatchExpr matchChild = new MatchExpr(echild, depth + 1, listvars, sw);
        add(matchChild);
      }
    }
    nodelist = match.getElementsByTagName("result");
    for (int i = 0; i < nodelist.getLength(); i++) {
      if (match.isEqualNode(nodelist.item(i).getParentNode())) { // niveau immédiatement inférieur
        Result result = new Result((Element) nodelist.item(i), depth, sw);
        result.varMap = varMap;
        add(result);
      }
    }
    setUserObject("modèle : " + sw.toString(getPattern()));
    varsInExpression(getPattern(), getVars(), listvars);
    String[] listopts = match.getAttribute("options").split(",");
    for (String option : listopts) {
      if (!option.isEmpty()) {
        String[] pair = option.split("=");
        if (pair.length == 2) {
          options.put(pair[0], pair[1]);
        }
      }
    }
    bidir = "yes".equals(options.get("bidirectional"));
  }

  /**
   * match l'expression expr en tenant compte de la table vars des variables déjà attribuées). 1. si
   * global = null, match direct de expr contre pattern. 2. sinon, transformation de l'expression
   * par la table varMap, résultat dans la variable global
   *
   * @param expr l'expression examinée par rapport à pattern, ex : ((A->B)->C)->(B->C)
   * @param typesMap table de remplacement d'un type par un autre (propse->prop)
   * @param listvars liste des symboles à remplacer
   * @return true si l'expression convient
   */
  public boolean checkExpr(Expression expr, HashMap<String, String> typesMap, 
          ArrayList<Expression> listvars) {
    boolean ret;
    if (expr != null) { // pattern : A->B type: prop
      HashMap<Expression, Expression> svars = new HashMap<>(), evars = new HashMap<>();
      if (bidir) {
        ret = genItemParent.matchBoth(expr, getPattern(), evars, svars);
      } else {
        ret = genItemParent.match(expr, getPattern(), svars);
      }
      if (varMap.isEmpty()) { // ajouter les nouvelles variables à la table vars
        varMap.putAll(svars);
        listvars.stream().forEach((var) -> {var.setSymbol(true);});
      } else { // ce n'est pas le premier modèle
        HashMap<Expression, Expression> nsvars = new HashMap<>(), nvars = new HashMap<>();
        for (Map.Entry<Expression, Expression> var : varMap.entrySet()) {
          Expression svar = svars.get(var.getKey()); // A->B
          Expression e = var.getValue(); // (A->B)->C
          if (ret && svar != null) { // nsvars={A=(A->B)->C, B=B->C} mais pas le C de B:= 
            ret &= genItemParent.match(e, svar, nsvars);
          }
        }
        if (ret) {
          // renomme certaines variables
          svars.values().stream().forEach((e) -> {
            extendMap(e, nsvars, listvars);
          });
          // corrige vars avec svars
          varMap.keySet().stream().forEach((var) -> {
            varMap.put(var, varMap.get(var).replace(nvars));
          });
          svars.keySet().stream().forEach((svar) -> {
            svars.put(svar, svars.get(svar).replace(nsvars));
          });
          varMap.putAll(svars);
        }
      }
      genItemParent.markUsedVars(expr);
    } else { // expr est nulle : vérifier si le schéma correspond au type
      Expression e = getPattern().replace(varMap);
      ret = getPattern().getType().equals(e.getType());
    }
    return ret;
  }

  /**
   * ajoute à la liste vars les variables de listvars qui composent l'expression e
   *
   * @param e
   * @param vars
   * @param listvars
   */
  public static void varsInExpression(Expression e, ArrayList<Expression> vars,
          ArrayList<Expression> listvars) {
    if (listvars.indexOf(e) == -1) {
      if (e.getChildren() != null) {
        e.getChildren().stream().forEach((child) -> {
          varsInExpression(child, vars, listvars);
        });
      }
    } else {
      vars.add(e);
    }
  }

  /**
   * ajoute à la table vars les variables de e déjà utilisées dans les valeurs de vars
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
      if (!evar.isSymbol() && !vars.containsKey(e)) { // evar est une variable utilisée 
        for (Expression var : listvars) {
          if (var.isSymbol() && var.getType().equals(e.getType())) {
            var.setSymbol(false);
            vars.put(e, var); // changement de variable
            break;
          }
        }
      }
    } else if (e.getChildren() != null) {
      e.getChildren().stream().forEach((child) -> {
        extendMap(child, vars, listvars);
      });
    }
  }

  public GenItem getGenItemParent() {
    return genItemParent;
  }

  @Override
  void setGenItemParent(GenItem genItem) {
    genItemParent = genItem;
  } 
  

  @Override
  public String toString() {
    String ret = "match : " + getPattern().toString() + "\nvars : " + getVars() + "\trgs : ";
    for (int i = 0; i < getRgs().length; i++) {
      ret += (i == 0) ? "" : ",";
      ret += getRgs()[i];
    }
    return ret;
  }

}
