/*   Generator.java
 * the code source is distributed under the GPL.
 * Please see http://www.fsf.org/copyleft/gpl.html
 */

package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */



public class Generator {

  private String name;
  private ArrayList<GenItem> genItems;
  private ArrayList<GenItem> discards;

  public Generator(String name, Element elem, Syntax syntax) throws Exception {
    this.name = name;
    genItems = new ArrayList<>();
    discards = new ArrayList<>();
    // remplir la table map
    TreeMap<String,String> map = new TreeMap<>();
    NodeList nrl = elem.getElementsByTagName("variable");
    for (int i = 0; i < nrl.getLength(); i++) {
        Element lv = (Element) nrl.item(i);
        String vname = lv.getAttribute("name"); // type de la variable
        String type = lv.getAttribute("type"); // le type représenté
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
            for (int j = 0; j < vars.length; j++) {
                map.put(vars[j], type);
            }
        }
    }
    nrl = elem.getElementsByTagName("genrule");
    for (int i = 0; i < nrl.getLength(); i++) {
      Element ifElement = (Element) nrl.item(i);
      genItems.add(new GenItem(ifElement, syntax, map));
    }
    nrl = elem.getElementsByTagName("discard");
    for (int i = 0; i < nrl.getLength(); i++) {
      Element ifElement = (Element) nrl.item(i);
      discards.add(new GenItem(ifElement, syntax, map));
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
