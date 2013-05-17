/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Patrice Maupou
 */
public class ExprNode {

  private Expression e;
  private ArrayList<Integer> childList;
  private ArrayList<int[]> parentList;
  private ArrayList<ExprNode> exprs;

  /**
   *
   * @param e
   * @param childList
   * @param parentList
   * @param exprs
   */
  public ExprNode(Expression e, ArrayList<Integer> childList, ArrayList<int[]> parentList,
          ArrayList<ExprNode> exprs) {
    this.e = e;
    this.childList = childList;
    this.parentList = parentList;
    this.exprs = exprs;
  }

  public ExprNode copy() {
    ArrayList<int[]> parents = new ArrayList<>();
    for (int[] p : parentList) {
      parents.add(Arrays.copyOf(p, p.length));
    }
    ArrayList<Integer> childs = new ArrayList<>();
    for (Integer integer : childList) {
      childs.add(integer);
    }
    Expression expr = (e == null)? e : e.copy();
    ExprNode en = new ExprNode(expr, childs, parents, exprs);
    return en;
  }

  @Override
  public boolean equals(Object obj) {
    boolean ret = false;
    if(e!= null & obj instanceof ExprNode) {
      ExprNode en = (ExprNode) obj;
      ret = e.equals(en.getE());
    }
    return ret;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 61 * hash + Objects.hashCode(this.e);
    return hash;
  }

  @Override
  public String toString() {
    return e.toString();
  }

  public Expression getE() {
    return e;
  }

  public void setE(Expression e) {
    this.e = e;
  }

  public ArrayList<Integer> getChildList() {
    return childList;
  }

  public void setChildList(ArrayList<Integer> childList) {
    this.childList = childList;
  }
  public ArrayList<int[]> getParentList() {
    return parentList;
  }
  public void setParentList(ArrayList<int[]> parentList) {
    this.parentList = parentList;
  }

  public ArrayList<ExprNode> getExprs() {
    return exprs;
  }



}
