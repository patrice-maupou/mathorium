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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Patrice
 */
public abstract class Expr {

  private String type;
  private Expr node;
  private List<Expr> list;

  /**
   * construit une expression à partir d'une liste d'expressions
   * @param list
   * @param type
   * @return null si size = 0, l'expression de la liste si size = 1, l'expr de node l'expression de rang 0
   * et les autres formant la liste
   */
  public static Expr listToExpr(List<Expr> list, String type) {
    Expr ret = null;
    if(list.size() > 1) {
      ret = new CExpr(list.get(0), list.subList(1, list.size()), type);
    }
    else if(list.size() == 1) {
      ret = list.get(0);
    }
    return ret;
  }  
  
  
  /**
   * modif
   * @param text
   * @return 
   */
  public static Expr scanExpr(String text) {
    StringBuilder buf = new StringBuilder();
    while (buf.length() < text.length()) {
      buf.append("____________________________________");
    }
    String tokenvar = buf.insert(0, "\u0000").toString();
    String tkvar = "\u0000_*"; // remplace les parties décodées
    Pattern[] patterns = new Pattern[2];
    patterns[0] = Pattern.compile("([^\\(\\),:]+):(\\w+)");
    patterns[1] = Pattern.compile("\\((\u0000_*(,\u0000_*)+)\\):(\\w+)"); // group(3) est le type
    HashMap<Integer, Expr> pos = new HashMap<>();
      for (int i = 0; i < patterns.length; i++) {
        Matcher m = patterns[i].matcher(text);
        while (m.find()) {
          Expr e;
          if (i == 0) { // simple
            e = new SExpr(m.group(1), m.group(2), false);
          } else {      // composite
            List<Expr> list = new ArrayList<>();   
            String type = m.group(3);
            int index = 0;
            do { // liste des expressions
              list.add(pos.remove(m.start(1) + index));
              index = m.group(1).indexOf(',', index) + 1;
            } while (index > 0);
            e = listToExpr(list, type);
          }
          pos.put(m.start(), e);
          text = text.substring(0, m.start()) + tokenvar.substring(0, m.end() - m.start())
                      + text.substring(m.end());
          m.reset(text);
          if (text.matches(tkvar)) {
            return e;            
          }
        }
      }
    return null;
  }
  

  public abstract Expr copy();

  public abstract Expr replace(HashMap<Expr, Expr> map);
  
  public abstract String toText();

  public Expr getNode() {
    return node;
  }
  
  public void setNode(Expr node) {
    this.node = node;
  }

  public List<Expr> getList() {
    return list;
  }

  public void setList(List<Expr> list) {
    this.list = list;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


}
