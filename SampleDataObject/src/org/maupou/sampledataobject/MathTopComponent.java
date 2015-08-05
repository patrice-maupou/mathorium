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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.maupou.expressions.ExprNode;
import org.maupou.expressions.Expression;
import org.maupou.expressions.GenItem;
import org.maupou.expressions.Generator;
import org.maupou.expressions.MatchExpr;
import org.maupou.expressions.Result;
import org.maupou.expressions.Schema;
import org.maupou.expressions.Syntax;
import org.maupou.expressions.SyntaxWrite;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import static org.netbeans.core.spi.multiview.MultiViewFactory.createUnsafeCloseState;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.UndoRedo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.maupou.sampledataobject//Math//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MathTopComponent",
        iconBase = "org/maupou/sampledataobject/edit-mathematics.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "org.maupou.sampledataobject.MathTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
/*
 @TopComponent.OpenActionRegistration(
 displayName = "#CTL_MathAction",
 preferredID = "MathTopComponent"
 )
 //*/
@Messages({
  "CTL_MathAction=Math",
  "CTL_MathTopComponent=Math Window",
  "HINT_MathTopComponent=This is a Math window"
})
public final class MathTopComponent extends JPanel implements MultiViewElement {

  private MathDataObject mdo;
  private ExprNode toAdd;
  private ArrayList<ExprNode> exprNodes;
  private Syntax syntax;
  private SyntaxWrite syntaxWrite;
  private ArrayList<Generator> generators;
  private Generator generator;
  private boolean resultReady;
  private int level;
  private MultiViewElementCallback callback;
  private final JToolBar toolbar = new JToolBar();
  private static RequestProcessor RP;
  private Schema curSchema;
  private ExprListModel listModel;

  public MathTopComponent() {
    exprNodes = new ArrayList<>();
    initComponents();
    setName(Bundle.CTL_MathTopComponent());
    setToolTipText(Bundle.HINT_MathTopComponent());
    toAdd = null;
    RP = new RequestProcessor("Generation of expressions", 1, true);
  }

  public MathTopComponent(MathDataObject mdo) {
    this();
    this.mdo = mdo;
    try {
      syntax = mdo.setSyntax();
      syntaxWrite = syntax.getSyntaxWrite();
      exprList.setCellRenderer(new ExprListCellRenderer(syntaxWrite));
      listModel = (ExprListModel) exprList.getModel();
      exprNodes = listModel.getExprList();
      generators = mdo.setGenerators(syntax);
      ArrayList<String> names = new ArrayList<>();
      generators.stream().forEach((gen) -> {
        names.add(gen.getName());
      });
      generatorBox.setModel(new DefaultComboBoxModel<>(names.toArray()));
      if (!generators.isEmpty()) {
        generator = generators.get(0);
        updateGenerator(generator);
      }
    } catch (Exception ex) {
      displayMessage("No syntax file available :\n" + ex.getMessage(), "Error message");
    }
  }

