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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Element;

/**
 * établit un lien entre un type et ceux d'une liste de noms de variables
 * @author Patrice Maupou
 */
public class TypeCheck {

  private final String type;
  private final HashMap<String, String> childtypes; // map : variable -> type

  /**
   *
   * @param typeItem
   * @param childNames liste des noms des enfants
   */
  TypeCheck(String type, String[] childNames, String[] typeValues) {
    this.type = type;
    int n = typeValues.length;
    childtypes = new HashMap<>();
    for (int i = 0; i < childNames.length; i++) {
      String childtype = (i < n) ? typeValues[i] : typeValues[n - 1];
      childtypes.put(childNames[i], childtype);
    }
  }

  /**
   * add subTypes to type defined by an Element.
   * @param typeItem
   * @param subtypes
   * @return
   */
  public static String addSubTypes(Element typeItem, HashMap<String, Set<String>> subtypes) {
    String type = typeItem.getAttribute("name");
    if ("inherit".equals(type) || type.isEmpty()) {
      return type;
    }
    Set<String> eset = subtypes.get(type);
    if (eset == null) {
      eset = new HashSet<>(20);
      eset.add(type);
      subtypes.put(type, eset);
    }
    if (typeItem.hasAttribute("subtypes")) {
      String[] typesArray = typeItem.getAttribute("subtypes").split(",");
      for (int i = 0; i < typesArray.length; i++) {
        String subtype = typesArray[i];
        eset.add(subtype);
        if(subtypes.get(subtype) != null) {
          eset.addAll(subtypes.get(subtype));
        }
      }
    }
    return type;
  }

  public String getType() {
    return type;
  }

  public HashMap<String, String> getChildtypes() {
    return childtypes;
  }

  @Override
  public String toString() {
    String ret = type + "  <-  ";
    for (Map.Entry<String, String> entry : childtypes.entrySet()) {
      ret += entry.getKey() + ":" + entry.getValue() + "  ";
    }
    return ret;
  }
}
