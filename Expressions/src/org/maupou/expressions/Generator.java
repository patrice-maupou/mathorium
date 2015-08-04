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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */
/**
 *
 * @author Patrice Maupou
 */
public class Generator extends Schema {

  private final String name;
  private final ArrayList<GenItem> discards; // pour écarter des expressions
  private final HashMap<String, Set<String>> subtypes;
  private final HashMap<String, String> typesMap; // type de la variable -> type remplacé
  private final ArrayList<Expression> listvars; // liste des variables

  public Generator(String name, Element elem, HashMap<String, Set<String>> subtypes) throws Exception {
    allowsChildren = true;
    this.name = name;
    setUserObject(name);
    discards = new ArrayList<>();
    typesMap = new HashMap<>(); // remplacement type de variable = type à remplacer
    this.subtypes = subtypes;
    NodeList nodesVariables = elem.getElementsByTagName("variable");
    listvars = new ArrayList<>(); // liste des variables
    for (int i = 0; i < nodesVariables.getLength(); i++) {
      Element lv = (Element) nodesVariables.item(i);
      String vname = lv.getAttribute("name"); // type de la variable
      String type = lv.getAttribute("type"); // le type représenté
      typesMap.put(vname, type);
      Set<String> typeSubtypes = subtypes.get(type);
      if (typeSubtypes == null) {
        typeSubtypes = new HashSet<>();
        typeSubtypes.add(type);
        subtypes.put(type, typeSubtypes);
      }
      typeSubtypes.add(vname);
      String list = lv.getAttribute("list");
      if (!list.isEmpty() && !type.isEmpty()) {
        String[] vars = list.trim().split("\\s");
        for (String var : vars) { // liste de variables marquées symbol
          listvars.add(new Expression(var, vname, null, true));
        }
      }
    }
    nodesVariables = elem.getElementsByTagName("genrule");
    for (int i = 0; i < nodesVariables.getLength(); i++) {
      Element genRuleElement = (Element) nodesVariables.item(i);
      GenItem genItem = new GenItem(genRuleElement, listvars);
      add(genItem);
    }
    nodesVariables = elem.getElementsByTagName("discard");
    for (int i = 0; i < nodesVariables.getLength(); i++) {
      Element ifElement = (Element) nodesVariables.item(i);
      discards.add(new GenItem(ifElement, listvars));
    }
  }

  Generator(HashMap<String, String> typesMap, ArrayList<Expression> listvars,
          HashMap<String, Set<String>> subtypes) {
    name = "test";
    this.listvars = listvars;
    this.typesMap = typesMap;
    this.subtypes = subtypes;
    discards = null;
  }
  
    
  /**
   * teste si e est conforme au modèle s
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
          svars.values().stream().forEach((value) -> {value.replace(evars);});
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
    String ret = name + "\n";
    ret = schemas.stream().map((genItem) -> genItem.toString() + "\n").reduce(ret, String::concat);
    return ret;
  }
  
  
  public String getName() {
    return name;
  }

  public ArrayList<GenItem> getDiscards() {
    return discards;
  }

  public HashMap<String, Set<String>> getSubtypes() {
    return subtypes;
  }

  public HashMap<String, String> getTypesMap() {
    return typesMap;
  }

  public ArrayList<Expression> getListvars() {
    return listvars;
  }


}
