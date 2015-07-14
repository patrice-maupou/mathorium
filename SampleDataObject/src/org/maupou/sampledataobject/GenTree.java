/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.sampledataobject;

import java.util.ArrayList;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.maupou.expressions.MatchExpr;
import org.maupou.expressions.Schema;
import org.maupou.expressions.SyntaxWrite;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrice
 */
public class GenTree extends JTree {

  private DefaultMutableTreeNode root;
  private final DefaultTreeModel m;
  private SyntaxWrite sw;

  public GenTree() {
    root = new DefaultMutableTreeNode("genItem");
    sw = null;
    m = new DefaultTreeModel(root);
    setModel(m);
    setRootVisible(false);
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
   * @param schemas
   * @param node
   * @throws java.lang.Exception
   */
  public void setNode(ArrayList<? extends Schema> schemas, DefaultMutableTreeNode node)
          throws Exception {
    if (schemas != null) {
      for (Schema schema : schemas) {
        DefaultMutableTreeNode SchNode = new DefaultMutableTreeNode(schema);
        setNode(schema.getSchemas(), SchNode);
        node.add(SchNode);
      }
    }
  }

  public void setTree(ArrayList<? extends Schema> schemas) throws Exception {
    root.removeAllChildren();
    m.reload(root);
    setNode(schemas, root);
    expandPath(new TreePath(root));
  }

  @Override
  public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    String ret = value.toString();

    if (((DefaultMutableTreeNode) value).getUserObject() instanceof Schema) {
      Schema schema = (Schema) ((DefaultMutableTreeNode) value).getUserObject();
      try {
        ret = (schema instanceof MatchExpr) ? "modèle : " : "résultat : ";
        ret += schema.getPattern().toString(sw);
      } catch (Exception ex) {
        Exceptions.printStackTrace(ex);
      }
    }
    return ret;
  }

  public DefaultMutableTreeNode getRoot() {
    return root;
  }

  public void setRoot(DefaultMutableTreeNode root) {
    this.root = root;
  }

  public void setSw(SyntaxWrite sw) {
    this.sw = sw;
  }

}
