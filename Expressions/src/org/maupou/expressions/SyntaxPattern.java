package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */
public class SyntaxPattern {

  private final ArrayList<TypeCheck> typeChecks;
  private final String name;
  private final String patternText;

  /**
   * Crée un élément de syntaxe
   * @param patternItem élément du document
   * @param childs les variables utilisées
   * @param subtypes map qui associe à un type ses sous-types
   * @param unused
   */
  public SyntaxPattern(Element patternItem, String[] childs, 
          HashMap<String, Set<String>> subtypes,
          String unused) {
    name = patternItem.getAttribute("node");
    // types admissibles pour ce modèle
    typeChecks = new ArrayList<>();
    patternText = patternItem.getTextContent().trim();
    String txt = patternText;
    for (String child : childs) {
      txt = txt.replace(child, "(" + unused + "_*)");
    }
    NodeList typeList = patternItem.getElementsByTagName("type");
    for (int i = 0; i < typeList.getLength(); i++) {
      Element typeItem = (Element) typeList.item(i);
      String type = TypeCheck.addSubTypes(typeItem, subtypes);
      String[] typeValues = typeItem.getAttribute("value").split(",");
      if (type.equals("inherit") && typeValues.length == 1) {
        String[] typeOptions = typeValues[0].split("\\|");
        for (String typeOption : typeOptions) {
          typeValues = new String[]{typeOption};
          typeChecks.add(new TypeCheck(typeOption, childs, typeValues));
        }
      }
      else {
        typeChecks.add(new TypeCheck(type, childs, typeValues));
      }
    }
  }
  

  /**
   * typeCheck contient un des types du modèle et la correspondance child -> type
   * @return la liste des typeChecks de ce modèle
   */
  public ArrayList<TypeCheck> getTypeChecks() {
    return typeChecks;
  }

  public String getName() {
    return name;
  }


  @Override
  public String toString() {
    String ret = "    name : " + name + "  pattern : " + patternText;
    ret = typeChecks.stream().map((typeCheck) -> "\n\t" + typeCheck).reduce(ret, String::concat);
    return ret;
  }
}
