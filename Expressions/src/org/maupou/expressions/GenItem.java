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
import java.util.Set;
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
  private final HashMap<String, Set<String>> subtypes;
  private final ArrayList<Expression> listvars; //  liste de référence des variables
  
/**
 * constructeur minimal pour tester les méthodes matchxx
 * @param typesMap
 * @param listvars
 * @param subtypes 
 */
  public GenItem(HashMap<String, String> typesMap,
          ArrayList<Expression> listvars, HashMap<String, Set<String>> subtypes) {
    this.name = "test";
    this.typesMap = typesMap;
    this.subtypes = subtypes;
    this.listvars = listvars;
  }

  /**
   * constructeur
   *
   * @param e élément de tagname "genrule" ou "discard"
   * @param syntax
   * @param typesMap
   * @param listvars
   * @param subtypes
   * @throws Exception
   */
  public GenItem(Element e, Syntax syntax, HashMap<String, String> typesMap,
          ArrayList<Expression> listvars, HashMap<String, Set<String>> subtypes) throws Exception {
    name = e.getAttribute("name");
    this.subtypes = subtypes;
    setUserObject(name);
    allowsChildren = true;
    SyntaxWrite sw = syntax.getSyntaxWrite();
    this.typesMap = typesMap;
    this.listvars = listvars;
    NodeList nodelist = e.getElementsByTagName("match");
    for (int i = 0; i < nodelist.getLength(); i++) {
      if (e.isEqualNode(nodelist.item(i).getParentNode())) {
        MatchExpr matchExpr = new MatchExpr((Element) nodelist.item(i), 1, listvars, sw);
        add(matchExpr);
      }
    }
    if (nodelist.getLength() == 0) {
      nodelist = e.getElementsByTagName("result"); // résultats directs
      for (int i = 0; i < nodelist.getLength(); i++) {
        Result result = new Result((Element) nodelist.item(i), 0, sw);
        add(result);
      }
    }
    setParent(null);    // TODO : changer pour prendre generator en compte dans l'arbre
  }
  
  /**
   * TODO : pour remplacer la méthode match de Expression
   * @param e l'expression à tester
   * @param s le modèle
   * @param vars table des remplacement pour que s devienne e
   * @return vrai si e est conforme au modèle s
   */
  public boolean match(Expression e, Expression s, HashMap<Expression, Expression> vars) {
    boolean fit;
    String vtype = (listvars.contains(s)) ? typesMap.get(s.getType()) : null;
    if (vtype != null) { // s est une variable représentant une expression de type vtype
      if (fit = subtypes.get(vtype).contains(e.getType())) { // e:type:vtype
        if (vars.get(s) != null) { // déjà dans la table vars
          fit = e.equals(vars.get(s));
        } else { // nouvelle entrée dans vars
          vars.put(s.copy(), e.copy());
        }
      }
    } else { 
      fit = e.getName().equals(s.getName()); // égalité des noms des noeuds
      if (fit && e.getChildren() != null && s.getChildren() != null) {
        if (fit = e.getChildren().size()==s.getChildren().size()) {
          for (int i = 0; i < e.getChildren().size(); i++) {
            if(!match(e.getChildren().get(i), s.getChildren().get(i), vars)) {
              return false;
            }
          }
        }
      }
    }
    return fit;
  }
  
  /**
   * transforme les variables de e et s pour obtenir e' et s' telles que e'=s'
   * @param e première expression
   * @param s seconde expression
   * @param evars variables de e à changer pour que e'=s'
   * @param svars variables de s à changer pour que e'=s'
   * @return 
   */
  public boolean matchBoth(Expression e, Expression s, 
          HashMap<Expression, Expression> evars, HashMap<Expression, Expression> svars) {
    boolean fit;
    Expression val;
    String vtype = (listvars.contains(s)) ? typesMap.get(s.getType()) : null;
    if (vtype != null) { // s est une variable
      if (fit = subtypes.get(vtype).contains(e.getType())) { // e:vtype
        if ((val = svars.get(s)) != null) { // déjà dans la table schvars
          fit = val.equals(e);
        } else { // nouvelle entrée dans svars
          svars.put(s.copy(), e.copy());
          svars.values().stream().forEach((value) -> {value = value.replace(evars);});
        }
      }     
    } else if (listvars.contains(e)) { // e est une variable
      if (fit = subtypes.get(typesMap.get(e.getType())).contains(s.getType())) { // erreur
        if ((val = evars.get(e)) != null) {
          fit = val.equals(s);
        } else {
          evars.put(e.copy(), s.copy());
          evars.values().stream().forEach((value) -> {value.replace(svars);});
        }
      }
    } else if (fit = e.getName().equals(s.getName())) {
      //fit &= !((e.getChildren()== null)^(e.getChildren()== null));
      if (fit = (e.getChildren()!= null) && (e.getChildren().size() == s.getChildren().size())) {
        for (int i = 0; i < e.getChildren().size(); i++) {
          Expression ei = e.getChildren().get(i), si = s.getChildren().get(i);
          fit = matchBoth(ei, si, evars, svars);
        }
      }
    }
    return fit;    
  }
  
  /**
   * examine les sous-expressions qui correspondent à s (obtenue) et leur donne le type de s
   * @param e
   * @param s expression déjà dans la liste obtenue
   * @param vars
   * @return
   */
  public boolean matchRecursively(Expression e, Expression s, HashMap<Expression, Expression> vars) {
    boolean fit = match(e, s, vars);
    if(fit) {
      e.setType(s.getType());
    }
    if(e.getChildren() != null) {
      e.getChildren().stream().forEach((child) -> {
        HashMap<Expression, Expression> nvars = new HashMap<>();
        matchRecursively(child, s, nvars);
      });
    }
    return fit;
  }
  
  /**
   * 
   * @param e l'expression à transformer
   * @param replaceMap table des transformations à effectuer (définitions)
   * @return une copie de l'expression éventuellement transformée
   */
  public Expression matchSubExpr(Expression e, HashMap<Expression, Expression> replaceMap) {
    Expression expr = e.copy();
    HashMap<Expression, Expression> vars = new HashMap<>();
    for (Expression key : replaceMap.keySet()) {
      if(match(expr, key, vars)) {
        expr = replaceMap.get(key).replace(vars);
      } 
    }
    if(expr.getChildren() != null) {
      for (int i = 0; i < expr.getChildren().size(); i++) {
        Expression child = expr.getChildren().get(i);
        expr.getChildren().set(i, matchSubExpr(child, replaceMap));
      }
    }
    return expr;
  }
  
  /**
   * si l'expression contient une variable de listvars, on fixe le boolean symbol de cette variable
   * à la valeur false. (utilisé dans MatchExpr.checkExpr)
   * @param e expression dont on recherche les variables
   */
  public void markUsedVars(Expression e) {
    int index = listvars.indexOf(e);
    if (index != -1) { // e est une variable
      listvars.get(index).setSymbol(false);
    } else if (e != null && e.getChildren() != null) {
      e.getChildren().stream().forEach(this::markUsedVars);
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
