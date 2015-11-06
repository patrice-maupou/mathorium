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
  private final String name, version;

  public Syntax(Document document) throws Exception {
    rules = new ArrayList<>();
    syntaxWrites = new HashMap<>();
    subtypes = new HashMap<>();
    Element syntax = document.getDocumentElement();
    version = syntax.getAttribute("version");
    NodeList readList = syntax.getElementsByTagName("read");
    if (readList.getLength() == 1) {
      Element read = (Element) readList.item(0);
      name = read.getAttribute("name");
      NodeList rulesList = read.getElementsByTagName("rule");
      for (int i = 0; i < rulesList.getLength(); i++) {
        Element rule = (Element) rulesList.item(i);
        SyntaxRule syntaxRule = new SyntaxRule(rule, subtypes);
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
      SyntaxWrite swr = null;
      try {
        swr = new SyntaxWrite(write, subtypes, version);
      } catch (Exception ex) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for (StackTraceElement item : stackTrace) {
          System.out.println(item.toString());
        }
      }
      syntaxWrites.put(name, swr);
      if (wname.isEmpty() || wname.equals(name)) {
        syntaxWrite = swr;
      }
    }
  }

  /**
   * 
   * @param text la chaîne à analyser
   * @return l'expression
   */
  //*
  public Expr parseExpr(String text) {
    StringBuilder buf = new StringBuilder();
    while (buf.length() < text.length()) {
      buf.append("____________________________________");
    }
    String tokenvar = buf.insert(0, "\u0000").toString();
    String tkvar = "\u0000_*"; // remplace les parties décodées
    HashMap<Integer, Expr> pos = new HashMap<>();
    boolean haschanged;
    do {
      haschanged = false;
      loop_rules:
      for (SyntaxRule rule : rules) {
        String[] childs = rule.getChilds();
        Matcher m = rule.getPatternRule().matcher(text);
        loop_find:
        while (m.find()) {
          ArrayList<Expr> list = new ArrayList<>(), nlist = new ArrayList<>(); 
          TypeCheck typeCheck = null;
          int i, idx;
          for (Integer key : rule.getSyntaxPatternGroups().keySet()) {
            if (m.group(key) != null) { // c'est le pattern qui convient
              idx = key; // c'est le premier groupe
              SyntaxPattern syntaxPattern = rule.getSyntaxPatternGroups().get(key);
              if (childs.length == 0) { // pattern simple 
                String type = (syntaxPattern.getTypeChecks().isEmpty())? null : 
                        syntaxPattern.getTypeChecks().get(0).getType();
                  list.add(new SExpr(m.group(), type, false));
              } else { // pattern composite, liste des enfants
                Expr e = syntaxPattern.getNode();
                int cor = 0;
                if(e != null) {
                  list.add(e);
                  cor = 1;
                }
                for (int j = idx + 1; j <= m.groupCount(); j++) {
                  if (m.group(j) != null && list.size()< childs.length + cor) {
                    list.add(pos.get(m.start(j)));
                  }
                }
                // vérification des types des enfants
                for (TypeCheck typChck : syntaxPattern.getTypeChecks()) {
                  if(typChck.check(childs, list, cor, subtypes)) {
                    typeCheck = typChck;
                    for (i = 0; i < childs.length; i++) { // suppression des enfants dans pos
                      pos.remove(m.start(idx + i + 1));
                    }
                    break;
                  }
                }
              } // fin liste
              for (Expr ch : list) {
                if (ch instanceof CExpr && ((CExpr) ch).getNode().equals(syntaxPattern.getFlatten())) {
                  nlist.addAll(((CExpr) ch).getList());
                } else {
                  nlist.add(ch);
                }
              }
              // changement de text et expression dans pos
              String type = (typeCheck == null)? null : typeCheck.getType();
              Expr e = Expr.listToExpr(nlist, type);
              pos.put(m.start(), e);
              text = text.substring(0, m.start()) + tokenvar.substring(0, m.end() - m.start())
                      + text.substring(m.end());
              m.reset(text);
              if (text.matches(tkvar)) {
                return e;
              }
              haschanged = true;
              break loop_rules;
            } // fin du pattern qui convient
          } // loop_pattern
        } // loop_find     // loop_find    
      } // loop_rules // loop_rules
    } while (haschanged);
    return null;
  }
  //*/

  /**
   * analyse le texte et retourne une expression ou null en cas d'erreur
   *
   * @param text
   * @return l'expression
   */
  public Expression parse(String text) {
    StringBuilder buf = new StringBuilder();
    while (buf.length() < text.length()) {
      buf.append("____________________________________");
    }
    String tokenvar = buf.insert(0, "\u0000").toString();
    String tkvar = "\u0000_*"; // remplace les parties décodées
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
            if (childs.length > 0 && nodeName.equals(childs[0])) { // cas général
              if (ch != null && ch.size() > 1) {
                ch.remove(0);
                e = new Expression(nodeName, typeCheck.getType(), ch, false);
              } else {
                e = tm.get(m.start(idx + 1)); // cas de la parenthèse
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
      } // end loop_rules
    } while (haschanged);
    return null;
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

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    String ret = "RULES :\n";
    ret = rules.stream().map((rule) -> rule + "\n").reduce(ret, String::concat);
    ret += "\nTYPES :\n";
    ret = subtypes.entrySet().stream().map((entry) -> entry.toString() + "\n").reduce(ret, String::concat);
    return ret;
  }
}
