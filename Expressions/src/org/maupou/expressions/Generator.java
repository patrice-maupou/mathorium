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
import java.util.Map;
import java.util.Set;
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
  private final HashMap<String, Set<String>> subtypes;
  private final ArrayList<Expression> listvars; // liste des variables

  public Generator(String name, Element elem, HashMap<String, Set<String>> subtypes) throws Exception {
    allowsChildren = true;
    this.name = name;
    setUserObject(name);
    this.subtypes = subtypes;
    NodeList nodesVariables = elem.getElementsByTagName("variable");
    listvars = new ArrayList<>(); // liste des variables
    for (int i = 0; i < nodesVariables.getLength(); i++) {
      Element lv = (Element) nodesVariables.item(i);
      String type = lv.getAttribute("type"); // type de la variable
      String list = lv.getAttribute("list");
      if (!list.isEmpty()) {
        String[] vars = list.trim().split("\\s");
        for (String var : vars) { // liste de variables marquées symbol
          listvars.add(new Expression(var, type, null, true));
        }
      }
    }
    nodesVariables = elem.getElementsByTagName("genrule");
    for (int i = 0; i < nodesVariables.getLength(); i++) {
      Element genRuleElement = (Element) nodesVariables.item(i);
      GenItem genItem = new GenItem(genRuleElement);
      add(genItem);
    }
  }

  Generator(ArrayList<Expression> listvars, HashMap<String, Set<String>> subtypes) {
    name = "test";
    this.listvars = listvars;
    this.subtypes = subtypes;
  }
  
  /**
   * teste l'expression expr en tenant compte de la table vars des variables déjà attribuées). 
   * transformation de l'expression par la table varMap
   * @param expr à tester
   * @param matchExpr le modèle suivant
   * @return true ssi l'expression est conforme au modèle
   */
  public boolean nextmatch(Expression expr, MatchExpr matchExpr) {
    boolean ret;
    HashMap<Expression, Expression> varsMap = matchExpr.getVarMap();
    if (expr != null) { // pattern : A->B type: prop
      HashMap<Expression, Expression> svars = new HashMap<>(), evars = new HashMap<>();
      if (matchExpr.isBidir()) {
        ret = matchBoth(expr, matchExpr.getPattern(), evars, svars);
      } else {
        ret = match(expr, matchExpr.getPattern(), svars);
      }
      if (varsMap.isEmpty()) { // ajouter les nouvelles variables à la table vars
        varsMap.putAll(svars);
        listvars.stream().forEach((var) -> {var.setSymbol(true);});
      } else { // ce n'est pas le premier modèle
        HashMap<Expression, Expression> nsvars = new HashMap<>(), nvars = new HashMap<>();
        for (Map.Entry<Expression, Expression> var : varsMap.entrySet()) {
          Expression svar = svars.get(var.getKey()); // A->B
          Expression e = var.getValue(); // (A->B)->C
          if (ret && svar != null) { // nsvars={A=(A->B)->C, B=B->C} mais pas le C de B:= 
            ret &= match(e, svar, nsvars);
          }
        }
        if (ret) {
          // renomme certaines variables
          svars.values().stream().forEach((e) -> {
            extendMap(e, nsvars);
          });
          // corrige vars avec svars
          varsMap.keySet().stream().forEach((var) -> {
            varsMap.put(var, varsMap.get(var).replace(nvars));
          });
          svars.keySet().stream().forEach((svar) -> {
            svars.put(svar, svars.get(svar).replace(nsvars));
          });
          varsMap.putAll(svars);
        }
      }
      markUsedVars(expr);
    } else { // expr est nulle : vérifier si le schéma correspond au type
      Expression e = matchExpr.getPattern().replace(varsMap);
      ret = matchExpr.getPattern().getType().equals(e.getType());
    }
    return ret;
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
    if (listvars.contains(s)) { // s est une variable représentant une expression de type vtype
      if (fit = subtypes.get(s.getType()).contains(e.getType())) { // e.type:s.type
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
    if (listvars.contains(s)) { // s est une variable
      if (fit = subtypes.get(s.getType()).contains(e.getType())) {
        if ((val = svars.get(s)) != null) { // déjà dans la table schvars
          fit = val.equals(e);
        } else { // nouvelle entrée dans svars
          svars.put(s.copy(), e.copy());
          svars.values().stream().forEach((value) -> {value.replace(evars);});
        }
      }     
    } else if (listvars.contains(e)) { // e est une variable
      if (fit = subtypes.get(e.getType()).contains(s.getType())) {
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
   * teste si une ExprNode est nouvelle
   *
   * @param en ExprNode à ajouter
   * @param result le modèle
   * @param exprNodes liste déjà établie
   * @return l'exprNode ou null si ne convient pa si elle est déjà dans la liste 
   */
  public boolean newExpr(ExprNode en, Result result, ArrayList<ExprNode> exprNodes)  {
    Expression e = result.getPattern().copy().replace(result.getVarMap());
    en.setE(e);
    for (ExprNode exprNode : exprNodes) {
      Expression expr = exprNode.getE();
      HashMap<Expression, Expression> nvars = new HashMap<>();
      if(matchRecursively(e, expr, nvars)) {
        if (!exprNode.getParentList().containsAll(en.getParentList())) {
          exprNode.getParentList().addAll(en.getParentList());
        } 
        return false;       
      }
    }
    return true;
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
  /**
   * ajoute à la liste vars les variables de listvars qui composent l'expression e
   *
   * @param e
   * @param vars
   */
  
  public void varsInExpression(Expression e, ArrayList<Expression> vars) {
    if (listvars.indexOf(e) == -1) {
      if (e.getChildren() != null) {
        e.getChildren().stream().forEach((child) -> {
          varsInExpression(child, vars);
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
   */
  public void extendMap(Expression e, HashMap<Expression, Expression> vars) {
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
        extendMap(child, vars);
      });
    }
  }
  
  /**
   * complète la table des variables et l'écriture des patterns
   * @param schema noeud inférieur
   * @param syntax 
   */
  public void setSchema(Schema schema, Syntax syntax) {
    for (Schema child : schema.getSchemas()) {
      String p = (syntax == null)? child.getPattern().toString() : 
              syntax.getSyntaxWrite().toString(child.getPattern());
      if(child instanceof MatchExpr) {
        child.setUserObject("modèle : " + p);
        varsInExpression(child.getPattern(), child.getVars());
      } else if(child instanceof Result) {
        child.setUserObject("résultat : " + p);
      }
      setSchema(child, syntax);
    }
  }
    
  @Override
  public String toString() {
    String ret = name + "\n";
    ret = schemas.stream().map((schema) -> schema.toString() + "\n").reduce(ret, String::concat);
    return ret;
  }
  
  
  public String getName() {
    return name;
  }


}
