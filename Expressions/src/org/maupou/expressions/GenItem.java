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

  /**
   * constructeur
   *
   * @param e élément de tagname "genrule" ou "discard"
   * @param listvars
   * @throws Exception
   */
  public GenItem(Element e, ArrayList<Expression> listvars) throws Exception {
    name = e.getAttribute("name");
    setUserObject(name);
    allowsChildren = true;
    NodeList nodelist = e.getElementsByTagName("match");
    for (int i = 0; i < nodelist.getLength(); i++) {
      if (e.isEqualNode(nodelist.item(i).getParentNode())) {
        MatchExpr matchExpr = new MatchExpr((Element) nodelist.item(i), 1, listvars);
        add(matchExpr);
      }
    }
    if (nodelist.getLength() == 0) {
      nodelist = e.getElementsByTagName("result"); // résultats directs
      for (int i = 0; i < nodelist.getLength(); i++) {
        Result result = new Result((Element) nodelist.item(i), 0);
        add(result);
      }
    }
  }  

  @Override
  public String toString() {
    return name;
  }

  
  @Override
  public String log() {
    return name;    
  }

}
