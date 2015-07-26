/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
  
  private ArrayList<ExprNode> exprList;

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
