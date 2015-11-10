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

import java.util.HashMap;
import org.w3c.dom.Element;

/**
 *
 * @author Patrice Maupou
 */
public class Result2 extends Schema2 {

  private final HashMap<Expr, Expr> changes;
  private final int level;

  /**
   *
   * @param result élément de document définissant l'instance pattern
   * @param depth nombre de conditions précédent ce résultat
   * @throws Exception
   */
  public Result2 (Element result, int depth) throws Exception {
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
        Expr[] exprs = new Expr[2];
        for (int i = 0; i < 2; i++) {
          String[] elems = couple[i].split(":");
          exprs[i] = new SExpr(elems[0], elems[1], false);
        }
        changes.put(exprs[0], exprs[1]);
        } else {
        throw new Exception("format de remplacement incorrect");
      }
    }
    setPattern(result);
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
  public HashMap<Expr, Expr> getChanges() {
    return changes;
  }


  public int getLevel() {
    return level;
  }
}