  /**
   * met à jour le generator et les expNodes correspondant
   *
   * @param generator
   * @throws Exception
   */
  private void updateGenerator(Generator generator) throws Exception {
    ArrayList<Schema> genItems = generator.getSchemas();
    String[] itemStrings = new String[genItems.size()];
    for (int i = 0; i < itemStrings.length; i++) {
      itemStrings[i] = genItems.get(i).toString();
    }
    try {
      mdo.readExprNodes(generator, listModel);
    } catch (Exception ex) {
      displayMessage("Erreur de lecture des expressions " + ex.getMessage(), "Error message");
    }
    resultTextField.setText("");
    DefaultTreeModel treeModel = (DefaultTreeModel) genTree.getModel();
    treeModel.setRoot(generator);
    treeModel.reload();
    genTree.expandPath(new TreePath(generator));
    valueTextField.setText("Sélectionner un modèle ou un résultat");
    resultReady = false;
    curSchema = null;
  }


  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    commentScroll = new javax.swing.JScrollPane();
    commentArea = new javax.swing.JTextArea();
    commentLabel = new javax.swing.JLabel();
    generatorLabel = new javax.swing.JLabel();
    generatorBox = new javax.swing.JComboBox();
    varTableScroll = new javax.swing.JScrollPane();
    varsTable = new javax.swing.JTable();
    hintLabel = new javax.swing.JLabel();
    valueTextField = new javax.swing.JTextField();
    resultLabel = new javax.swing.JLabel();
    resultTextField = new javax.swing.JTextField();
    valButton = new javax.swing.JButton();
    levelLabel = new javax.swing.JLabel();
    levelSpinner = new javax.swing.JSpinner();
    autoButton = new javax.swing.JButton();
    commentButton = new javax.swing.JButton();
    deleteButton = new javax.swing.JButton();
    listScrollPane = new javax.swing.JScrollPane();
    exprList = new javax.swing.JList();
    treeScrollPane = new javax.swing.JScrollPane();
    genTree = new org.maupou.sampledataobject.GenTree();

    commentArea.setColumns(20);
    commentArea.setRows(5);
    commentScroll.setViewportView(commentArea);

    org.openide.awt.Mnemonics.setLocalizedText(commentLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.commentLabel.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(generatorLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.generatorLabel.text")); // NOI18N

    varsTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {
        {null, null, null},
        {null, null, null},
        {null, null, null},
        {null, null, null},
        {null, null, null},
        {null, null, null},
        {null, null, null},
        {null, null, null}
      },
      new String [] {
        "Variable", "Valeur", "Type"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.String.class, java.lang.String.class, java.lang.String.class
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }
    });
    varTableScroll.setViewportView(varsTable);

