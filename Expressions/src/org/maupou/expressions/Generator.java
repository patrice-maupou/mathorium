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
public class Generator {

  private final String name;
  private final ArrayList<GenItem> genItems; // pour créer des expressions
  private final ArrayList<GenItem> discards; // pour écarter des expressions

  public Generator(String name, Element elem, Syntax syntax) throws Exception {
    this.name = name;
    genItems = new ArrayList<>();
    discards = new ArrayList<>();
    NodeList nodesVariables = elem.getElementsByTagName("variable");
    HashMap<String, String> freevars = new HashMap<>(); // remplacement type de variable = type à remplacer
    ArrayList<Expression> listvars = new ArrayList<>(); // liste des variables
    for (int i = 0; i < nodesVariables.getLength(); i++) {
      Element lv = (Element) nodesVariables.item(i);
      String vname = lv.getAttribute("name"); // type de la variable
      String type = lv.getAttribute("type"); // le type représenté
      freevars.put(vname, type);
      Set<String> subtypes = syntax.getSubtypes().get(type);
      if (subtypes == null) {
        subtypes = new HashSet<>();
        subtypes.add(type);
        syntax.getSubtypes().put(type, subtypes);
      }
      subtypes.add(vname);
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
      genItems.add(new GenItem(genRuleElement, syntax, freevars, listvars));
    }
    nodesVariables = elem.getElementsByTagName("discard");
    for (int i = 0; i < nodesVariables.getLength(); i++) {
      Element ifElement = (Element) nodesVariables.item(i);
      discards.add(new GenItem(ifElement, syntax, freevars, listvars));
    }
  }

  @Override
  public String toString() {
    String ret = name + "\n";
    ret = genItems.stream().map((genItem) -> genItem.toString() + "\n").reduce(ret, String::concat);
    return ret;
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

}
