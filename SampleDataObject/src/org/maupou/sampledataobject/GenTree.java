/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
