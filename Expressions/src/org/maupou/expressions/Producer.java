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
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice
 */
public class Producer extends Schema2 {

  private final String name;
  private final HashMap<String, Set<String>> subtypes;
  private final ArrayList<SExpr> listvars; // liste des variables

  Producer(ArrayList<SExpr> listvars, HashMap<String, Set<String>> subtypes) {
    name = "test";
    this.listvars = listvars;
    this.subtypes = subtypes;
  }

  public Producer(Element elem, HashMap<String, Set<String>> subtypes, ArrayList<SExpr> listvars)
          throws Exception {
    allowsChildren = true;
    name = elem.getAttribute("name");
    setUserObject(name);
    this.subtypes = subtypes;
    NodeList nodes = elem.getElementsByTagName("variable");
    this.listvars = listvars; // liste des variables
    for (int i = 0; i < nodes.getLength(); i++) {
      Element lv = (Element) nodes.item(i);
      String type = lv.getAttribute("type"); // type de la variable
      String list = lv.getAttribute("list");
      if (!list.isEmpty()) {
        String[] vars = list.trim().split("\\s");
        for (String var : vars) { // liste de variables marquées symbol
          listvars.add(new SExpr(var, type, true));
        }
      }
    }
    nodes = elem.getElementsByTagName("generator");
    for (int i = 0; i < nodes.getLength(); i++) {
      Element prodElem = (Element) nodes.item(i);
      if (elem.isEqualNode(prodElem.getParentNode())) {
        add(new Producer(prodElem, subtypes, listvars));
      }
    }
    nodes = elem.getElementsByTagName("match");
    for (int i = 0; i < nodes.getLength(); i++) {
      Element matchElem = (Element) nodes.item(i);
      if (elem.isEqualNode(matchElem.getParentNode())) {
        add(new MatchExpr2(matchElem, 1));
      }
    }
  }

  /**
   * teste si e est conforme au modèle s
   *
   * @param e l'expression à tester
   * @param s le modèle
   * @param vars table des remplacement pour que s devienne e
   * @return vrai si e est conforme au modèle s
   */
  public boolean match(Expr e, Expr s, HashMap<Expr, Expr> vars) {
    boolean fit;
    if (s instanceof SExpr) { // s est une expression simple
      fit = match(e, (SExpr) s, vars);
    } else { // s est une expr composée
      fit = match(e.getNode(), s.getNode(), vars);
      if (fit && e.getList() != null && s.getList() != null) {
        if (fit = e.getList().size() == s.getList().size()) {
          for (int i = 0; i < e.getList().size(); i++) {
            if (!match(e.getList().get(i), s.getList().get(i), vars)) {
              return false;
            }
          }
        }
      }
    }
    return fit;
  }

  private boolean match(Expr e, SExpr s, HashMap<Expr, Expr> vars) {
    boolean fit = listvars.contains(s);
    if (fit) {
      if (fit = subtypes.get(s.getType()).contains(e.getType())) { // e.type : s.type
        if (vars.get(s) != null) { // déjà dans la table vars
          fit = e.equals(vars.get(s));
        } else { // nouvelle entrée dans vars
          vars.put(s.copy(), e.copy());
        }
      }
    } else { // s est une constante
      fit = e.equals(s);
    }
    return fit;
  }

  /**
   * transforme les variables de e et s pour obtenir e' et s' telles que e'=s'
   *
   * @param e première expression
   * @param s seconde expression
   * @param evars variables de e à changer pour que e'=s'
   * @param svars variables de s à changer pour que e'=s'
   * @return
   */
  public boolean matchBoth(Expr e, Expr s, HashMap<Expr, Expr> evars, HashMap<Expr, Expr> svars) {
    boolean fit;
    if (s instanceof SExpr) {
      fit = match(e, (SExpr) s, svars);
      svars.values().stream().forEach((value) -> {
        value.replace(evars);
      });
    } else if (e instanceof SExpr) {
      fit = match(s, (SExpr) e, evars);
      evars.values().stream().forEach((value) -> {
        value.replace(svars);
      });
    } else if (fit = e.getNode().equals(s.getNode())) {
      if (fit = (e.getList() != null) && (e.getList().size() == s.getList().size())) {
        for (int i = 0; i < e.getList().size(); i++) {
          Expr ei = e.getList().get(i), si = s.getList().get(i);
          fit = matchBoth(ei, si, evars, svars);
        }
      }
    }
    return fit;
  }

  /**
   *
   * @param e l'expression à transformer
   * @param m une sous-expression de e doit être conforme à ce modèle
   * @param r fournit la transformée
   * @return
   */
  public Expr matchSubExpr(Expr e, Expr m, Expr r) {
    Expr expr = e.copy();
    HashMap<Expr, Expr> vars = new HashMap<>();
    if (match(expr, m, vars)) {
      return r.replace(vars);
    }
    if (expr.getList() != null) {
      for (int i = 0; i < expr.getList().size(); i++) {
        Expr child = expr.getList().get(i);
        Expr nchild = matchSubExpr(child, m, r);
        if (nchild != null) {
          expr.getList().set(i, nchild);
          return expr;
        }
      }
    }
    return null;
  }

  /**
   * si l'expression contient une variable de listvars, on fixe le boolean symbol de cette variable
   * à la valeur false. (utilisé dans MatchExpr.checkExpr)
   *
   * @param e expression dont on recherche les variables
   */
  public void markUsedVars(Expr e) {
    int index = listvars.indexOf(e);
    if (index != -1) { // e est une variable
      listvars.get(index).setSymbol(false);
    } else if (e != null && e.getList() != null) {
      e.getList().stream().forEach(this::markUsedVars);
    }
  }

  /**
   * ajoute à la liste vars les variables de listvars qui composent l'expression e
   *
   * @param e
   * @param vars
   */
  public void varsInExpression(Expr e, ArrayList<Expr> vars) {
    if (listvars.indexOf(e) == -1) {
      if (e.getList() != null) {
        e.getList().stream().forEach((child) -> {
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
  public void extendMap(Expr e, HashMap<SExpr, Expr> vars) {
    int index = listvars.indexOf(e);
    if (index != -1) { // e est une variable
      SExpr evar = listvars.get(index);
      if (!evar.isSymbol() && !vars.containsKey(evar)) { // evar est une variable déjà utilisée 
        for (SExpr var : listvars) {
          if (var.isSymbol() && var.getType().equals(e.getType())) {
            var.setSymbol(false);
            vars.put(evar, var); // changement de variable
            break;
          }
        }
      }
    } else if (e.getList() != null) {
      e.getList().stream().forEach((child) -> {
        extendMap(child, vars);
      });
    }
  }

  /**
   * complète la table des variables et l'écriture des patterns
   *
   * @param schema noeud inférieur
   * @param syntax
   */
  public void setSchema(Schema2 schema, Syntax syntax) {
    Schema2 pre = null;
    for (Schema2 child : schema.getSchemas()) {
      if (pre != null) {
        pre.setNext(child);
      }
      String p = (syntax == null) ? child.getPattern().toString()
              : syntax.getSyntaxWrite().toString(child.getPattern());
      if (child instanceof MatchExpr2) {
        child.setUserObject("modèle : " + p);
        //varsInExpression(child.getPattern(), child.getVars());
      } else if (child instanceof Result2) {
        child.setUserObject("résultat : " + p);
      }
      setSchema(child, syntax);
      pre = child;
    }
    if (pre != null) {
      pre.setNext(pre.parent);
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

  @Override
  public String log() {
    return name;
  }
}
