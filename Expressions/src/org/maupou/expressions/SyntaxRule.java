package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
    String after = ""; // pour les priorités
    for (int i = 0; i < e.getChildNodes().getLength(); i++) {
      Node childNode = e.getChildNodes().item(i);
      if(childNode.getNodeType() == Node.CDATA_SECTION_NODE) {
        after = childNode.getNodeValue();
        break;
      }
    }
    NodeList patternList = e.getElementsByTagName("pattern");
    int grcnt = 2;
    for (int i = 0; i < patternList.getLength(); i++) {
      if(i > 0) patternRuleTxt += "|";
      Element patternItem = (Element) patternList.item(i);
      String patternText = patternItem.getTextContent().trim();
      SyntaxPattern syntaxPattern = new SyntaxPattern(patternItem, childs, subtypes, unused);
      syntaxPatternGroups.put(grcnt, syntaxPattern);
      for (String child : childs) {
        patternText = patternText.replace(child, group);        
      }
      Pattern p = Pattern.compile(patternText);
      grcnt += p.matcher("").groupCount()+1;
      patternRuleTxt += "(" + patternText + ")";
    }    
    patternRuleTxt = "(" + patternRuleTxt + ")" + after; 
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
