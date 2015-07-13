package org.maupou.expressions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Element;

/**
 * établit un lien entre un type et ceux d'une liste de noms de variables
 * @author Patrice Maupou
 */
public class TypeCheck {

  private final String type;
  private final HashMap<String, String> childtypes; // map : variable -> type

  /**
   *
   * @param typeItem
   * @param childNames liste des noms des enfants
   */
  TypeCheck(String type, String[] childNames, String[] typeValues) {
    this.type = type;
    int n = typeValues.length;
    childtypes = new HashMap<>();
    for (int i = 0; i < childNames.length; i++) {
      String childtype = (i < n) ? typeValues[i] : typeValues[n - 1];
      childtypes.put(childNames[i], childtype);
    }
  }

  /**
   * add subTypes to type defined by an Element.
   * @param typeItem
   * @param subtypes
   * @return
   */
  public static String addSubTypes(Element typeItem, HashMap<String, Set<String>> subtypes) {
    String type = typeItem.getAttribute("name");
    if ("inherit".equals(type) || type.isEmpty()) {
      return type;
    }
    Set<String> eset = subtypes.get(type);
    if (eset == null) {
      eset = new HashSet<>(20);
      eset.add(type);
      subtypes.put(type, eset);
    }
    if (typeItem.hasAttribute("subtypes")) {
      String[] typesArray = typeItem.getAttribute("subtypes").split(",");
      for (int i = 0; i < typesArray.length; i++) {
        String subtype = typesArray[i];
        eset.add(subtype);
        if(subtypes.get(subtype) != null) {
          eset.addAll(subtypes.get(subtype));
        }
      }
    }
    return type;
  }

  public String getType() {
    return type;
  }

  public HashMap<String, String> getChildtypes() {
    return childtypes;
  }

  @Override
  public String toString() {
    String ret = type + "  <-  ";
    for (Map.Entry<String, String> entry : childtypes.entrySet()) {
      ret += entry.getKey() + ":" + entry.getValue() + "  ";
    }
    return ret;
  }
}
