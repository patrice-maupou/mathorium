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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */
public class SyntaxWrite {

  private final HashMap<String, NodeWrite> nameToNode;
  private final ArrayList<NodeWrite> nodeWrites;
  private final ArrayList<RuleWrite> ruleWrites;
  private final HashMap<String, Set<String>> subtypes;
  private final String name;

  public SyntaxWrite(Element write, HashMap<String, Set<String>> subtypes, String version) throws Exception {
    nameToNode = new HashMap<>();
    nodeWrites = new ArrayList<>();
    ruleWrites = new ArrayList<>();
    this.subtypes = subtypes;
    this.name = write.getAttribute("name"); // nom de cette syntaxe
    NodeList rulesList = write.getElementsByTagName("wrule");
    for (int i = 0; i < rulesList.getLength(); i++) {
      Element rule = (Element) rulesList.item(i);
      String[] childs = rule.getAttribute("child").split(",");
      HashMap<String, String[]> childmap = new HashMap<>();
      NodeList nodechilds = rule.getElementsByTagName("child");
      for (int j = 0; j < nodechilds.getLength(); j++) {
        Element item = (Element) nodechilds.item(j);
        String[] s = item.getAttribute("replace").split("->");
        String[] replacement = new String[]{s[1], item.getAttribute("rules")};
        childmap.put(s[0], replacement);
      }
      if ("0".equals(version)) {
        NodeList nodes = rule.getElementsByTagName("node"); // nom du noeud
        for (int j = 0; j < nodes.getLength(); j++) {
          Element elem = (Element) nodes.item(j);
          String node_name = elem.getAttribute("name");
          nameToNode.put(node_name, new NodeWrite(elem, childs, childmap));
          nodeWrites.add(new NodeWrite(elem, childs, childmap));
        }
      } else {
        String[] nodetext = rule.getAttribute("node").split(":");
        SExpr node = null;
        if (nodetext.length == 2) {
          node = new SExpr(nodetext[0], nodetext[1], false);
        }
        String text = rule.getTextContent().trim();
        ruleWrites.add(new RuleWrite(text, node, childs, childmap));
      }
    }
  }

  /**
   * Ecriture de l'expression e par la syntaxe
   *
   * @param e l'expression
   * @return la chaîne représentant e
   */
  public String toString(Expression e) {
    String ret = null;
    if (e != null) {
      if (e.getChildren() == null) { // expression simple
        ret = e.toString();
      } else {
        for (NodeWrite node : nodeWrites) {
          String nodename = node.getName();
          if (e.getName().matches(nodename)) {
            ret = node.getValue();
            for (Map.Entry<Integer, ChildReplace> entry : node.getMapreplace().entrySet()) {
              Integer j = entry.getKey();
              ChildReplace childReplace = entry.getValue();
              String childname = childReplace.getName();
              String replace = childReplace.getReplacement();
              List<String> nodeNames = childReplace.getNodeNames();
              Expression child = e.getChildren().get(j);
              String ewr = toString(child);
              replace = (nodeNames.contains(child.getName())) ? replace.replace(childname, ewr) : ewr;
              int pos = ret.indexOf("\u0000");
              ret = ret.substring(0, pos) + replace + ret.substring(pos + 1);
            }
            break;
          }
        }
      }
    }
    return ret;
  }

  /**
   * Ecriture de l'Expr e par la syntaxe
   *
   * @param e l'expression
   * @return l'écriture de e
   */
  public String toString(Expr e) {
    String ret = null;
    if (e != null) {
      if(e instanceof SExpr) {
        ret = e.toString();
      } else {
        CExpr expr = (CExpr) e;
        for (RuleWrite rule : ruleWrites) {
          SExpr node = rule.getNode(); // exemples : ADD:func , :func avec f variable
          boolean match = (node.getName().isEmpty())?
                  subtypes.get(node.getType()).contains(e.getType()) : node.equals(expr.getNode());
          if(match) {
            ret = rule.getValue();
            List<Expr> list = new ArrayList<>();
            list.add(expr.getNode());
            list.addAll(expr.getList());
            int n = rule.getChilds().length;
            for (int i = 0; i < n ; i++) {
              String var = rule.getChilds()[n-1-i];
              String replace = rule.getvReplace()[n-1-i];
              List<Expr> nodes = rule.getvNodes()[n-1-i];
              Expr child = list.get(list.size()-1-i);   
              String ch = toString(child);
              if(nodes.contains(child.getNode())) {
                replace = replace.replace(var, ch);
              } else {
                replace = ch;
              }
              ret = ret.replace(var, replace);
            }
            break;
          }
        }
      }
    }
    return ret;
  }
  
  @Override
  public String toString() {
    String ret = "";
    for (Map.Entry<String, NodeWrite> entry : nameToNode.entrySet()) {
      ret += entry.getKey() + " : " + entry.getValue() + "\n";
    }
    return ret;
  }

  /**
   * associe à chaque nom de noeud un Nodewrite décrivant la manière de l'écrire
   *
   * @return la table
   */
  public HashMap<String, NodeWrite> getNameToNode() {
    return nameToNode;
  }

  public ArrayList<NodeWrite> getNodeWrites() {
    return nodeWrites;
  }

  public String getName() {
    return name;
  }
}
