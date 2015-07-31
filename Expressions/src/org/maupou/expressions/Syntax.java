/* 
 * Copyright (C) 2015 Patrice.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.maupou.expressions;

import java.util.*;
import java.util.regex.Matcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice Maupou
 */
public class Syntax {

  private final ArrayList<SyntaxRule> rules;
  private final HashMap<String, SyntaxWrite> syntaxWrites; // TODO : option future
  private SyntaxWrite syntaxWrite;
  private final HashMap<String, Set<String>> subtypes;
  private ArrayList<Generator> generators;
  private final String unused;
  private final String name;

  public Syntax(Document document) throws Exception {
    rules = new ArrayList<>();
    syntaxWrites = new HashMap<>();
    subtypes = new HashMap<>();
    Element syntax = document.getDocumentElement();
    unused = (syntax.hasAttribute("unused"))? syntax.getAttribute("unused") : "\u0000";
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
          rules.add(syntaxRule);
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
   * analyse le texte et retourne une expression ou null en cas d'erreur
   *
   * @param text
   * @return
   */
  public Expression parse(String text) {
    String tokenvar = "____________________________________";
    StringBuilder buf = new StringBuilder();
    while (buf.length() < text.length()) {
      buf.append(tokenvar);
    }
    buf.insert(0, unused);
    tokenvar = buf.toString();
    String tkvar = unused + "_*"; // remplace les parties décodées
    TreeMap<Integer, Expression> tm = new TreeMap<>();
    boolean haschanged;
    do {
      haschanged = false;
      loop_rules:
      for (SyntaxRule rule : rules) {
        String[] childs = rule.getChilds();
        Matcher m = rule.getPatternRule().matcher(text);
        loop_find:
        while (m.find()) {
          SyntaxPattern syntaxPattern = null;
          ArrayList<Expression> ch = new ArrayList<>();
          TypeCheck typeCheck;
          int i, idx = 0;
          for (Integer key : rule.getSyntaxPatternGroups().keySet()) {
            if (m.group(key) != null) { // c'est le pattern qui convient
              idx = key; // c'est le premier groupe
              syntaxPattern = rule.getSyntaxPatternGroups().get(key);
              break;
            }
          }
          if (syntaxPattern == null) {
            break;
          }
          String nodeName = syntaxPattern.getName();
          if (nodeName.isEmpty()) { // atomes ou expressions simples
            nodeName = m.group();
            ch = null;
            typeCheck = syntaxPattern.getTypeChecks().get(0);
          } else {
            // liste des enfants
            for (int j = idx + 1; j <= m.groupCount(); j++) {
              if (m.group(j) != null && ch.size() < childs.length) {
                ch.add(tm.get(m.start(j)));
              }
            }
            // vérification des types des enfants
            typeCheck = null;
            for (TypeCheck typChck : syntaxPattern.getTypeChecks()) {
              for (i = 0; i < childs.length; i++) {
                String childType = typChck.getChildtypes().get(childs[i]);
                if (!subtypes.get(childType).contains(ch.get(i).getType())) {
                  break;
                }
              }
              if (i == childs.length) { // terminé
                typeCheck = typChck;
                break;
              }
            }
          }
          // suppression des enfants dans tm
          if (typeCheck != null) {
            Expression e = new Expression(nodeName, typeCheck.getType(), ch, false);
            if (childs.length > 0 && nodeName.equals(childs[0])) {
              if(ch != null && ch.size() > 1) {
                ch.remove(0);
                e = new Expression(nodeName, typeCheck.getType(), ch, false);
              } else {
               e = tm.get(m.start(idx + 1));
              }
            }
            for (i = 0; i < childs.length; i++) {
              tm.remove(m.start(idx + i + 1));
            }
            // changement de text et expression dans tm
            tm.put(m.start(), e);
            text = text.substring(0, m.start()) + tokenvar.substring(0, m.end() - m.start())
                    + text.substring(m.end());
            m.reset(text);
            if (text.matches(tkvar)) {
              return e;
            }
            haschanged = true;
            break loop_rules;
          }
        } // end loop_find // end loop_find
      } // end rules // end rules
    } while (haschanged);
    return null;
  }
  
  /**
   * établit la liste des générateurs et celle des expressions à écarter
   *
   * @param document
   * @throws Exception
   */
  public void addGenerators(Document document) throws Exception {
    generators = new ArrayList<>();
    NodeList list = document.getElementsByTagName("generator");
    for (int i = 0; i < list.getLength(); i++) {
      Element genElement = (Element) list.item(i);
      generators.add(new Generator(genElement.getAttribute("name"), genElement, this));
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
    String ret = "RULES :\n";
    ret = rules.stream().map((rule) -> rule + "\n").reduce(ret, String::concat);
    ret += "\nTYPES :\n";
    ret = subtypes.entrySet().stream().map((entry) -> entry.toString() + "\n").reduce(ret, String::concat);
    ret += "\nGENERATORS :\n";
    ret = generators.stream().map((generator) -> generator + "\n").reduce(ret, String::concat);
    return ret;
  }
}
