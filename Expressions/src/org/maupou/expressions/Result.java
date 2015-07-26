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
import java.util.Map;
import org.w3c.dom.Element;

/**
 *
 * @author Patrice Maupou
 */
public class Result extends Schema {

  private final HashMap<Expression, Expression> changes;
  private final int level;

  /**
   *
   * @param result élément de document définissant l'instance pattern
   * @param depth nombre de conditions précédent ce résultat
   * @param sw
   * @throws Exception
   */
  public Result (Element result, int depth, SyntaxWrite sw) throws Exception {
    allowsChildren = false;
    rgs = new int[depth];
    int l = 0;
    try {
      if (result.hasAttribute("level")) {
        l = Integer.parseInt(result.getAttribute("level"));
      }
    } catch (NumberFormatException nfe) {
      throw new Exception("level incorrect : " + nfe.getMessage());
    }
    level = l;
    changes = new HashMap<>();
    String replace = result.getAttribute("changes");
    if (!replace.isEmpty()) {
      String[] couple = replace.split("/");
      if (couple.length == 2) {
        changes.put(new Expression(couple[0]), new Expression(couple[1]));
      } else {
        throw new Exception("format de remplacement incorrect");
      }
    }
    setPattern(result);
    setUserObject("résultat : " + getPattern().toString(sw));
  }

  /**
   * teste si une ExprNode est nouvelle
   *
   * @param en ExprNode à ajouter
   * @param typesMap table des types associés aux types des variables (ex: nv=natural)
   * @param listvars
   * @param syntax
   * @param exprNodes liste déjà établie
   * @return l'exprNode ou null si ne convient pa si elle est déjà dans la liste 
   */
  public boolean newExpr(ExprNode en, HashMap<String, String> typesMap, ArrayList<Expression> listvars, 
          Syntax syntax, ArrayList<ExprNode> exprNodes)  {
    Expression e = getPattern().copy().replace(varMap);
    en.setE(e);
    for (ExprNode exprNode : exprNodes) {
      Expression expr = exprNode.getE();
      HashMap<Expression, Expression> nvars = new HashMap<>();
      if (e.matchRecursively(expr, typesMap, listvars, nvars, syntax.getSubtypes(), en)) {
        // déjà dans la liste (aux variables près)
        if (!exprNode.getParentList().containsAll(en.getParentList())) {
          exprNode.getParentList().addAll(en.getParentList());
        }
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    String ret = "";
    if (!changes.isEmpty()) {
      ret = changes.entrySet().stream().map((change)
              -> "(" + change.getValue() + "/" + change.getKey() + ")").reduce(ret, String::concat);
    }
    ret += getPattern().toString();
    return ret;
  }

  /**
   * table de changements d'expressions (non utilisé)
   *
   * @return
   */
  public HashMap<Expression, Expression> getChanges() {
    return changes;
  }


  public int getLevel() {
    return level;
  }
}
