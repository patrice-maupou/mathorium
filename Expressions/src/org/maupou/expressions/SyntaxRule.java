package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */
public class SyntaxRule {

  private final TreeMap<Integer, SyntaxPattern> syntaxPatternGroups;  // rang du groupe -> modèle du groupe
  private String id;              // nom du modèle et nom du noeud de l'expression
  private final String[] childs;  // noms des variables représentant les enfants
  private final Pattern patternRule;

  /**
   * Crée une règle de syntaxe
   * 
   * @param e
   * @param subtypes
   * @param unused
   */
  public SyntaxRule(Element e, HashMap<String, Set<String>> subtypes, String unused) {
    id = e.getAttribute("key");
    String childText = e.getAttribute("child");
    String group = "(" + unused + "_*)"; 
    childs = (childText.isEmpty()) ? new String[0] : childText.split(",");
    syntaxPatternGroups = new TreeMap<>();
    String patternRuleTxt = "";
    NodeList patternList = e.getElementsByTagName("pattern");
    int cnt = 1;
    for (int i = 0; i < patternList.getLength(); i++) {
      if(i > 0) patternRuleTxt += "|";
      Element patternItem = (Element) patternList.item(i);
      String patternText = patternItem.getTextContent().trim();
      SyntaxPattern syntaxPattern = new SyntaxPattern(patternItem, childs, subtypes, unused);
      syntaxPatternGroups.put(cnt, syntaxPattern);
      for (String child : childs) {
        int start = 0, next;
        while ((next = patternText.indexOf(child, start)+1) != 0) { // groupe de child
          cnt++;
          start = next;
        }
        patternText = patternText.replace(child, group);        
      }
      cnt += 1;
      if((patternText.contains("(?!(("))) {cnt += 2;}
      else if((patternText.contains("(?!("))) {cnt += 1;}
      patternRuleTxt += "(" + patternText + ")";
    }    
    patternRule = Pattern.compile(patternRuleTxt.trim());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String[] getChilds() {
    return childs;
  }

  public Pattern getPatternRule() {
    return patternRule;
  }

  public TreeMap<Integer, SyntaxPattern> getSyntaxPatternGroups() {
    return syntaxPatternGroups;
  }


  @Override
  public String toString() {
    String ret = id + "\n";
    ret += "pattern : " + getPatternRule() + "\n";
    for (Integer key : syntaxPatternGroups.keySet()) {
      ret += syntaxPatternGroups.get(key) + "\n";
    }
    return ret;
  }
}
