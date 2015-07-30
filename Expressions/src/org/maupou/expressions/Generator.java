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
import java.util.Set;
import java.util.TreeMap;
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
  private final ArrayList<GenItem> genItems; // pour créer des expressions
  private final ArrayList<GenItem> discards; // pour écarter des expressions
  private HashMap<String, Set<String>> subtypes;

  public Generator(String name, Element elem, Syntax syntax) throws Exception {
    allowsChildren = true;
    this.name = name;
    genItems = new ArrayList<>();
    discards = new ArrayList<>();
    NodeList nodesVariables = elem.getElementsByTagName("variable");
    HashMap<String, String> typesMap = new HashMap<>(); // remplacement type de variable = type à remplacer
    ArrayList<Expression> listvars = new ArrayList<>(); // liste des variables
    for (int i = 0; i < nodesVariables.getLength(); i++) {
      Element lv = (Element) nodesVariables.item(i);
      String vname = lv.getAttribute("name"); // type de la variable
      String type = lv.getAttribute("type"); // le type représenté
      typesMap.put(vname, type);
      subtypes = syntax.getSubtypes();
      Set<String> typeSubtypes = syntax.getSubtypes().get(type);
      if (typeSubtypes == null) {
        typeSubtypes = new HashSet<>();
        typeSubtypes.add(type);
        syntax.getSubtypes().put(type, typeSubtypes);
      }
      typeSubtypes.add(vname);
      String list = lv.getAttribute("list");
      if (!list.isEmpty() && !type.isEmpty()) {
        String[] vars = list.trim().split("\\s");
        for (String var : vars) { // liste de variables marquées symbol
          listvars.add(new Expression(var, vname, null, true));
        }
      }
    }
    nodesVariables = elem.getElementsByTagName("genrule");
    for (int i = 0; i < nodesVariables.getLength(); i++) {
      Element genRuleElement = (Element) nodesVariables.item(i);
      GenItem genItem = new GenItem(genRuleElement, syntax, typesMap, listvars, subtypes);
      setGenItemAncestor(genItem, genItem);
      add(genItem);
      //*/
    }
    schemas.stream().forEach((schema) -> {
      genItems.add((GenItem) schema);
    });
    nodesVariables = elem.getElementsByTagName("discard");
    for (int i = 0; i < nodesVariables.getLength(); i++) {
      Element ifElement = (Element) nodesVariables.item(i);
      discards.add(new GenItem(ifElement, syntax, typesMap, listvars, subtypes));
    }
  }
  
  @Override
  public String toString() {
    String ret = name + "\n";
    ret = genItems.stream().map((genItem) -> genItem.toString() + "\n").reduce(ret, String::concat);
    return ret;
  }
  
  /**
   * Etablit genItem comme le genItemParent des branches inférieures
   * @param schema pour MatchExpr ou Result
   * @param genItem 
   */
  private void setGenItemAncestor(Schema schema, GenItem genItem) {
    schema.setGenItemParent(genItem);
    schema.getSchemas().stream().forEach((child) -> {setGenItemAncestor(child, genItem);});
  }


  public ArrayList<GenItem> getGenItems() {
    return genItems;
  }

  public String getName() {
    return name;
  }

  public ArrayList<GenItem> getDiscards() {
    return discards;
  }

  public HashMap<String, Set<String>> getSubtypes() {
    return subtypes;
  }

}
