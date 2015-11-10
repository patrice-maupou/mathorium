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
import java.util.Objects;


public class CExpr extends Expr {
  
  
  public CExpr(Expr node, List<Expr> list, String type) {
    setNode(node);
    setList(list);
    setType(type);
  }

  @Override
  public boolean equals(Object obj) {
    boolean ret = obj instanceof CExpr;
    if(ret) {
      CExpr e = (CExpr) obj;
      ret = getNode().equals(e.getNode()) && getList().equals(e.getList());      
    }
    return ret; //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 23 * hash + Objects.hashCode(this.getNode());
    hash = 23 * hash + Objects.hashCode(this.getList());
    return hash;
  }
  
  
  
  @Override
  public Expr copy() {
    Expr nnode = (getNode() == null)? null : getNode().copy();
    List<Expr> nlist = new ArrayList<>();
    for (Expr e : getList()) {
      nlist.add(e.copy());
    }
    return new CExpr(nnode, nlist, getType());
  }

  @Override
  public Expr replace(HashMap<Expr, Expr> map) {
    Expr nnode = getNode().replace(map);
    List<Expr> nlist = new ArrayList<>();
    getList().stream().forEach((e) -> {
      nlist.add(e.replace(map));
    });
    return new CExpr(nnode, nlist, getType());
  }

    
  @Override
  public String toText() {
    String ret = "(" + getNode().toText() + ",";
    for (int i = 0; i < getList().size(); i++) {
      ret += getList().get(i).toText();
      ret += (i +1  == getList().size())? ")" : ",";
    }
    return ret;
  }
  
  @Override
  public String toString() {
    String ret = "(" + getNode().toString() + ",";
    for (int i = 0; i < getList().size(); i++) {
      ret += getList().get(i).toString();
      ret += (i +1  == getList().size())? ")" : ",";
    }
    return ret;
  }
  
  
  
}
