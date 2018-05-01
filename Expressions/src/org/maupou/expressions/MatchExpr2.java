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
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice
 */
public class MatchExpr2 extends Schema2 {

  private final boolean bidir;
  private final boolean recursive;

  /**
   *
   * @param match
   * @param depth
   * @throws Exception
   */
  public MatchExpr2(Element match, int depth) throws Exception {
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
        MatchExpr2 matchChild = new MatchExpr2(echild, depth + 1);
        add(matchChild);
      }
    }    
    nodelist = match.getElementsByTagName("result");
    for (int i = 0; i < nodelist.getLength(); i++) {
      if (match.isEqualNode(nodelist.item(i).getParentNode())) { // niveau immédiatement inférieur
        Result2 result = new Result2((Element) nodelist.item(i), depth);
        result.varMap = varMap;
        add(result);
      }
    }
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
    recursive = "yes".equals(options.get("recursive"));
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

  public boolean isBidir() {
    return bidir;
  }

  public boolean isRecursive() {
    return recursive;
  }

}
