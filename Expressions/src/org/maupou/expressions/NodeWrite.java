/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.w3c.dom.Element;

/**
 *
 * @author Patrice Maupou
 */
public class NodeWrite {

  private String name;
  private String initvalue;
  private String value;
  private String var;
  private TreeMap<Integer, ChildReplace> mapReplace;

  /**
   * donne la valeur du noeud, et de la table replacing
   * @param node élément définissant
   * @param childs liste des noms des variables enfant
   * @param childmap associe le remplacement éventuel et les conditions de remplacement
   * @throws Exception
   */
  public NodeWrite(Element node, String[] childs, HashMap<String, String[]> childmap, String unused)
          throws Exception {
    name = node.getAttribute("name");
    initvalue = node.getTextContent().trim();
    var = node.getAttribute("var");
    value = initvalue;
    mapReplace = new TreeMap<>();
    ChildReplace childReplace;
    Set<String> vars = childmap.keySet();
    for (int i = 0; i < childs.length; i++) {
      String child = childs[i];
      value = value.replace(child, unused); // ex: a+b -> <unused>+<unused>
      if(vars.contains(child)) {
        String[] s = childmap.get(child);
        String replacement = s[0];
        List<String> conditions = Arrays.asList(s[1].split(","));
        childReplace = new ChildReplace(child, replacement, conditions);
      }
      else {
        childReplace = new ChildReplace(child, child, new ArrayList<String>());
      }
      mapReplace.put(i, childReplace);
    }
  }

  @Override
  public String toString() {
    String ret = initvalue + "\n";
    for (Map.Entry<Integer, ChildReplace> entry : getMapreplace().entrySet()) {
      ret += "  " + entry.getKey() + ": " + entry.getValue()+"\n";
    }
    return ret;
  }

  public String getName() {
    return name;
  }

  /**
   * écriture du noeud
   * @return la chaîne correspondante
   */
  public String getValue() {
    return value;
  }

  public String getVar() {
    return var;
  }

  public TreeMap<Integer, ChildReplace> getMapreplace() {
    return mapReplace;
  }



}
