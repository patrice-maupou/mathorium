package org.maupou.expressions;

import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */
public class Syntax {

  private ArrayList<SyntaxRule> rules;
  private SyntaxWrite syntaxWrite;
  private SyntaxRule atoms;
  private HashMap<String, Set<String>> subtypes;
  private ArrayList<Generator> generators;
  private ArrayList<MatchExpr> discards;
  private String unused;

  public Syntax(Document document) throws Exception {
    rules = new ArrayList<>();
    subtypes = new HashMap<>();
    Element syntax = document.getDocumentElement();
    unused = syntax.getAttribute("unused");
    if (unused.length() != 1) {
      throw new Exception("unused must be a one character string");
    }
    else {
      setRules(syntax);
    }
    NodeList writeList = syntax.getElementsByTagName("write");
    if (writeList.getLength() == 1) {
      Element write = (Element) writeList.item(0);
      syntaxWrite = new SyntaxWrite(write, unused);
    }
  }

  private void setRules(Element syntax) {
    NodeList readList = syntax.getElementsByTagName("read");
    if (readList.getLength() == 1) {
      Element read = (Element) readList.item(0);
      NodeList rulesList = read.getElementsByTagName("rule");
      for (int i = 0; i < rulesList.getLength(); i++) {
        Element rule = (Element) rulesList.item(i);
        SyntaxRule syntaxRule = new SyntaxRule(rule, subtypes, unused);
        syntaxRule.setId(rule.getAttribute("key"));
        if ("SIMPLES".equals(syntaxRule.getId())) {
          atoms = syntaxRule;
        }
        else {
          getRules().add(syntaxRule);
        }
      }
    }
  }

  /**
   * établit la liste des générateurs et celle des expressions à écarter
   * @param document
   * @throws Exception
   */
  public void addGenerators(Document document) throws Exception {
    generators = new ArrayList<>();
    NodeList list = document.getElementsByTagName("generators");
    for (int i = 0; i < list.getLength(); i++) {
      Element genElement = (Element) list.item(i);
      String name = genElement.getAttribute("name");
      generators.add(new Generator(name, genElement, this));
    }
  }

  public ArrayList<Generator> getGenerators() {
    return generators;
  }

  public SyntaxWrite getSyntaxWrite() {
    return syntaxWrite;
  }

  public ArrayList<MatchExpr> getDiscards() {
    return discards;
  }

  /**
   * liste des modèles de base sans descendants
   *
   * @return la liste
   */
  public SyntaxRule getAtoms() {
    return atoms;
  }

  /**
   * liste des règles de composition
   *
   * @return la liste
   */
  public ArrayList<SyntaxRule> getRules() {
    return rules;
  }

  /**
   * table associant à un type l'ensemble des types le vérifiant ex : integer=[integer, natural]
   *
   * @return la table
   */
  public HashMap<String, Set<String>> getSubtypes() {
    return subtypes;
  }

  /**
   * chaîne formée d'un seul charactère non utilisé dans les expressions et les modèles exemples : "
   *
   * @" "§"
   * @return la chaîne
   */
  public String getUnused() {
    return unused;
  }

  @Override
  public String toString() {
    String ret = "RULES :\n" + atoms + "\n";
    for (int i = 0; i < rules.size(); i++) {
      ret += rules.get(i) + "\n";
    }
    ret += "\nTYPES :\n";
    for (Iterator<Map.Entry<String, Set<String>>> it = subtypes.entrySet().iterator(); it.hasNext();) {
      Map.Entry<String, Set<String>> entry = it.next();
      ret += entry.toString() + "\n";
    }
    ret += "\nGENERATORS :\n";
    for (Generator generator : generators) {
      ret += generator + "\n";
    }
    return ret;
  }
}
