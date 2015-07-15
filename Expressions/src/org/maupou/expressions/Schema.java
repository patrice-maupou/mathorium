/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Element;

/**
 *
 * @author Patrice
 */
public class Schema {
  
  private Expression pattern;
  private final ArrayList<Schema> schemas;
  private final ArrayList<Expression> vars;
  
  public Schema() {
    schemas = new ArrayList<>();
    vars = new ArrayList<>();
  }
  
  public void setPattern(Element elem) throws Exception {
    String txt = elem.getTextContent();
    Expression p = null;
    try {
      p = new Expression(txt);
      if(elem.hasAttribute("name")) {
        p.setType(elem.getAttribute("name"));
      }
    } catch (Exception ex) {
      throw new Exception("modèle incorrect :\n" + ex.getMessage());
    }
    pattern = p;
  }
   
  public Expression getPattern() {
    return pattern;
  }

  public ArrayList<Schema> getSchemas() {
    return schemas;
  }

  public ArrayList<Expression> getVars() {
    return vars;
  }

 
}
