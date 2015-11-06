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
 * @author Patrice Maupou
 */
public class SyntaxPattern {

  private final ArrayList<TypeCheck> typeChecks;
  private final String name;
  private final Expr node, flatten;
  private final String patternText;

  /**
   * Crée un élément de syntaxe
   * @param patternItem élément du document
   * @param childs les variables utilisées
   * @param subtypes map qui associe à un type ses sous-types
   */
  public SyntaxPattern(Element patternItem, String[] childs, HashMap<String, Set<String>> subtypes) {
    name = patternItem.getAttribute("node");
    String[] s = name.split(":");
    switch (s.length) {
      case 1 :
        node = (name.isEmpty())? null : new SExpr(s[0], null, false);
        break;
      case 2:
        node = new SExpr(s[0], s[1], false);
        break;
      default:
        node = null; 
    }
    String f = patternItem.getAttribute("flatten");
    s = f.split(":");
    switch (s.length) {
      case 1:
        flatten = (f.isEmpty())? null :  new SExpr(s[0], null, false);
        break;
      case 2:
        flatten = new SExpr(s[0], s[1], false);
        break;
      default:
        flatten = null;        
    }
    // types admissibles pour ce modèle
    typeChecks = new ArrayList<>();
    patternText = patternItem.getTextContent().trim();
    String txt = patternText;
    for (String child : childs) {
      txt = txt.replace(child, "(\u0000_*)");
    }
    NodeList typeList = patternItem.getElementsByTagName("type");
    for (int i = 0; i < typeList.getLength(); i++) {
      Element typeItem = (Element) typeList.item(i);
      String type = TypeCheck.addSubTypes(typeItem, subtypes);
      String[] typeValues = typeItem.getAttribute("value").split(",");
      if (type.equals("inherit") && typeValues.length == 1) {
        String[] typeOptions = typeValues[0].split("\\|");
        for (String typeOption : typeOptions) {
          typeValues = new String[]{typeOption};
          typeChecks.add(new TypeCheck(typeOption, childs, typeValues));
        }
      }
      else {
        typeChecks.add(new TypeCheck(type, childs, typeValues));
      }
    }
  }
  

  /**
   * typeCheck contient un des types du modèle et la correspondance child -> type
   * @return la liste des typeChecks de ce modèle
   */
  public ArrayList<TypeCheck> getTypeChecks() {
    return typeChecks;
  }

  public String getName() {
    return name;
  }

  public Expr getNode() {
    return node;
  }

  public Expr getFlatten() {
    return flatten;
  }


  @Override
  public String toString() {
    String ret = "    name : " + name + "  pattern : " + patternText;
    ret = typeChecks.stream().map((typeCheck) -> "\n\t" + typeCheck).reduce(ret, String::concat);
    return ret;
  }

}
