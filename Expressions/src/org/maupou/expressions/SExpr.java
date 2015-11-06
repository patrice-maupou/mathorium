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

import java.util.HashMap;
import java.util.Objects;

/**
 *
 * @author Patrice
 */
public class SExpr extends Expr {
  
  private final String name;
  private final boolean symbol;

  /**
   *
   * @param name
   * @param type
   * @param symbol
   */
  public SExpr(String name, String type, boolean symbol) {
    this.name = name;
    setType(type);
    this.symbol = symbol;
  }

  /**
   *
   * @param obj
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    boolean ret = obj instanceof SExpr;
    if(ret) {
      SExpr e = (SExpr) obj;
      ret = name.equals(e.getName()) && getType().equals(e.getType());
    }
    return ret; //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + Objects.hashCode(this.name);
    hash = 97 * hash + (this.symbol ? 1 : 0);
    return hash;
  }
 
  

  @Override
  public Expr copy() {
    return new SExpr(name, getType(), symbol);
  }

  @Override
  public Expr replace(HashMap<Expr, Expr> map) {
    Expr e = map.get(this);
    return (e == null)? copy() : e;
  }

  @Override
  public String toText() {
    return name + ":" + getType();
  }  
  
  @Override
  public String toString() {
    return name;
  }

  public boolean isSymbol() {
    return symbol;
  }
  public String getName() {
    return name;
  }

  @Override
  public Expr getNode() {
    return this;
  }
  
  
}
