/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */
public class SyntaxWrite {

  private final HashMap<String, NodeWrite> nameToNode;
  private final ArrayList<NodeWrite> nodeWrites;
  private final String unused;
  private final String name;

  public SyntaxWrite(Element write, String unused) throws Exception {
    nameToNode = new HashMap<>();
    nodeWrites = new ArrayList<>();
    this.unused = unused;
    this.name = write.getAttribute("name");
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
      NodeList nodes = rule.getElementsByTagName("node");
      for (int j = 0; j < nodes.getLength(); j++) {
        Element node = (Element) nodes.item(j);
        String node_name = node.getAttribute("name");
        nameToNode.put(node_name, new NodeWrite(node, childs, childmap, unused));
        nodeWrites.add(new NodeWrite(node, childs, childmap, unused));
      }
    }
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

  public String getUnused() {
    return unused;
  }

    public String getName() {
        return name;
    }
}
