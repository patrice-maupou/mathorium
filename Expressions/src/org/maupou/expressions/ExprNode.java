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

   
}
