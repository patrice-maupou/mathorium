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
    setText(sw.toString(e));
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