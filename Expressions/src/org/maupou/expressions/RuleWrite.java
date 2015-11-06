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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Patrice
 */
public class RuleWrite {

  private final SExpr node;
  private final String value;
  private final String[] childs;
  private final String[] vReplace;
  private final List<Expr>[] vNodes;
  //*/

  /**
   *
   * @param value
   * @param node
   * @param childs
   * @param childmap
   */
  public RuleWrite(String value, SExpr node, String[] childs, HashMap<String, String[]> childmap) {
    this.node = node;
    int n = childs.length;
    vReplace = new String[n];
    vNodes = new List[n];
    Set<String> vars = childmap.keySet();
    for (int i = 0; i < childs.length; i++) {
      String child = childs[i];
      vNodes[i]= new ArrayList<>();
      childs[i] = "\u0000"+child;
      vReplace[i] = childs[i];
      value = value.replace(child, childs[i]);
      if (vars.contains(child)) {
        String[] s = childmap.get(child);
        vReplace[i] = s[0].replace(child, childs[i]);
        List<String> conditions = Arrays.asList(s[1].split(","));
        for (String condition : conditions) { // exemple : ADD:func,SUB:func
          String[] cond = condition.split(":");
          if (s.length == 2) {
            vNodes[i].add(new SExpr(cond[0], cond[1], false));
          }
        }
      } 
    }
    this.childs = childs;
    this.value = value;
  }

  @Override
  public String toString() {
    String ret = value.replace("\u0000", "") + "\n";
    return ret;
  }

  public SExpr getNode() {
    return node;
  }

  public String[] getChilds() {
    return childs;
  }


  public String getValue() {
    return value;
  }



  public String[] getvReplace() {
    return vReplace;
  }

  public List<Expr>[] getvNodes() {
    return vNodes;
  }

}
