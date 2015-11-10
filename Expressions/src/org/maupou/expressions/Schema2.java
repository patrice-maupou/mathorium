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
public class Schema2 implements MutableTreeNode {
  
  private Expr pattern;
  private Object descr;
  protected Schema2 parent;
  private Schema2 next;
  protected boolean allowsChildren;
  protected final ArrayList<Schema2> schemas; // liste des descendants directs
  private final ArrayList<Expr> vars; // liste des variables
  protected HashMap<Expr, Expr> varMap; // pour les valeurs des variables
  protected int[] rgs; // rangs utilisés dans la liste exprNodes
  private boolean ready;
  
  public Schema2() {
    schemas = new ArrayList<>();
    vars = new ArrayList<>();
    varMap = new HashMap<>();
    parent = null;
    rgs = new int[0];
    ready = true;
  }
 
  
  public void setPattern(Element elem) throws Exception {
    String txt = elem.getTextContent();
    Expr p = null;
    try {
      p = Expr.scanExpr(txt);//new Expression(txt);
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
   
  public Expr getPattern() {
    return pattern;
  }

  public ArrayList<Schema2> getSchemas() {
    return schemas;
  }

  public ArrayList<Expr> getVars() {
    return vars;
  }

  public HashMap<Expr, Expr> getVarMap() {
    return varMap;
  }

  public int[] getRgs() {
    return rgs;
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

  public Schema2 getNext() {
    return next;
  }

  public void setNext(Schema2 next) {
    this.next = next;
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
  public Enumeration<Schema2> children() {
    return new TreeEnum2(schemas);
  }
  
  public  void add(MutableTreeNode child) {
    if(child != null && (child instanceof Schema2) && allowsChildren && !isAncestor(child)) {
      schemas.add((Schema2)child);
      child.setParent(this);
    }
  }
  
  @Override
  public void insert(MutableTreeNode child, int index) {
    if(child != null && (child instanceof Schema2) && allowsChildren && !isAncestor(child)) {
      schemas.add(index, (Schema2)child);
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
    Schema2 child = schemas.remove(index);
    child.setParent(null);
  }

  @Override
  public void remove(MutableTreeNode node) {
    if(node != null && node instanceof Schema2 && node.getParent() == this) { 
      Schema2 child = (Schema2)node;
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
    if(newParent instanceof Schema2) {
     parent = (Schema2)newParent;
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
    Schema2 p = (Schema2) getParent();
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
