package org.maupou.expressions;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */
public class SyntaxRule {

  private TreeMap<Integer, SyntaxPattern> syntaxPatternGroups;
  // rang du group -> syntaxPattern
  private String id;
  // nom du modèle et nom du noeud de l'expression
  private String[] childs;
  // chaque pattern correspondant à une syntaxPattern2 correspond à un groupe de patternRule
  private Pattern patternRule;

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
    String group = e.getAttribute("group");
    if (group.isEmpty()) {
      group = "(.+)";
    }
    childs = (childText.isEmpty()) ? new String[0] : childText.split(",");
    syntaxPatternGroups = new TreeMap<>();
    String patternRuleText = e.getTextContent().trim();
    patternRuleText = "(" + patternRuleText.replaceAll("[\\s]+", ")|(") + ")";
    for (int i = 0; i < childs.length; i++) {
      patternRuleText = patternRuleText.replace(childs[i], group);
    }
    patternRule = Pattern.compile(patternRuleText.trim());
    NodeList patternList = e.getElementsByTagName("pattern");
    int cnt = 1;
    for (int i = 0; i < patternList.getLength(); i++) {
      Element patternItem = (Element) patternList.item(i);
      String patternText = patternItem.getTextContent().trim();
      SyntaxPattern syntaxPattern = new SyntaxPattern(patternItem, childs, subtypes, unused);
      syntaxPatternGroups.put(cnt, syntaxPattern);
      if (patternText.indexOf("?<" + syntaxPattern.getName() + ">") != -1) { // variable
        cnt += 1;
      }
      for (int j = 0; j < childs.length; j++) {
        String child = childs[j];
        int start = 0;
        while ((start = patternText.indexOf(child, start)+1) != 0) { // groupe de child
          cnt += 1;
        }
      }
      cnt += 1;
    }
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
