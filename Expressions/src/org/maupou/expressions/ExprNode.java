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
    private int range; // à utiliser dans une liste d'ExprNode
    private String comment;
    private ArrayList<Integer> childList;
    private ArrayList<int[]> parentList;
    private final ArrayList<Expression> exprs;

    /**
     *
     * @param e
     * @param childList
     * @param parentList
     */
    public ExprNode(Expression e, ArrayList<Integer> childList, ArrayList<int[]> parentList) {
        this.e = e;
        this.childList = (childList == null)? new ArrayList<>() : childList;
        this.parentList = (parentList == null)? new ArrayList<>() : parentList;
        exprs = new ArrayList<>();
        comment = "";
    }

    

    public ExprNode copy() {
        ArrayList<int[]> parents = new ArrayList<>();
        parentList.stream().forEach((p) -> {
          parents.add(Arrays.copyOf(p, p.length));
      });
        ArrayList<Integer> childs = new ArrayList<>();
        childList.stream().forEach((integer) -> {
          childs.add(integer);
      });
        Expression expr = (e == null) ? e : e.copy();
        ExprNode en = new ExprNode(expr, childs, parents);
        en.setRange(range);
        return en;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (e != null & obj instanceof ExprNode) {
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

  public int getRange() {
    return range;
  }

  public void setRange(int range) {
    this.range = range;
  }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public ArrayList<Expression> getExprs() {
        return exprs;
    }

   
}
