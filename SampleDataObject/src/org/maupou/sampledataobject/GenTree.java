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

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.maupou.expressions.Schema;

/**
 *
 * @author Patrice
 */
public class GenTree extends JTree {

  

  public GenTree() {
    setRootVisible(true);
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setShowsRootHandles(true);
    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    renderer.setClosedIcon(null);
    renderer.setOpenIcon(null);
    renderer.setLeafIcon(null);
    setCellRenderer(renderer);
  }

  
  /**
   * 
   * @param value
   * @param selected
   * @param expanded
   * @param leaf
   * @param row
   * @param hasFocus
   * @return 
   */
  @Override
  public String convertValueToText(Object value, boolean selected, boolean expanded, 
          boolean leaf, int row, boolean hasFocus) {
    String ret = value.toString();
    if(value instanceof Schema) {
      ret = ((Schema)value).getDescr();
    }
    return ret;
  }
  
  public MutableTreeNode getRoot() {
    return (MutableTreeNode) getModel().getRoot();
  }


}
