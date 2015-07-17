/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.w3c.dom.Element;

/**
 *
 * @author Patrice
 */
public class Schema {
  
  private Expression pattern;
  private final ArrayList<Schema> schemas;
  private final ArrayList<Expression> vars; // liste des variables
  protected final HashMap<Expression, Expression> varMap; // pour les valeurs des variables
  protected int[] enRgs;
  
  public Schema() {
    schemas = new ArrayList<>();
    vars = new ArrayList<>();
    varMap = new HashMap<>();
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
  
  /**
   * transmet les rangs des expressions utilisées précédemment
   * @param range le rang de l'ExprNode correspondant à ce modèle
   */
  public void updateEnRgs(int range) {
    enRgs[enRgs.length - 1] = range;
    schemas.stream().forEach((schema) -> {
      schema.enRgs = Arrays.copyOf(enRgs, schema.enRgs.length);
    });
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

  public HashMap<Expression, Expression> getVarMap() {
    return varMap;
  }

  public int[] getEnRgs() {
    return enRgs;
  }

 
}
