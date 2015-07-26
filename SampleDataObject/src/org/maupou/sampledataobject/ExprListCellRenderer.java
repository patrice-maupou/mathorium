/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.sampledataobject;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.maupou.expressions.ExprNode;
import org.maupou.expressions.Expression;
import org.maupou.expressions.SyntaxWrite;

/**
 *
 * @author Patrice
 */
public class ExprListCellRenderer extends JLabel implements ListCellRenderer {
  
  private final SyntaxWrite sw;

  public ExprListCellRenderer(SyntaxWrite sw) {
    this.sw = sw;
  } 

  /**
   *
   * @param list
   * @param value 
   * @param index
   * @param isSelected
   * @param cellHasFocus
   * @return
   */
  @Override
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, 
          boolean cellHasFocus) {
    ExprNode en = (ExprNode) value;
    Expression e = en.getE();
    try {
      setText(e.toString(sw));
    } catch (Exception ex) {
      setText(e.toString());
    }
    setOpaque(true);
    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }
    return this;
  }

}