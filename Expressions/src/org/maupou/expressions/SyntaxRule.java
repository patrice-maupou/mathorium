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
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */
public class SyntaxRule {

  private final TreeMap<Integer, SyntaxPattern> syntaxPatternGroups;  // rang du groupe -> modèle du groupe
  private String id;              // nom du modèle et nom du noeud de l'expression
  private final String[] childs;  // noms des variables représentant les enfants
  private final Pattern patternRule;

  /**
   * Crée une règle de syntaxe
   * 
   * @param e
   * @param subtypes
   */
  public SyntaxRule(Element e, HashMap<String, Set<String>> subtypes) {
    id = e.getAttribute("key");
    String childText = e.getAttribute("child");
    String group = "(\u0000_*)"; 
    childs = (childText.isEmpty()) ? new String[0] : childText.split(",");
    syntaxPatternGroups = new TreeMap<>();
    String patternRuleTxt = "";
    String after = ""; // pour les priorités
    for (int i = 0; i < e.getChildNodes().getLength(); i++) {
      Node childNode = e.getChildNodes().item(i);
      if(childNode.getNodeType() == Node.CDATA_SECTION_NODE) {
        after = childNode.getNodeValue();
        break;
      }
    }
    NodeList patternList = e.getElementsByTagName("pattern");
    int grcnt = 2;
    for (int i = 0; i < patternList.getLength(); i++) {
      if(i > 0) patternRuleTxt += "|";
      Element patternItem = (Element) patternList.item(i);
      String patternText = patternItem.getTextContent().trim();
      SyntaxPattern syntaxPattern = new SyntaxPattern(patternItem, childs, subtypes);
      syntaxPatternGroups.put(grcnt, syntaxPattern);
      for (String child : childs) {
        patternText = patternText.replace(child, group);        
      }
      Pattern p = Pattern.compile(patternText);
      grcnt += p.matcher("").groupCount()+1;
      patternRuleTxt += "(" + patternText + ")";
    }    
    patternRuleTxt = "(" + patternRuleTxt + ")" + after; 
    patternRule = Pattern.compile(patternRuleTxt.trim());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String[] getChilds() {
    return childs;
  }

  public Pattern getPatternRule() {
    return patternRule;
  }

  public TreeMap<Integer, SyntaxPattern> getSyntaxPatternGroups() {
    return syntaxPatternGroups;
  }


  @Override
  public String toString() {
    String ret = id + "\n";
    ret += "pattern : " + getPatternRule() + "\n";
    for (Integer key : syntaxPatternGroups.keySet()) {
      ret += syntaxPatternGroups.get(key) + "\n";
    }
    return ret;
  }
}
