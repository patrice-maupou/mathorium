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
  private final Pattern pattern;
  private int[] rgOfChild;

  /**
   * TODO : numéroter les groupes correspondant aux variables rgOfChild avec patternText
   * @param patternItem
   * @param childs
   * @param subtypes
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
    pattern = Pattern.compile(txt);
    try {
      rgOfChild = new int[childs.length];
      setChildGroups(patternText, childs);
    } catch (IndexOutOfBoundsException iob) {
      System.out.println("écriture incorrecte de " + patternText);
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
  
  private void setChildGroups(String text,  String[] childs) throws IndexOutOfBoundsException {
    int start = 0, np = 0;
    char pre = '_';
    for (int k = 0; k < rgOfChild.length; k++) {
      int idx = text.indexOf(childs[k]); // ex : "(\(a+b\))"
      String s = text.substring(start, idx);
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if(c == '(' && pre != '\\') np++;
        pre = c;
      }
      np++;
      rgOfChild[k] = np;
      start = idx + 1;
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


  public Pattern getPattern() {
    return pattern;
  }


  @Override
  public String toString() {
    String ret = "    name : " + name + "  pattern : " + patternText;
    ret = typeChecks.stream().map((typeCheck) -> "\n\t" + typeCheck).reduce(ret, String::concat);
    return ret;
  }
}
