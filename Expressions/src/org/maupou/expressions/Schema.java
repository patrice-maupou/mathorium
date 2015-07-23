/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.w3c.dom.Element;

/**
 *
 * @author Patrice
 */
public class Schema implements MutableTreeNode {
  
  private Expression pattern;
  private Object descr;
  protected Schema parent;
  protected boolean allowsChildren;
  protected final ArrayList<Schema> schemas;
  private final ArrayList<Expression> vars; // liste des variables
  protected final HashMap<Expression, Expression> varMap; // pour les valeurs des variables
  private int[] rgs;
  private boolean ready;
  
  public Schema() {
    schemas = new ArrayList<>();
    vars = new ArrayList<>();
    varMap = new HashMap<>();
    parent = null;
    rgs = new int[0];
    ready = true;
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
  public void updateRgs(int range) {
    if (rgs.length != 0) {
      if (range != -1) {
        rgs[rgs.length - 1] = range;
      }
      schemas.stream().forEach((schema) -> {
        schema.rgs = Arrays.copyOf(rgs, schema.rgs.length);
      });
    }
  }

  public String getDescr() {
    return descr.toString();
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

  public int[] getRgs() {
    return rgs;
  }

  public void setRgs(int[] rgs) {
    this.rgs = rgs;
  }

  @Override
  public TreeNode getChildAt(int childIndex) {
    return schemas.get(childIndex);
  }

  @Override
  public int getChildCount() {
    return schemas.size();
  }

  @Override
  public TreeNode getParent() {
    return parent;
  }

  @Override
  public int getIndex(TreeNode node) {
    return schemas.indexOf(node);
  }

  @Override
  public boolean getAllowsChildren() {
    return allowsChildren;
  }

  @Override
  public boolean isLeaf() {
    return schemas.isEmpty();
  }

  @Override
  public Enumeration<Schema> children() {
    return new TreeEnum(schemas);
  }
  
  public  void add(MutableTreeNode child) {
    if(child != null && (child instanceof Schema) && allowsChildren && !isAncestor(child)) {
      schemas.add((Schema)child);
      child.setParent(this);
    }
  }
  
  @Override
  public void insert(MutableTreeNode child, int index) {
    if(child != null && (child instanceof Schema) && allowsChildren && !isAncestor(child)) {
      schemas.add(index, (Schema)child);
      child.setParent(this);
    }
  }
  
  private boolean isAncestor(TreeNode child) {
    if (child == null) {
      return false;
    }
    TreeNode current = this;
    while (current != null && current != child) {
      current = current.getParent();
    }
    return current == child;
  } 

  @Override
  public void remove(int index) {
    Schema child = schemas.remove(index);
    child.setParent(null);
  }

  @Override
  public void remove(MutableTreeNode node) {
    if(node != null && node instanceof Schema && node.getParent() == this) { 
      Schema child = (Schema)node;
      schemas.remove(child);
      child.setParent(null);
    }
  }
  
  @Override
  public void setUserObject(Object object) {
    descr = object;
  }

  @Override
  public void removeFromParent() {
    parent.remove(this);
    parent = null;
  }

  @Override
  public void setParent(MutableTreeNode newParent) {
    if(newParent instanceof Schema) {
     parent = (Schema)newParent;
    }
  }

  public boolean isReady() {
    return ready;
  }

  public void setReady(boolean ready) {
    this.ready = ready;
  }
  
  public String log() {
    String ret = getClass().getSimpleName().substring(0, 1);
    Schema p = (Schema) getParent();
    if(p != null) {
      int index = p.getSchemas().indexOf(this);
      ret += index + " [";
    }
    for (int i = 0; i < rgs.length; i++) {
      ret += (i==0)? rgs[i] : ","+rgs[i];
    }
    return ret + "]";
  }
 
}