    org.openide.awt.Mnemonics.setLocalizedText(hintLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.hintLabel.text")); // NOI18N

    valueTextField.setText(org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.valueTextField.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(resultLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.resultLabel.text")); // NOI18N

    resultTextField.setText(org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.resultTextField.text")); // NOI18N
    resultTextField.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        resultTextFieldActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(valButton, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.valButton.text")); // NOI18N
    valButton.setToolTipText(org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.valButton.toolTipText")); // NOI18N
    valButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        valButtonActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(levelLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.levelLabel.text")); // NOI18N

    levelSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), null, null, Integer.valueOf(1)));
    levelSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        levelSpinnerStateChanged(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(autoButton, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.autoButton.text")); // NOI18N
    autoButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        autoButtonActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(commentButton, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.commentButton.text")); // NOI18N
    commentButton.setToolTipText(org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.commentButton.toolTipText")); // NOI18N
    commentButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        commentButtonActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.deleteButton.text")); // NOI18N
    deleteButton.setToolTipText(org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.deleteButton.toolTipText")); // NOI18N
    deleteButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        deleteButtonActionPerformed(evt);
      }
    });

    listScrollPane.setPreferredSize(new java.awt.Dimension(627, 130));

    exprList.setModel(new ExprListModel(exprNodes));
    exprList.setToolTipText(org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.exprList.toolTipText")); // NOI18N
    exprList.setMaximumSize(resultTextField.getMaximumSize());
    listScrollPane.setViewportView(exprList);

    javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("(vide)");
    genTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
    genTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
      public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
        genTreeValueChanged(evt);
      }
    });
    treeScrollPane.setViewportView(genTree);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(hintLabel)
          .addComponent(resultLabel)
          .addComponent(commentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(generatorLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(listScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(varTableScroll)
              .addComponent(generatorBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(levelLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(levelSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(194, 194, 194)
                .addComponent(commentButton)
                .addGap(94, 94, 94))
              .addComponent(treeScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(commentScroll)
              .addComponent(valueTextField)
              .addComponent(resultTextField))
            .addGap(0, 0, Short.MAX_VALUE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(deleteButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(valButton)
            .addGap(125, 125, 125)
            .addComponent(autoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addGap(54, 54, 54))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(listScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(18, 18, 18)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(deleteButton)
          .addComponent(valButton)
          .addComponent(autoButton))
        .addGap(20, 20, 20)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(commentScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(commentLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(levelSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(levelLabel)
          .addComponent(commentButton))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGap(27, 27, 27)
            .addComponent(generatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(generatorBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addGap(26, 26, 26)
        .addComponent(treeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(33, 33, 33)
        .addComponent(varTableScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(25, 25, 25)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(valueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(hintLabel))
        .addGap(10, 10, 10)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(resultLabel)
          .addComponent(resultTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

    private void resultTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resultTextFieldActionPerformed
      addExprNode(toAdd);
      for (int i = 0; i < varsTable.getRowCount(); i++) {
        varsTable.setValueAt("", i, 0);
        varsTable.setValueAt("", i, 1);
        varsTable.setValueAt("", i, 2);
      }
      toAdd = null;
      resultTextField.setText("");
    }//GEN-LAST:event_resultTextFieldActionPerformed

  private void addExprNode(ExprNode en) {
    if (en != null) {
      try {
        int n = exprNodes.size();
        en.setRange(n);
        listModel.add(en);
        exprList.ensureIndexIsVisible(n);
        mdo.insert(exprNodes.subList(n, n + 1), n, generator);
      } catch (Exception ex) {
        displayMessage("insertion failed : " + ex.getMessage(), "Expression error");
      }
    }
  }
    
    
    private void valButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valButtonActionPerformed
      if (!resultReady && curSchema instanceof MatchExpr) {
        MatchExpr matchExpr = (MatchExpr) curSchema;
        int index = exprList.getSelectedIndex();
        ExprNode en = (ExprNode) listModel.getElementAt(index);
        Expression e = en.getE().copy();
        if (generator.nextmatch(e, matchExpr)) {
          resultReady = true;
          valueTextField.setText(syntaxWrite.toString(e));
          int row = 0;
          for (Map.Entry<Expression, Expression> entry : curSchema.getVarMap().entrySet()) {
            String key = syntaxWrite.toString(entry.getKey());
            String val = syntaxWrite.toString(entry.getValue());
            String type = entry.getValue().getType();
            varsTable.setValueAt(key, row, 0);
            varsTable.setValueAt(val, row, 1);
            varsTable.setValueAt(type, row, 2);
            row++;
          }
          curSchema.updateRgs(index);
          TreePath path = genTree.getSelectionModel().getLeadSelectionPath();
          genTree.expandPath(path);
        } else {
          valueTextField.setText("L'expression choisie ne convient pas.");
          resultReady = false;
        }
      }
    }//GEN-LAST:event_valButtonActionPerformed

    private void commentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commentButtonActionPerformed
      ExprNode en = (ExprNode) exprList.getSelectedValue();      
      en.setComment(commentArea.getText());
      mdo.setModified(true);
    }//GEN-LAST:event_commentButtonActionPerformed

    private void autoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoButtonActionPerformed
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          level = (int) levelSpinner.getValue();
          int oldsize, inf = 0;
          do {
            oldsize = exprNodes.size();
            for (Schema genSchema : getGenerator().getSchemas()) {
              GenItem genItem = (GenItem) genSchema;
              if(!genItem.isReady()) continue;
              Schema schema = genItem, child;
              loop:
              do { // examen des childs
                int cnt = schema.getChildCount() - 1;              
                for (int m = 0; m <= cnt; m++) { // liste child
                  child = (Schema) schema.getChildAt(m);
                  System.arraycopy(schema.getRgs(), 0, child.getRgs(), 0, schema.getRgs().length);
                  child.setReady(schema.isReady()); // transmission descendante
                  int last = child.getRgs().length - 1;
                  if (child instanceof Result && child.isReady()) {
                    ExprNode en = new ExprNode(null, null, new ArrayList<>());
                    if (schema.getRgs().length > 0) {
                      en.getParentList().add(schema.getRgs());
                    }
                    if(generator.newExpr(en, (Result)child, exprNodes)) {
                      addExprNode(en);
                    }
                    schema.setReady(m != cnt); // pour genItem
                    /*System.out.println(schema.log()+ " -> "+ child.log() + " :" + 
                            exprNodes.get(exprNodes.size()-1));  */// première sortie
                  } 
                  else if (child instanceof MatchExpr) {
                    MatchExpr matchExpr = (MatchExpr) child;
                    if(!child.isReady()) {
                      child.setReady(child.getRgs()[last] > inf);
                    }
                    for (int i = child.getRgs()[last]; i < oldsize; i++) { // parcours de la liste
                      child.getRgs()[last] = i + 1;
                      child.getVarMap().clear();
                      child.getVarMap().putAll(schema.getVarMap());
                      Expression e = exprNodes.get(i).getE();
                      if (generator.nextmatch(e, matchExpr)) {
                        //System.out.println(schema.log()+ " -> "+ child.log() + " e"+i+":"+ e);
                        schema = child;
                        continue loop; // 2è sortie
                      }  
                    } // plus d'expressions, 3è sortie
                    child.getRgs()[last] = 0; // seul le dernier est pris en compte
                    //System.out.println(schema.log()+ " -> "+ child.log()+ " ...");
                  }
                } // fin boucle child                       
                if (Thread.interrupted()) {
                  exprList.repaint();
                  return;
                } 
                schema = (Schema) schema.getParent();
              } while (!generator.equals(schema)); // remonte au parent
            } // boucle genItem
            inf = exprNodes.size();
          } while (oldsize < exprNodes.size());
        }
      };
      final RequestProcessor.Task theTask = RP.create(runnable);
      final ProgressHandle ph = ProgressHandleFactory.createHandle("génération automatique", theTask);
      theTask.addTaskListener((org.openide.util.Task task) -> {
        ph.finish();
      });
      ph.start(); //start the progresshandle the progress UI will show 500s after        
      theTask.schedule(0); //this actually start the task
    }//GEN-LAST:event_autoButtonActionPerformed


    private void levelSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_levelSpinnerStateChanged
      level = (int) levelSpinner.getValue();
    }//GEN-LAST:event_levelSpinnerStateChanged

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
      NotifyDescriptor d = new NotifyDescriptor.Confirmation("Really delete?", "Delete expression",
              NotifyDescriptor.OK_CANCEL_OPTION);
      if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
        int index = exprList.getSelectedIndex();
        try {
          listModel.remove(index);
          mdo.delete(index, generator);
        } catch (Exception ex) {
          displayMessage(ex.getMessage(), "Error Message");
        }
      }
    }//GEN-LAST:event_deleteButtonActionPerformed

  private void genTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_genTreeValueChanged
    Object node = genTree.getLastSelectedPathComponent();
    if (node != null && (node instanceof Schema)) {
      Schema schema = (Schema) node;
      Schema pre = (Schema) schema.getParent();
      if ((pre instanceof GenItem) || (resultReady && pre.equals(curSchema))) {
        curSchema = schema;
        resultReady = false;
        if (curSchema instanceof MatchExpr) {
          curSchema.getVarMap().clear();
          curSchema.getVarMap().putAll(pre.getVarMap()); // transmission des variables
          ArrayList<Expression> vars = curSchema.getVars(); 
          Set<Expression> prevars = curSchema.getVarMap().keySet();
          int m = prevars.size();
          for (Expression var : vars) {
            if (!prevars.contains(var) && m < varsTable.getRowCount()) {
              varsTable.setValueAt(var.toString(), m, 0);
              varsTable.setValueAt("", m, 1);
              varsTable.setValueAt(var.getType(), m, 2);
              m++;
            }
          }
          for (int i = m; i < varsTable.getRowCount(); i++) {
            varsTable.setValueAt("", i, 0);
            varsTable.setValueAt("", i, 1);
            varsTable.setValueAt("", i, 2);            
          }
          valueTextField.setText("Choisir une expression de la liste conforme "
                  + "au modèle et cliquer sur valeur");
        } else if (curSchema instanceof Result) { // fabrique une expression, vérifie la liste
          Result result = (Result) curSchema;
          toAdd = new ExprNode(null, null, new ArrayList<>());
          if (result.getRgs().length > 0) {
            toAdd.getParentList().add(curSchema.getRgs());
          }
          result.setReady(generator.newExpr(toAdd, result, exprNodes));
          resultTextField.setText(syntaxWrite.toString(toAdd.getE()));
          resultTextField.requestFocus();
          if(!result.isReady()) {
            toAdd = null;
          }
        }
      } else {
        valueTextField.setText("Sélectionner le modèle ou le résultat suivant");
      }
    }
  }//GEN-LAST:event_genTreeValueChanged

  private static void displayMessage(Object message, String title) {
    NotifyDescriptor nd = new NotifyDescriptor(message, title,
            NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, null, null);
    DialogDisplayer.getDefault().notify(nd);
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton autoButton;
  private javax.swing.JTextArea commentArea;
  private javax.swing.JButton commentButton;
  private javax.swing.JLabel commentLabel;
  private javax.swing.JScrollPane commentScroll;
  private javax.swing.JButton deleteButton;
  private javax.swing.JList exprList;
  private org.maupou.sampledataobject.GenTree genTree;
  private javax.swing.JComboBox generatorBox;
  private javax.swing.JLabel generatorLabel;
  private javax.swing.JLabel hintLabel;
  private javax.swing.JLabel levelLabel;
  private javax.swing.JSpinner levelSpinner;
  private javax.swing.JScrollPane listScrollPane;
  private javax.swing.JLabel resultLabel;
  private javax.swing.JTextField resultTextField;
  private javax.swing.JScrollPane treeScrollPane;
  private javax.swing.JButton valButton;
  private javax.swing.JTextField valueTextField;
  private javax.swing.JScrollPane varTableScroll;
  private javax.swing.JTable varsTable;
  // End of variables declaration//GEN-END:variables
    @Override
  public void componentOpened() {
    // TODO add custom code on component opening
  }

  @Override
  public void componentClosed() {
    mdo.setModified(false);
  }

  void writeProperties(java.util.Properties p) {
    // better to version settings since initial version as advocated at
    // http://wiki.apidesign.org/wiki/PropertyFiles
    p.setProperty("version", "1.0");
    // TODO store your settings
  }

  void readProperties(java.util.Properties p) {
    String version = p.getProperty("version");
    // TODO read your settings according to their version
  }

  @Override
  public JComponent getVisualRepresentation() {
    return this;
  }

  @Override
  public JComponent getToolbarRepresentation() {
    return toolbar;
  }

  @Override
  public void setMultiViewCallback(MultiViewElementCallback callback) {
    this.callback = callback;
  }

  @Override
  public CloseOperationState canCloseElement() {
    return (mdo.isModified()) ? createUnsafeCloseState("", null, null) : CloseOperationState.STATE_OK;
  }

  @Override
  public Action[] getActions() {
    return new Action[0];
  }

  @Override
  public Lookup getLookup() {
    return mdo.getLookup();
  }

  @Override
  public void componentShowing() {
  }

  @Override
  public void componentHidden() {
  }

  @Override
  public void componentActivated() {
  }

  @Override
  public void componentDeactivated() {
  }

  @Override
  public UndoRedo getUndoRedo() {
    return UndoRedo.NONE;
  }

  public Generator getGenerator() {
    return generator;
  }


  ArrayList<ExprNode> getExprNodes() {
    return exprNodes;
  }

  public SyntaxWrite getSyntaxWrite() {
    return syntaxWrite;
  }

  public ExprListModel getListModel() {
    return listModel;
  }



}
