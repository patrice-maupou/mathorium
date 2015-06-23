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

  private final ArrayList<SyntaxRule> rules;
  private final HashMap<String, SyntaxWrite> syntaxWrites;
  private SyntaxWrite syntaxWrite;
  private SyntaxRule atoms;
  private final HashMap<String, Set<String>> subtypes;
  private ArrayList<Generator> generators;
  private ArrayList<MatchExpr> discards;
  private final String unused;
  private final String name;

  public Syntax(Document document) throws Exception {
    rules = new ArrayList<>();
    syntaxWrites = new HashMap<>();
    subtypes = new HashMap<>();
    Element syntax = document.getDocumentElement();
    unused = syntax.getAttribute("unused");
    if (unused.length() != 1) {
      throw new Exception("unused must be a one character string");
    } else {
      NodeList readList = syntax.getElementsByTagName("read");
      if (readList.getLength() == 1) {
        Element read = (Element) readList.item(0);
        name = read.getAttribute("name");
        NodeList rulesList = read.getElementsByTagName("rule");
        for (int i = 0; i < rulesList.getLength(); i++) {
          Element rule = (Element) rulesList.item(i);
          SyntaxRule syntaxRule = new SyntaxRule(rule, subtypes, unused);
          syntaxRule.setId(rule.getAttribute("key"));
          /* avant
          if ("SIMPLES".equals(syntaxRule.getId())) {
            atoms = syntaxRule;
          } else {
            rules.add(syntaxRule);
          }
          //*/
          //* modif (couplée avec parse)
          rules.add(syntaxRule);
          //*/
        }
      } else {
        name = null;
      }
      NodeList writeList = syntax.getElementsByTagName("write");
      for (int i = 0; i < writeList.getLength(); i++) {
        Element write = (Element) writeList.item(i);
        String wname = write.getAttribute("name");
        SyntaxWrite swr = new SyntaxWrite(write, unused);
        syntaxWrites.put(name, swr);
        if(wname.isEmpty() || wname.equals(name)) {
          syntaxWrite = swr;
        }
      }
    }
  }

  /**
   * établit la liste des générateurs et celle des expressions à écarter
   *
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

  public HashMap<String, SyntaxWrite> getSyntaxWrites() {
    return syntaxWrites;
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

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    String ret = "RULES :\n" + atoms + "\n";
    ret = rules.stream().map((rule) -> rule + "\n").reduce(ret, String::concat);
    ret += "\nTYPES :\n";
    for (Map.Entry<String, Set<String>> entry : subtypes.entrySet()) {
      ret += entry.toString() + "\n";
    }
    ret += "\nGENERATORS :\n";
    ret = generators.stream().map((generator) -> generator + "\n").reduce(ret, String::concat);
    return ret;
  }
}
