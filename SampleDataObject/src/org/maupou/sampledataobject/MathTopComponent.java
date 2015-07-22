/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.sampledataobject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.tree.DefaultMutableTreeNode;
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
  private ArrayList<ExprNode> listparents;
  private Syntax syntax;
  private SyntaxWrite syntaxWrite;
  private ArrayList<Generator> generators;
  private Generator generator;
  private GenItem genItem;
  private boolean resultReady;
  private int level;
  private HashMap<Expression, Expression> varsToExprs;
  private MultiViewElementCallback callback;
  private final JToolBar toolbar = new JToolBar();
  private static RequestProcessor RP;
  private static final Logger log = Logger.getLogger(MathTopComponent.class.getName());
  private GenTree genTree;
  private Schema curSchema;

  public MathTopComponent() {
    initComponents();
    genTree = (GenTree) treeGenItem;
    setName(Bundle.CTL_MathTopComponent());
    setToolTipText(Bundle.HINT_MathTopComponent());
    toAdd = null;
    exprNodes = new ArrayList<>();
    varsToExprs = new HashMap<>();
    listparents = new ArrayList<>();
    level = 1;
    RP = new RequestProcessor("Generation of expressions", 1, true);
    log.setLevel(Level.INFO);
  }

  public MathTopComponent(MathDataObject mdo) {
    this();
    this.mdo = mdo;
    try {
      syntax = mdo.setSyntax();
      syntaxWrite = syntax.getSyntaxWrite();
      //genTree.setSw(syntaxWrite);
      generators = syntax.getGenerators();
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
   * met à jour exprRange et editField
   *
   * @throws Exception
   */
  private void updateEditor() {
    int max = exprNodes.size();
    int min = (max == 0) ? 0 : 1;
    SpinnerNumberModel model = (SpinnerNumberModel) exprRange.getModel();
    model.setValue(max);
    model.setMaximum(max);
    model.setMinimum(min);
    try {
      if (max > 0) {
        ExprNode en = exprNodes.get(max - 1);
        editField.setText(en.getE().toString(syntaxWrite));
      } else {
        editField.setText("");
      }
    } catch (Exception ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  /**
   * met à jour le generator et les expNodes correspondant
   *
   * @param generator
   * @throws Exception
   */
  private void updateGenerator(Generator generator) throws Exception {
    ArrayList<GenItem> genItems = generator.getGenItems();
    String[] itemStrings = new String[genItems.size()];
    for (int i = 0; i < itemStrings.length; i++) {
      itemStrings[i] = genItems.get(i).getName();
    }
    try {
      exprNodes = mdo.readExprNodes(generator);
      updateEditor();
    } catch (Exception ex) {
      displayMessage("Erreur de lecture des expressions " + ex.getMessage(), "Error message");
    }
    genItemBox.setModel(new DefaultComboBoxModel(itemStrings));
    if (!genItems.isEmpty()) {
      genItem = genItems.get(0);
      updateGenItem(genItem);
    }
    genItemBox.repaint();
  }

  /**
   * met à jour le genItem et les variables
   *
   * @param genItem
   */
  private void updateGenItem(GenItem genItem) throws Exception {
    resultTextField.setText("");
    /* avant
     genTree.getRoot().setUserObject(genItem.getName());
     genTree.setTree(genItem.getSchemas());
     //*/
    //* modif
    DefaultTreeModel model = (DefaultTreeModel) genTree.getModel();
    model.setRoot(genItem);
    model.reload();
    genTree.expandPath(new TreePath(genItem));
    //*/
    listparents.clear();
    if (!genItem.getSchemas().isEmpty()) {
      ArrayList<Expression> vars = genItem.getSchemas().get(0).getVars();
      int m = vars.size();
      for (int k = 0; k < varsTable.getRowCount(); k++) {
        String name = (k < m) ? vars.get(k).toString() : "";
        String type = (k < m) ? vars.get(k).getType() : "";
        varsTable.setValueAt(name, k, 0);
        varsTable.setValueAt("", k, 1);
        varsTable.setValueAt(genItem.getTypesMap().get(type), k, 2);
      }
    }
    valueTextField.setText("Sélectionner un modèle ou un résultat");
    varsToExprs.clear();
    resultReady = false;
    curSchema = null;
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT
   * modify this code. The content of this method is always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    editField = new javax.swing.JTextField();
    exprRange = new javax.swing.JSpinner();
    jScrollPane1 = new javax.swing.JScrollPane();
    commentArea = new javax.swing.JTextArea();
    commentLabel = new javax.swing.JLabel();
    editLabel = new javax.swing.JLabel();
    generatorLabel = new javax.swing.JLabel();
    generatorBox = new javax.swing.JComboBox();
    itemLabel = new javax.swing.JLabel();
    genItemBox = new javax.swing.JComboBox();
    jScrollPane3 = new javax.swing.JScrollPane();
    varsTable = new javax.swing.JTable();
    hintLabel = new javax.swing.JLabel();
    valueTextField = new javax.swing.JTextField();
    resultLabel = new javax.swing.JLabel();
    resultTextField = new javax.swing.JTextField();
    toValButton = new javax.swing.JButton();
    cntResultsLabel = new javax.swing.JLabel();
    cntResSpinner = new javax.swing.JSpinner();
    levelLabel = new javax.swing.JLabel();
    levelSpinner = new javax.swing.JSpinner();
    autoButton = new javax.swing.JButton();
    commentButton = new javax.swing.JButton();
    deleteButton = new javax.swing.JButton();
    treeScrollPane = new javax.swing.JScrollPane();
    treeGenItem = new GenTree();

    editField.setEditable(false);
    editField.setText(org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.editField.text")); // NOI18N

    exprRange.setModel(new javax.swing.SpinnerNumberModel(0, 0, 0, 1));
    exprRange.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        exprRangeStateChanged(evt);
      }
    });

    commentArea.setColumns(20);
    commentArea.setRows(5);
    jScrollPane1.setViewportView(commentArea);

    org.openide.awt.Mnemonics.setLocalizedText(commentLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.commentLabel.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(editLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.editLabel.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(generatorLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.generatorLabel.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(itemLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.itemLabel.text")); // NOI18N

    genItemBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        genItemBoxActionPerformed(evt);
      }
    });

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
    jScrollPane3.setViewportView(varsTable);

    org.openide.awt.Mnemonics.setLocalizedText(hintLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.hintLabel.text")); // NOI18N

    valueTextField.setText(org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.valueTextField.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(resultLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.resultLabel.text")); // NOI18N

    resultTextField.setText(org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.resultTextField.text")); // NOI18N
    resultTextField.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        resultTextFieldActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(toValButton, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.toValButton.text")); // NOI18N
    toValButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toValButtonActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(cntResultsLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.cntResultsLabel.text")); // NOI18N

    cntResSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(20), null, null, Integer.valueOf(1)));

    org.openide.awt.Mnemonics.setLocalizedText(levelLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.levelLabel.text")); // NOI18N

    levelSpinner.setModel(new javax.swing.SpinnerNumberModel());
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
    commentButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        commentButtonActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.deleteButton.text")); // NOI18N
    deleteButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        deleteButtonActionPerformed(evt);
      }
    });

    treeGenItem.setModel(treeGenItem.getModel());
    treeGenItem.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
      public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
        treeGenItemValueChanged(evt);
      }
    });
    treeScrollPane.setViewportView(treeGenItem);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(exprRange, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(hintLabel)
              .addComponent(resultLabel)
              .addComponent(commentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(itemLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(generatorLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(editField, javax.swing.GroupLayout.Alignment.TRAILING)
                  .addComponent(valueTextField)
                  .addComponent(resultTextField))
                .addGap(38, 38, 38))
              .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                  .addComponent(treeScrollPane, javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
                  .addComponent(genItemBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(generatorBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addGroup(layout.createSequentialGroup()
                    .addGap(2, 2, 2)
                    .addComponent(cntResultsLabel)
                    .addGap(18, 18, 18)
                    .addComponent(cntResSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(levelLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(levelSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(194, 194, 194)
                    .addComponent(commentButton)
                    .addGap(94, 94, 94))
                  .addComponent(jScrollPane1))
                .addContainerGap(38, Short.MAX_VALUE))))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(editLabel)
            .addGap(351, 351, 351))))
      .addGroup(layout.createSequentialGroup()
        .addGap(191, 191, 191)
        .addComponent(deleteButton)
        .addGap(77, 77, 77)
        .addComponent(toValButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(autoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(54, 54, 54))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(18, 18, 18)
        .addComponent(editLabel)
        .addGap(18, 18, 18)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(editField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(exprRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(36, 36, 36)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(deleteButton)
          .addComponent(toValButton)
          .addComponent(autoButton))
        .addGap(20, 20, 20)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(commentLabel))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(levelSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(levelLabel)
          .addComponent(cntResSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(cntResultsLabel)
          .addComponent(commentButton))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGap(27, 27, 27)
            .addComponent(generatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(generatorBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(genItemBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(itemLabel))
        .addGap(18, 18, 18)
        .addComponent(treeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(18, 18, 18)
        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(25, 25, 25)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(valueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(hintLabel))
        .addGap(10, 10, 10)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(resultLabel)
          .addComponent(resultTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(32, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

    private void exprRangeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_exprRangeStateChanged
      Integer range = (Integer) exprRange.getValue();
      try {
        editField.setText(exprNodes.get(range - 1).getE().toString(syntaxWrite));
      } catch (Exception ex) {
        NotifyDescriptor error = new NotifyDescriptor.Message(ex);
      }
    }//GEN-LAST:event_exprRangeStateChanged

    private void genItemBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genItemBoxActionPerformed
      int index = genItemBox.getSelectedIndex();
      genItem = generator.getGenItems().get(index);
      try {
        updateGenItem(genItem);
      } catch (Exception ex) {
        displayMessage("Ne peut afficher l'item : " + genItem.getName(), "Error message");
      }
    }//GEN-LAST:event_genItemBoxActionPerformed

    private void resultTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resultTextFieldActionPerformed
      addExprNode();
      resultTextField.setText("");
      try {
        updateGenItem(genItem);
      } catch (Exception ex) {
        displayMessage("Item non valide : " + ex.getMessage(), "Item error");
      }
    }//GEN-LAST:event_resultTextFieldActionPerformed

  private void addExprNode() {
    if (toAdd != null) {
      try {
        int n = exprNodes.size();
        toAdd.setRange(n);
        exprNodes.add(toAdd);
        Integer rg = (Integer) exprRange.getValue();
        mdo.insert(exprNodes.subList(n, n + 1), rg, generator);
        exprRange.setModel(new SpinnerNumberModel(n + 1, 1, n + 1, 1));
        editField.setText(toAdd.getE().toString(syntaxWrite));
        toAdd = null;
      } catch (Exception ex) {
        displayMessage("insertion failed : " + ex.getMessage(), "Expression error");
      }
    }
  }

    
    
    private void toValButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toValButtonActionPerformed
      if (!resultReady && curSchema instanceof MatchExpr) {
        try {
          MatchExpr matchExpr = (MatchExpr) curSchema;
          int index = (int) getExprRange().getValue() - 1;
          ExprNode en = exprNodes.get(index);
          Expression e = en.getE().copy();
          if (matchExpr.checkExpr(e, curSchema.getVarMap(), genItem.getTypesMap(),
                  genItem.getListvars(), syntax)) {
            resultReady = true;
            valueTextField.setText(editField.getText());
            int row = 0;
            for (Map.Entry<Expression, Expression> entry : curSchema.getVarMap().entrySet()) {
              String key = entry.getKey().toString(syntaxWrite);
              String val = entry.getValue().toString(syntaxWrite);
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
        } catch (Exception ex) {
          displayMessage(ex.getMessage(), "Error message");
        }
      }
    }//GEN-LAST:event_toValButtonActionPerformed

    private void commentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commentButtonActionPerformed
      int n = (int) exprRange.getValue();
      ExprNode en = exprNodes.get(n - 1);
      en.setComment(commentArea.getText());
      mdo.setModified(true);
    }//GEN-LAST:event_commentButtonActionPerformed

    private void autoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoButtonActionPerformed
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          int oldsize;
          ArrayList<ExprNode> exprDiscards = new ArrayList<>();
          do {
            oldsize = exprNodes.size();
            for (GenItem genItem : generator.getGenItems()) {
              Schema schema = genItem, child;
              HashMap<String, String> typesMap = genItem.getTypesMap();
              ArrayList<Expression> listvars = genItem.getListvars();
              int lastmatch = 0;
              loop:
              do { // on suppose que schema est vérifié
                int cnt = schema.getChildCount() - 1;              
                for (int m = lastmatch; m <= cnt; m++) { // 
                  child = (Schema) schema.getChildAt(m);
                  System.arraycopy(schema.getRgs(), 0, child.getRgs(), 0, schema.getRgs().length);
                  int last = child.getRgs().length - 1;
                  if (child instanceof Result) {
                    Expression e = child.getPattern().copy().replace(schema.getVarMap());
                    ExprNode en = new ExprNode(null, null, new ArrayList<>());
                    if (schema.getRgs().length > 0) {
                      en.getParentList().add(schema.getRgs());
                    }
                    toAdd = ((Result)child).newExpr(en, schema.getVarMap(), typesMap, listvars, syntax, 
                            exprNodes, exprDiscards);
                    addExprNode();
                  } 
                  else if (child instanceof MatchExpr) {
                    MatchExpr matchExpr = (MatchExpr) child;
                    for (int i = child.getRgs()[last]; i < oldsize; i++) { // parcours de la liste
                      matchExpr.getVarMap().clear();
                      if (schema instanceof MatchExpr) {
                        matchExpr.getVarMap().putAll(((MatchExpr) schema).getVarMap());
                      }
                      Expression e = exprNodes.get(i).getE();
                      if (matchExpr.checkExpr(e, matchExpr.getVarMap(), typesMap, listvars, syntax)) {
                        child.getRgs()[last] = i + 1;
                        schema = child;
                        continue loop;
                      }                      
                      if (Thread.interrupted()) {
                        return;
                      }
                    } // plus d'expressions
                  }
                } // fin boucle child                
                schema = (Schema) schema.getParent();
              } while (schema != null && !schema.equals(genItem)); // (si child of Result ou child = MatchExpr
            } // boucle genItem
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
        int index = (Integer) exprRange.getValue() - 1;
        try {
          if (exprNodes.remove(index) != null) {
            updateEditor();
          }
        } catch (Exception ex) {
          Exceptions.printStackTrace(ex);
        }
        mdo.delete(index, generator);
      }
    }//GEN-LAST:event_deleteButtonActionPerformed

  private void treeGenItemValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeGenItemValueChanged
    Object node = genTree.getLastSelectedPathComponent();
    if (node != null && (node instanceof Schema)) {
      Schema schema = (Schema) node;
      TreeNode pre = (Schema) schema.getParent();
      boolean ok = (pre != null) && ((pre instanceof GenItem) || (resultReady && pre.equals(curSchema)));
      if (ok) {
        HashMap<Expression, Expression> varMap = (curSchema == null) ? new HashMap<>() : curSchema.getVarMap();
        curSchema = schema;
        curSchema.getVarMap().clear();
        curSchema.getVarMap().putAll(varMap);
        resultReady = false;
        if (curSchema instanceof MatchExpr) {
          valueTextField.setText("Choisir une expression de la liste conforme "
                  + "au modèle et cliquer sur valeur");
        } else if (curSchema instanceof Result) {
          Expression e = curSchema.getPattern().copy().replace(curSchema.getVarMap());
          ArrayList<int[]> parents = new ArrayList<>();
          if (curSchema.getRgs().length > 0) {
            parents.add(curSchema.getRgs());
          }
          toAdd = new ExprNode(e, new ArrayList<>(), parents);
          int index = exprNodes.indexOf(toAdd);
          if (index != -1) {
            ExprNode en = exprNodes.get(index);
            index = en.getParentList().indexOf(curSchema.getRgs());
            if (index != -1) {
              en.getParentList().add(curSchema.getRgs());
            }
            toAdd = null;
          }
          try {
            resultTextField.setText(e.toString(syntaxWrite));
            resultTextField.requestFocus();
          } catch (Exception ex) {
            displayMessage(ex.getMessage(), "Error message");
          }
        }
      } else {
        displayMessage("sélection incorrecte", "Error message");
      }
    }
  }//GEN-LAST:event_treeGenItemValueChanged

  private static void displayMessage(Object message, String title) {
    NotifyDescriptor nd = new NotifyDescriptor(message, title,
            NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, null, null);
    DialogDisplayer.getDefault().notify(nd);
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton autoButton;
  private javax.swing.JSpinner cntResSpinner;
  private javax.swing.JLabel cntResultsLabel;
  private javax.swing.JTextArea commentArea;
  private javax.swing.JButton commentButton;
  private javax.swing.JLabel commentLabel;
  private javax.swing.JButton deleteButton;
  private javax.swing.JTextField editField;
  private javax.swing.JLabel editLabel;
  private javax.swing.JSpinner exprRange;
  private javax.swing.JComboBox genItemBox;
  private javax.swing.JComboBox generatorBox;
  private javax.swing.JLabel generatorLabel;
  private javax.swing.JLabel hintLabel;
  private javax.swing.JLabel itemLabel;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JLabel levelLabel;
  private javax.swing.JSpinner levelSpinner;
  private javax.swing.JLabel resultLabel;
  private javax.swing.JTextField resultTextField;
  private javax.swing.JButton toValButton;
  private javax.swing.JTree treeGenItem;
  private javax.swing.JScrollPane treeScrollPane;
  private javax.swing.JTextField valueTextField;
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

  ArrayList<ExprNode> getExprNodes() {
    return exprNodes;
  }

  public SyntaxWrite getSyntaxWrite() {
    return syntaxWrite;
  }

  public javax.swing.JSpinner getExprRange() {
    return exprRange;
  }

}
