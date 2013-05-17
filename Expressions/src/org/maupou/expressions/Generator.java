/*   Generator.java
 * the code source is distributed under the GPL.
 * Please see http://www.fsf.org/copyleft/gpl.html
 */

package org.maupou.expressions;

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */



public class Generator {

  private String name;
  private ArrayList<GenItem> genItems;

  public Generator(String name, Element elem, Syntax syntax) throws Exception {
    this.name = name;
    genItems = new ArrayList<>();
    NodeList nl = elem.getElementsByTagName("genrule");
    for (int i = 0; i < nl.getLength(); i++) {
      Element ifElement = (Element) nl.item(i);
      genItems.add(new GenItem(ifElement, syntax));
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



}
