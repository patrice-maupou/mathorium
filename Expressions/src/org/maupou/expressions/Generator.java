/*   Generator.java
 * the code source is distributed under the GPL.
 * Please see http://www.fsf.org/copyleft/gpl.html
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
        if(subtypes == null) {
            subtypes = new HashSet<>();
            subtypes.add(type);
            syntax.getSubtypes().put(type, subtypes);
        }
        subtypes.add(vname);
        String list = lv.getAttribute("list");
        if(!list.isEmpty() && !type.isEmpty()) {
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
    for (GenItem genItem : genItems) {
      ret += genItem.toString() + "\n";
    }
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
