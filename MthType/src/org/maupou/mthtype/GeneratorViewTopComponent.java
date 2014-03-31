/*
 * Copyright (C) 2013 Patrice
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.maupou.mthtype;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.text.StyledDocument;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.maupou.expressions.*;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.maupou.mthFile//GeneratorView//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "GeneratorViewTopComponent",
        iconBase = "org/maupou/mthtype/arrow.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_GeneratorViewAction",
        preferredID = "GeneratorViewTopComponent")

@Messages({
    "CTL_GeneratorViewAction=GeneratorView",
    "CTL_GeneratorViewTopComponent=GeneratorView Window",
    "HINT_GeneratorViewTopComponent=This is a GeneratorView window"
})
public final class GeneratorViewTopComponent extends CloneableTopComponent {

    private Syntax syntax;
    private mathDataObject mdo;
    private SyntaxWrite syntaxWrite;
    private ArrayList<Generator> generators;
    private Generator generator;
    private GenItem genItem;
    private ArrayList<ExprNode> exprNodes;
    private ArrayList<ExprNode> listparents;
    private ArrayList<ExprNode> exrpDiscards;
    private HashMap<Expression, Expression> varsToExprs;
    private boolean resultReady;
    private int level, limit, matchRange;
    private Document document;
    private StyledDocument styleDocument;

    public GeneratorViewTopComponent() {
        initComponents();
        varsToExprs = new HashMap<>();
        listparents = new ArrayList<>();
        exprNodes = new ArrayList<>();
        //setName("GeneratorView Window");
        //setToolTipText(Bundle.HINT_GeneratorViewTopComponent());
    }

    public GeneratorViewTopComponent(mathDataObject mdo) {
        this();
        this.mdo = mdo;
        syntax = mdo.getSyntax();
        document = mdo.getDocument();
        try {
            DataEditorSupport dataEditorSupport = mdo.getLookup().lookup(DataEditorSupport.class);
            styleDocument = dataEditorSupport.openDocument();
        } catch (IOException ex) {
            NotifyDescriptor error = new NotifyDescriptor.Message(ex);
        }
        if (syntax != null) {
            syntaxWrite = syntax.getSyntaxWrite();
            generators = syntax.getGenerators();
            String[] genNnames = new String[generators.size()];
            for (int i = 0; i < generators.size(); i++) {
                genNnames[i] = generators.get(i).getName();
            }
            generatorsBox.setModel(new DefaultComboBoxModel<>(genNnames));
            if (!generators.isEmpty()) {
                generator = generators.get(0);
                updateGenerator(generator);
            }
            readExprNodes(document);
        }
    }

    /**
     * Etablit la liste exprNodes à partir du document
     * @param doc le dom chargé
     */
    private void readExprNodes(Document doc) {
        NodeList list = doc.getElementsByTagName("expr");
        for (int i = 0; i < list.getLength(); i++) {
            Element item = (Element) list.item(i);
            Node next = item.getFirstChild();
            String etxt = "";
            ArrayList<Integer> childs = new ArrayList<>();
            ArrayList<int[]> parents = new ArrayList<>();
            while (next != null) {
                if (next.getNodeType() == Node.CDATA_SECTION_NODE) {
                    etxt = next.getTextContent();
                } else if (next.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) next;
                    switch (elem.getTagName()) {
                        case "parents":
                            String[] s = elem.getTextContent().trim().split(" ");
                            for (String string : s) {
                                String[] si = string.split("-");
                                int[] p = new int[2];
                                if (si.length == 2) {
                                    p[0] = Integer.parseInt(si[0]);
                                    p[1] = Integer.parseInt(si[1]);
                                    parents.add(p);
                                }
                            }
                            break;
                        case "children":
                            s = elem.getTextContent().trim().split(" ");
                            for (String string : s) {
                                childs.add(Integer.parseInt(string));
                            }
                            break;
                    }
                }
                next = next.getNextSibling();
            }
            if(!etxt.isEmpty()) {
                try {
                    Expression e = new Expression(etxt, syntax);
                    ExprNode en = new ExprNode(e, childs, parents);
                    exprNodes.add(en);
                } catch (Exception ex) {
                    String message = "No expression for " + etxt;
                    NotifyDescriptor error = new NotifyDescriptor.Message(message);
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        title = new javax.swing.JLabel();
        generatorsBox = new javax.swing.JComboBox();
        jSeparator = new javax.swing.JSeparator();
        manuelButton = new javax.swing.JRadioButton();
        autoButton = new javax.swing.JRadioButton();
        genItemBox = new javax.swing.JComboBox();
        genItemNameLabel = new javax.swing.JLabel();
        resultRanges = new javax.swing.JSpinner();
        rangesLabel = new javax.swing.JLabel();
        resultLabel = new javax.swing.JLabel();
        resultField = new javax.swing.JTextField();
        commLabel = new javax.swing.JLabel();
        refCheckBox = new javax.swing.JCheckBox();
        matchCheckBox = new javax.swing.JCheckBox();
        typeCheckBox = new javax.swing.JCheckBox();
        valueLabel = new javax.swing.JLabel();
        valueField = new javax.swing.JTextField();
        nbResultSpinner = new javax.swing.JSpinner();
        nbResultsLbl = new javax.swing.JLabel();
        varsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        varsTable = new javax.swing.JTable();
        levelLabel = new javax.swing.JLabel();
        levelSpinner = new javax.swing.JSpinner();
        goButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        freeTextField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        replaceMap = new javax.swing.JTable();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.jLabel1.text")); // NOI18N

        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(title, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.title.text")); // NOI18N

        generatorsBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generatorsBoxActionPerformed(evt);
            }
        });

        buttonGroup.add(manuelButton);
        manuelButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(manuelButton, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.manuelButton.text")); // NOI18N

        buttonGroup.add(autoButton);
        org.openide.awt.Mnemonics.setLocalizedText(autoButton, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.autoButton.text")); // NOI18N

        genItemBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genItemBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(genItemNameLabel, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.genItemNameLabel.text")); // NOI18N

        resultRanges.setValue(1);
        resultRanges.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                resultRangesStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(rangesLabel, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.rangesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(resultLabel, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.resultLabel.text")); // NOI18N

        resultField.setText(org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.resultField.text")); // NOI18N
        resultField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resultFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(commLabel, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.commLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(refCheckBox, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.refCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(matchCheckBox, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.matchCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(typeCheckBox, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.typeCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(valueLabel, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.valueLabel.text")); // NOI18N

        valueField.setText(org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.valueField.text")); // NOI18N
        valueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueFieldActionPerformed(evt);
            }
        });

        nbResultSpinner.setValue(20);

        org.openide.awt.Mnemonics.setLocalizedText(nbResultsLbl, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.nbResultsLbl.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(varsLabel, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.varsLabel.text")); // NOI18N

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
                "nom", "valeur", "type"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        varsTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(varsTable);
        if (varsTable.getColumnModel().getColumnCount() > 0) {
            varsTable.getColumnModel().getColumn(0).setPreferredWidth(5);
            varsTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.varsTable.columnModel.title0")); // NOI18N
            varsTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.varsTable.columnModel.title1")); // NOI18N
            varsTable.getColumnModel().getColumn(2).setPreferredWidth(20);
            varsTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.varsTable.columnModel.title2")); // NOI18N
        }

        org.openide.awt.Mnemonics.setLocalizedText(levelLabel, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.levelLabel.text")); // NOI18N

        levelSpinner.setValue(1);
        levelSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                levelSpinnerStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(goButton, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.goButton.text")); // NOI18N
        goButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.jLabel2.text")); // NOI18N

        freeTextField.setText(org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.freeTextField.text")); // NOI18N
        freeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freeTextFieldActionPerformed(evt);
            }
        });

        replaceMap.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "modèle", "remplacement"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(replaceMap);
        if (replaceMap.getColumnModel().getColumnCount() > 0) {
            replaceMap.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.replaceMap.columnModel.title0")); // NOI18N
            replaceMap.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.replaceMap.columnModel.title1")); // NOI18N
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(title, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(autoButton)
                        .addGap(38, 38, 38)
                        .addComponent(nbResultsLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nbResultSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(levelLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(levelSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(goButton)
                        .addGap(72, 72, 72))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(varsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
                                    .addComponent(jScrollPane1)))
                            .addComponent(manuelButton)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(resultLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(resultField))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(valueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(valueField, javax.swing.GroupLayout.PREFERRED_SIZE, 448, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(genItemNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(genItemBox, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(rangesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(resultRanges, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(commLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(matchCheckBox)
                                .addGap(34, 34, 34)
                                .addComponent(refCheckBox)
                                .addGap(79, 79, 79)
                                .addComponent(typeCheckBox))
                            .addComponent(generatorsBox, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGap(258, 258, 258)
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(228, 228, 228))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(freeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(freeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 20, Short.MAX_VALUE)
                .addComponent(title)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(goButton)
                    .addComponent(levelSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(levelLabel)
                    .addComponent(nbResultSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nbResultsLbl)
                    .addComponent(autoButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(manuelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(generatorsBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(genItemBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resultRanges, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rangesLabel)
                    .addComponent(genItemNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(commLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(matchCheckBox)
                            .addComponent(refCheckBox)
                            .addComponent(typeCheckBox))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(valueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(valueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(resultField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(resultLabel)))
                    .addComponent(varsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(50, 50, 50))
        );
    }// </editor-fold>//GEN-END:initComponents


  private void genItemBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genItemBoxActionPerformed
      int index = genItemBox.getSelectedIndex();
      genItem = generator.getGenItems().get(index);
      updateGenItem(genItem, 1);
  }//GEN-LAST:event_genItemBoxActionPerformed

    private void updateGenItem(GenItem genItem, int range) {
        resultField.setText("");
        ArrayList<Result> resultExprs = genItem.getResultExprs();
        resultRanges.setModel(new SpinnerNumberModel(1, 1, 1, 0));
        ArrayList<MatchExpr> matchExprs = genItem.getMatchExprs();
        int n = matchExprs.size();
        HashMap<Expression, Expression> map = (n == 0) ? new HashMap() : matchExprs.get(0).getReplaceMap();
        matchRange = 0;
        fillTable(map);
        listparents = new ArrayList<>();
        for (int i = 0; i < varsTable.getRowCount(); i++) {
            varsTable.setValueAt("", i, 0);
            varsTable.setValueAt("", i, 1);
            varsTable.setValueAt("", i, 2);
        }
        if (!resultExprs.isEmpty()) {
            resultRanges.setModel(new SpinnerNumberModel(range, 1, resultExprs.size(), 1));
            if (range < resultExprs.size() + 1) {
                String result = "";
                result += resultExprs.get(range - 1);
                resultField.setText(result);
                if (n == 0) {
                    resultField.requestFocus();
                }
                else {
                    valueField.requestFocus();
                }
            }
        }
        valueField.setText("");
        varsToExprs.clear();
        resultReady = false;
    }

    /**
     * Mise à jour de la table des modèles
     * @param map table des modèles et remplacements éventuels
     */
    private void fillTable(HashMap<Expression, Expression> map) {
        int rows = replaceMap.getRowCount();
        Iterator<Expression> it = map.keySet().iterator();
        for (int row = 0; row < rows; row++) {
            String t0 = "", t1 = "";
            if (it.hasNext()) {
                Expression key = it.next();
                Expression val = map.get(key);
                try {
                    t0 = key.toString(syntaxWrite);
                    t1 = val.toString(syntaxWrite);
                } catch (Exception exc) {}
            }
            replaceMap.setValueAt(t0, row, 0);
            replaceMap.setValueAt(t1, row, 1);
        }
    }

  private void generatorsBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generatorsBoxActionPerformed
      int index = generatorsBox.getSelectedIndex();
      if (index != -1) {
          generator = generators.get(index);
          updateGenerator(generator);
      }
  }//GEN-LAST:event_generatorsBoxActionPerformed

  private void valueFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueFieldActionPerformed
      String e = valueField.getText().trim();
      if (!genItem.getResultExprs().isEmpty()) {
          try {
              MatchExpr matchExpr = genItem.getMatchExprs().get(matchRange);
              Expression expr = new Expression(e, syntax);
              ExprNode en = new ExprNode(expr, new ArrayList<Integer>(), new ArrayList<int[]>());
              int i = exprNodes.indexOf(en);
              if (i != -1) { // expression dans la liste
                  en = exprNodes.get(i);
                  listparents.add(en);
                  en = en.copy();
                  if (matchExpr.checkExpr(en.getE(), varsToExprs, genItem.getFreevars(),
                          genItem.getListvars(), syntax)) {
                      matchRange++;
                      if (matchRange < genItem.getMatchExprs().size()) { // modèle suivant
                          matchExpr = genItem.getMatchExprs().get(matchRange);
                          fillTable(matchExpr.getReplaceMap());
                      } else { // check results
                          int index = (Integer) resultRanges.getValue() - 1;
                          Result result = genItem.getResultExprs().get(index);
                          ExprNode en1 = result.addExpr(en, varsToExprs, genItem.getFreevars(),
                                  genItem.getListvars(), syntax, exprNodes, null);
                          ArrayList<int[]> parentList = new ArrayList<>();
                          int psize = listparents.size();
                          if (psize > 0) {
                              int[] p = new int[psize];
                              for (int k = 0; k < p.length; k++) {
                                  p[k] = exprNodes.indexOf(listparents.get(k));
                              }
                              parentList.add(p);
                          }
                          en1.setParentList(parentList);
                          resultField.setText(en1.getE().toString(syntaxWrite));
                          resultField.requestFocus();
                          resultReady = true;
                          fillTable(new HashMap());
                      }
                      if (manuelButton.isSelected()) { // remplit la table des variables
                          int row = 0;
                          for (Map.Entry<Expression, Expression> entry : varsToExprs.entrySet()) {
                              String key = entry.getKey().toString(syntaxWrite);
                              String val = entry.getValue().toString(syntaxWrite);
                              String type = entry.getValue().getType();
                              varsTable.setValueAt(key, row, 0);
                              varsTable.setValueAt(val, row, 1);
                              varsTable.setValueAt(type, row, 2);
                              row++;
                          }
                      }
                      valueField.setText("");
                  } else {
                      NotifyDescriptor error = new NotifyDescriptor.Message("non conforme au modèle",
                              NotifyDescriptor.INFORMATION_MESSAGE);
                      DialogDisplayer.getDefault().notify(error);
                  }
              }
          } catch (Exception ex) {
              NotifyDescriptor error = new NotifyDescriptor.Message(ex);
          }
      } else {
          resultField.setText(e);
          resultField.requestFocus();
      }
  }//GEN-LAST:event_valueFieldActionPerformed

    /**
     * met à jour le document de tc en ajoutant les nouvelles expressions et
     * l'ouvre à nouveau
     *
     * @param newExprs expressions à ajouter
     * @throws Exception
     */
    private void addToDocument(List<ExprNode> newExprs) throws Exception {
        String gname = (generator == null) ? "freelist" : generator.getName();
        Element root = document.getDocumentElement();
        Element gen = document.createElement("generator");
        gen.setAttribute("name", gname);
        boolean found = false;
        NodeList gl = root.getElementsByTagName("generator");
        for (int i = 0; i < gl.getLength(); i++) {
            Element ge = (Element) gl.item(i);
            if (found = gname.equals(ge.getAttribute("name"))) {
                gen = ge;
                break;
            }
        }
        if (!found) {
            root.appendChild(gen);
        }
        for (ExprNode exprNode : newExprs) {
            //addDiscards(exprNode);
            String parents = "", enfants = "";
            for (int[] is : exprNode.getParentList()) {
                for (int i = 0; i < is.length; i++) {
                    String last = (i == is.length - 1) ? " " : "-";
                    parents += is[i] + last;
                }
            }
            parents = parents.trim();
            for (Integer integer : exprNode.getChildList()) {
                enfants += integer + " ";
            }
            enfants = enfants.trim();
            Expression e = exprNode.getE();
            String etxt = e.toString(syntaxWrite);
            String type = e.getType();
            int index = exprNodes.indexOf(exprNode);
            Element expr = document.createElement("expr");
            expr.setAttribute("id", "" + index);
            expr.setAttribute("type", type);
            CDATASection txtnode = document.createCDATASection(etxt);
            expr.appendChild(txtnode);
            Element comment = document.createElement("comment");
            txtnode = document.createCDATASection("");
            expr.appendChild(txtnode);
            if (!parents.isEmpty()) {
                Element par = document.createElement("parents");
                txtnode = document.createCDATASection(parents);
                par.appendChild(txtnode);
                expr.appendChild(par);
            }
            if (!enfants.isEmpty()) {
                Element children = document.createElement("children");
                txtnode = document.createCDATASection(enfants);
                children.appendChild(txtnode);
                expr.appendChild(children);
            }
            gen.appendChild(expr);
        }
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty(OutputKeys.METHOD, "xml");
        Source source = new DOMSource(document);
        StringWriter result = new StringWriter();
        t.transform(source, new StreamResult(result));
        styleDocument.remove(0, styleDocument.getLength());
        styleDocument.insertString(0, result.toString(), null);
    }

    /**
     * ajoute les expressions cachées à la liste exrpDiscards
     *
     * @param exprNode
     * @throws Exception
     */
    private void addDiscards(ExprNode exprNode) throws Exception {
        Expression e = exprNode.getE();
        for (GenItem discard : generator.getDiscards()) {
            HashMap<Expression, Expression> vars = new HashMap<>();
            HashMap<String, String> freevars = discard.getFreevars();
            ArrayList<Expression> listvars = discard.getListvars();
            Iterator<MatchExpr> it = discard.getMatchExprs().iterator();
            boolean fit = it.next().checkExpr(e, vars, freevars, listvars, syntax);
            while (fit && it.hasNext()) {
                MatchExpr matchExpr = it.next();
                Expression expr = matchExpr.getSchema().replace(vars);
                fit = matchExpr.checkExpr(expr, vars, freevars, listvars, syntax);
            }
            if (!it.hasNext() && fit) { // vars = {T=A->(B->A)}
                Iterator<Result> itr = discard.getResultExprs().iterator();
                while (itr.hasNext()) {
                    Expression expr = new Expression(itr.next().getResult(), syntax); // A->T
                    MatchExpr.extendMap(e, vars, listvars);
                    expr = expr.replace(vars);
                    ExprNode en = new ExprNode(expr, null, null);
                    en.setVisible(false);
                    exrpDiscards.add(en);
                }
            }
        }
    }

    private void resultRangesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_resultRangesStateChanged
        int range = (Integer) resultRanges.getValue();
        updateGenItem(genItem, range);
        if(genItem.getMatchExprs().isEmpty()) {
            resultField.requestFocus();
        }
        else {
            valueField.requestFocus();
        }
    }//GEN-LAST:event_resultRangesStateChanged

    private void levelSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_levelSpinnerStateChanged
        level = (int) levelSpinner.getValue();
    }//GEN-LAST:event_levelSpinnerStateChanged

    private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goButtonActionPerformed
        if (autoButton.isEnabled() && generator != null) {
            goButton.setText("stop");
            limit = (Integer) nbResultSpinner.getValue();
            //*
            exrpDiscards = new ArrayList<>(); // FIXME : boucle infinie
            //*/
            SwingWorker worker = new SwingWorker<ArrayList<ExprNode>, String[]>() {
                @Override
                protected ArrayList<ExprNode> doInBackground() throws Exception {
                    level = 1;
                    boolean once = exprNodes.isEmpty();
                    do {
                        for (GenItem genItem : generator.getGenItems()) {
                            if (!once) {
                                continue;
                            }
                            int oldsize = exprNodes.size();
                            int matchsize = genItem.getMatchExprs().size();
                            int[] genpList = new int[matchsize];
                            HashMap<Expression, Expression> evars = new HashMap<>();
                            ArrayList<Integer> childList = new ArrayList<>();
                            ArrayList<int[]> parentList = new ArrayList<>();
                            parentList.add(genpList);
                            ExprNode en = new ExprNode(null, childList, parentList);
                            genItem.generate(0, limit, level, syntax, en, evars, exprNodes, exrpDiscards);
                            List<ExprNode> newExprs = exprNodes.subList(oldsize, exprNodes.size());
                            addToDocument(newExprs);
                        }
                        once = false;
                    } while (exprNodes.size() < limit);
                    goButton.setText("execute");
                    return exprNodes;
                }
            };
            worker.execute();
        }
    }//GEN-LAST:event_goButtonActionPerformed

    private void freeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freeTextFieldActionPerformed
        String text = freeTextField.getText();
        try {
            Expression expr = new Expression(text, syntax);
            ArrayList<ExprNode> one = new ArrayList<>();
            one.add(new ExprNode(expr, new ArrayList<Integer>(), new ArrayList<int[]>()));
            addToDocument(one);
        } catch (Exception ex) {
            NotifyDescriptor error = new NotifyDescriptor.Message(ex);
        }
    }//GEN-LAST:event_freeTextFieldActionPerformed

    private void resultFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resultFieldActionPerformed
        try {
            String eText = resultField.getText().trim();
            Expression e = null;
            ArrayList<Integer> childList = new ArrayList<>();
            ArrayList<int[]> parentList = new ArrayList<>();
            if (genItem.getMatchExprs().isEmpty()) { // résultat direct
                e = new Expression(eText, syntax);
                if (!genItem.getResultExprs().isEmpty()) {
                    int index = (Integer) resultRanges.getValue() - 1;
                    Result result = genItem.getResultExprs().get(index);
                    e.setType(result.getName());
                }
                exprNodes.add(new ExprNode(e, childList, parentList));
            } else if (resultReady) {
                e = exprNodes.get(exprNodes.size() - 1).getE();
            }
            if (e != null) {
                int n = exprNodes.size();
                addToDocument(exprNodes.subList(n - 1, n));
            }
        } catch (Exception ex) {
            NotifyDescriptor error = new NotifyDescriptor.Message(ex);
        }
        resultField.setText("");
    }//GEN-LAST:event_resultFieldActionPerformed

    private void updateGenerator(Generator gen) {
        ArrayList<GenItem> genItems = gen.getGenItems();
        String[] itemStrings = new String[genItems.size()];
        for (int i = 0; i < itemStrings.length; i++) {
            itemStrings[i] = genItems.get(i).getName();
        }
        genItemBox.setModel(new DefaultComboBoxModel(itemStrings));
        genItem = genItems.get(0);
        updateGenItem(genItem, 1);
        genItemBox.repaint();
        resultRanges.repaint();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton autoButton;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel commLabel;
    private javax.swing.JTextField freeTextField;
    private javax.swing.JComboBox genItemBox;
    private javax.swing.JLabel genItemNameLabel;
    private javax.swing.JComboBox generatorsBox;
    private javax.swing.JButton goButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel levelLabel;
    private javax.swing.JSpinner levelSpinner;
    private javax.swing.JRadioButton manuelButton;
    private javax.swing.JCheckBox matchCheckBox;
    private javax.swing.JSpinner nbResultSpinner;
    private javax.swing.JLabel nbResultsLbl;
    private javax.swing.JLabel rangesLabel;
    private javax.swing.JCheckBox refCheckBox;
    private javax.swing.JTable replaceMap;
    private javax.swing.JTextField resultField;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JSpinner resultRanges;
    private javax.swing.JLabel title;
    private javax.swing.JCheckBox typeCheckBox;
    private javax.swing.JTextField valueField;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JLabel varsLabel;
    private javax.swing.JTable varsTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        varsToExprs = new HashMap<>();
    }

    @Override
    public void componentClosed() {
        mdo.getMathOpenSupport().close(); // ne fonctionne pas
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

    public void setExprNodes(ArrayList<ExprNode> exprNodes) {
        this.exprNodes = exprNodes;
    }

}
