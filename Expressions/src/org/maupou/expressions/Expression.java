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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Patrice Maupou
 */
public class Expression {

  private String name;
  private String type;
  private ArrayList<Expression> children;
  private boolean symbol;

  /**
   * Création directe en connaissant tous les paramètres
   *
   * @param name
   * @param type
   * @param children
   * @param symbol
   */
  public Expression(String name, String type, ArrayList<Expression> children, boolean symbol) {
    this.name = name;
    this.type = type;
    this.children = children;
    this.symbol = symbol;
  }

  /**
   * Construction par étapes d'une expression
   *
   * @param text
   * @param syntax
   * @throws Exception si l'écriture est non valide
   */
  public Expression(String text, Syntax syntax) throws Exception {
    Expression e = syntax.parse(text);   
    symbol = false;
    if (e != null) {
      name = e.getName();
      type = e.getType();
      children = e.getChildren();
    } else {
      name = "non valid Expression";
      type = null;
      throw new Exception("Expression <" + text + "> non valide");
    }
  }

  /**
   * inverse de toText()
   *
   * @param text texte complet avec les types
   * @throws Exception si text n'est pas valide
   */
  public Expression(String text) throws Exception {
    symbol = false;
    ArrayList<Expression> list = new ArrayList<>();
    String[] ret = scanExpr(text.replaceAll("\\s",""), list);
    if (list.size() == 1 && ret[1].isEmpty()) {
      name = list.get(0).getName();
      type = list.get(0).getType();
      children = list.get(0).getChildren();
    } else {
      name = "non valid Expression";
      type = null;
      throw new Exception("Expression <" + text + "> non valide");
    }
  }

  /**
   * Décriptage du texte d'une suite d'expressions (avec les types)
   * la fin est basée sur les valeurs possibles du dernier match "simple;childType" si le marqueur
   * est "):childType" on ajoute l'expression à list, on retourne. "," on ajoute l'expression à
   * list, on continue
   *
   * @param text chaîne à analyser
   * @param list la liste des expressions scannées
   * @return le texte restant et le dernier marqueur
   */
  public static String[] scanExpr(String text, ArrayList<Expression> list) {
    String[] ret = new String[]{"", text};
    String mark = "";
    Matcher m = Pattern.compile("([^\\(\\),]+):(\\w+)(,|\\):|)").matcher(text);
    if (m.lookingAt()) { // expression simple
      String name = m.group(1);
      String type = m.group(2);
      mark = (m.groupCount() == 3) ? m.group(3) : "";
      list.add(new Expression(name, type, null, false));
      text = text.substring(m.end());
    } else { // composée
      m = Pattern.compile("\\((\\w+),").matcher(text); // exemple "(add,"
      if (m.lookingAt()) {
        String name = m.group(1);
        ArrayList<Expression> childs = new ArrayList<>();
        text = text.substring(m.end());
        ret = scanExpr(text, childs);
        text = ret[1];
        if (ret[0].equals("):") || ret[0].isEmpty()) { // fin normale
          m = Pattern.compile("(\\w+)(,|\\):|)").matcher(text);
          if (m.lookingAt()) {
            mark = (m.groupCount() == 2) ? m.group(2) : "";
            String type = m.group(1);
            Expression e = new Expression(name, type, childs, false);
            list.add(e); // liste remplacée par un seul élément
            text = text.substring(m.end());
          }
        }
      }
    }
    if ("):".equals(mark) || mark.isEmpty()) { // niveau terminé
      ret = new String[]{mark, text};
    } else if (",".equals(mark)) { // même niveau
      ret = scanExpr(text, list);
    }
    return ret;
  }


  /**
   * copie complète de l'expression
   *
   * @return
   */
  public Expression copy() {
    Expression e;
    if (children == null) {
      e = new Expression(name, type, null, isSymbol());
    } else {
      ArrayList<Expression> nchildren = new ArrayList<>();
      children.stream().map((children1) -> children1.copy()).forEach((child) -> {nchildren.add(child);});
      e = new Expression(name, type, nchildren, isSymbol());
    }
    return e;
  }

  /**
   *
   * @param map associe une expression à une expression de remplacement
   * @return la nouvelle expression
   */
  public Expression replace(HashMap<Expression, Expression> map) {
    Expression e = map.get(this);
    if (e == null) {
      if (children != null) {
        ArrayList<Expression> echilds = new ArrayList<>();
        children.stream().forEach((child) -> {echilds.add(child.replace(map));});
        e = new Expression(name, type, echilds, isSymbol());
      } else {
        e = new Expression(name, type, null, isSymbol());
      }
    }
    return e;
  }

  
  /**
   * si e est une sous-expression de l'expression actuelle, le etype est celui de e
   *
   * @param e
   */
  public void updateType(Expression e) {
    if (this.equals(e)) {
      type = e.getType();
    } else if (children != null) {
      children.stream().forEach((child) -> {child.updateType(e);});
    }
  }

   /**
   * pas d'égalité de types requis, seulement sur les noeuds de l'arbre
   *
   * @param obj
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    boolean ret = false;
    if (obj != null && obj instanceof Expression) {
      Expression eobj = (Expression) obj;
      if (ret = name.equals(eobj.getName())) {
        if (children != null && eobj.getChildren() != null && children.size() == eobj.getChildren().size()) {
          for (int i = 0; i < children.size(); i++) {
            if (!(ret = children.get(i).equals(eobj.getChildren().get(i)))) {
              break;
            }
          }
        } else {
          ret = children == null && eobj.getChildren() == null;
        }
      }
    }
    return ret;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 89 * hash + Objects.hashCode(this.name);
    hash = 89 * hash + Objects.hashCode(this.children);
    return hash;
  }

  /**
   * Ecriture complète de l'expression comprenant le childType et permettant de reconstruire
   * l'expression exemple : (ADD,3:natural,x:real):real
   *
   * @return la chaîne représentant l'expression
   */
  public String toText() {
    StringBuilder sb = new StringBuilder(getName());
    if (getChildren() != null) {
      sb.insert(0, "(");
      getChildren().stream().forEach((child) -> {
        sb.append(",");
        sb.append(child.toText());
      });
      sb.append(")");
    }
    sb.append(":").append(type);
    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getName());
    if (getChildren() != null) {
      sb.insert(0, "(");
      getChildren().stream().forEach((expression) -> {
        sb.append(",");
        sb.append(expression);
      });
      sb.append(")");
    }
    return sb.toString();
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public ArrayList<Expression> getChildren() {
    return children;
  }
  public void setChildren(ArrayList<Expression> children) {
    this.children = children;
  }
  
  public boolean isSymbol() {
    return symbol;
  }

  public void setSymbol(boolean symbol) {
    this.symbol = symbol;
  }

}
