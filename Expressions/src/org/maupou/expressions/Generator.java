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
  private final ArrayList<GenItem> genItems;
  private final ArrayList<GenItem> discards;

  public Generator(String name, Element elem, Syntax syntax) throws Exception {
    this.name = name;
    genItems = new ArrayList<>();
    discards = new ArrayList<>();
    // remplir la table map
    TreeMap<String,String> map = new TreeMap<>();
    NodeList nodesVariables = elem.getElementsByTagName("variable");
    //* modif, ajout de variables propvar=prop et d'une liste ordonnée de ces variables
    HashMap<String, String> freevars = new HashMap<>();
    ArrayList<Expression> listvars = new ArrayList<>();
    //*/
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
            for (String var : vars) {
                map.put(var, type);
                //* modif
                listvars.add(new Expression(var, syntax));
                //*/
            }
        }
    }
    nodesVariables = elem.getElementsByTagName("genrule");
    for (int i = 0; i < nodesVariables.getLength(); i++) {
      Element genRuleElement = (Element) nodesVariables.item(i);
      genItems.add(new GenItem(genRuleElement, syntax, map, freevars, listvars));
    }
    nodesVariables = elem.getElementsByTagName("discard");
    for (int i = 0; i < nodesVariables.getLength(); i++) {
      Element ifElement = (Element) nodesVariables.item(i);
      discards.add(new GenItem(ifElement, syntax, map, freevars, listvars));
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
