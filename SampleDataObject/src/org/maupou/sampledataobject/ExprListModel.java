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
package org.maupou.sampledataobject;

import java.util.ArrayList;
import javax.swing.AbstractListModel;
/**
 *
 * @author Patrice
 * @param <ExprNode>
 */
public class ExprListModel<ExprNode> extends AbstractListModel<ExprNode> {
  
  private final ArrayList<ExprNode> exprList;

  public ExprListModel(ArrayList<ExprNode> exprList) {
    this.exprList = exprList;
  }
  
  @Override
  public int getSize() {
    return exprList.size();
  }

  @Override
  public ExprNode getElementAt(int index) {
    return exprList.get(index);
  }
  
  /**
   *
   * @param index le point d'insertion
   * @param en l'expression à insérer
   */
  public void add(int index, ExprNode en) {
    exprList.add(index, en);
    fireIntervalAdded(this, index, index);
  }
  
  public void add(ExprNode en) {
    int index = exprList.size();
    exprList.add(en);
    fireIntervalAdded(this, index, index);
  }
  
  public ExprNode remove(int index) {
    ExprNode result;
    result = exprList.remove(index);
    fireIntervalRemoved(this, index, index);
    return result;
  }
  
  public void removeRange(int startIndex, int endIndex) {
    int index;
    if (startIndex > endIndex)
      throw new IllegalArgumentException();
    for (index = endIndex; index >= startIndex; index--)
      exprList.remove(index);
    fireIntervalRemoved(this, startIndex, endIndex);
  }
  
  public void clear() {
    int s = exprList.size();
    if (s > 0) {
      exprList.clear();
      fireIntervalRemoved(this, 0, s - 1);
    }
  }
  
 
  public ArrayList<ExprNode> getExprList() {
    return exprList;
  }

  
}
