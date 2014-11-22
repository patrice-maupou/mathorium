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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.maupou.expressions.*;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;

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
    private boolean resultReady, listen;
    private int level, limit, matchRange;
    private StyledDocument styleDocument;
    private DocumentListener dl;
    private final SpinnerNumberModel model;

    public GeneratorViewTopComponent() {
        this.model = new SpinnerNumberModel(0, 0, 0, 1);
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
        listen = true;
        try {
            styleDocument = mdo.getStyledDocument();
            dl = new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent de) {
                    if (listen) {
                        docToExprs(generator);
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent de) {
                    if (listen) {
                        docToExprs(generator);
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent de) {
                }
            };
            styleDocument.addDocumentListener(dl);
            findSyntax();
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
            }
        } catch (IOException ex) {
            
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * le styleDocument fournit la syntaxe
     *
     * @throws Exception
     */
    private void findSyntax() {
        try {
            Position end = styleDocument.getEndPosition();
            String text = styleDocument.getText(0, end.getOffset());
            String q = String.valueOf('"');
            Matcher matcher = Pattern.compile("syntax=" + q + "(.+)" + q).matcher(text);
            if (matcher.find()) {
                String path = matcher.group(1);
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                if (!path.isEmpty()) {
                    File syntaxFile = new File(path);
                    Document syxdoc = documentBuilder.parse(syntaxFile);
                    syntax = new Syntax(syxdoc);
                    syntax.addGenerators(syxdoc);
                }
            }
        } catch (Exception exception) {
            syntax = null;
        }
    }

    /**
     * parcourt le styledDocument et met à jour ce composant
     *
     * @param generator l'objet courant
     */
    private void docToExprs(Generator generator) {
        exprNodes.clear();
        if (generator == null) {
            return;
        }
        try {
            Position end = styleDocument.getEndPosition();
            String text = styleDocument.getText(0, end.getOffset());
            String specific = "name=\\u0022" + generator.getName() + "\\u0022";
            String[] elem = mathVisualElement.readTag("generator", text, specific);
            text = elem[1];
            while (!text.isEmpty()) {
                elem = mathVisualElement.readTag("expr", text, null);
                ArrayList<int[]> parents = new ArrayList<>();
                ArrayList<Integer> childs = new ArrayList<>();
                String inner = elem[1];
                text = elem[2]; // le reste
                if (!inner.isEmpty()) {
                    elem = mathVisualElement.readTag("text", inner, null);
                    Expression e = new Expression(elem[1]);
                    elem = mathVisualElement.readTag("parents", inner, null);
                    if (!elem[1].isEmpty()) {
                        String[] s = elem[1].trim().split(" ");
                        for (String string : s) {
                            String[] si = string.split("-");
                            int[] p = new int[2];
                            if (si.length == 2) {
                                p[0] = Integer.parseInt(si[0]); // exception avec [CDATA
                                p[1] = Integer.parseInt(si[1]);
                                parents.add(p);
                            }
                        }
                    }
                    elem = mathVisualElement.readTag("children", inner, null);
                    if (!elem[1].isEmpty()) {
                        String[] s = elem[1].trim().split(" ");
                        for (String string : s) {
                            childs.add(Integer.parseInt(string));
                        }
                    }
                    exprNodes.add(new ExprNode(e, childs, parents));
                }
            }
            updateEditor();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * met à jour l'éditeur et le spinneur
     */
    private void updateEditor() {
        int max = exprNodes.size();
        int min = (max == 0) ? 0 : 1;
        model.setValue(max);
        model.setMaximum(max);
        model.setMinimum(min);
        //exprRange.setModel(model);
        try {
            if (max > 0) {
                ExprNode en = exprNodes.get(max - 1);
                editorPane.setText(en.getE().toString(syntaxWrite));
            } else {
                editorPane.setText("");
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
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
        editLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        replaceMap = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JTextPane();
        exprRange = new javax.swing.JSpinner();
        toValue = new javax.swing.JButton();

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

        org.openide.awt.Mnemonics.setLocalizedText(editLabel, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.editLabel.text")); // NOI18N

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

        jScrollPane3.setViewportView(editorPane);

        exprRange.setModel(model);
        exprRange.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                exprRangeStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(toValue, org.openide.util.NbBundle.getMessage(GeneratorViewTopComponent.class, "GeneratorViewTopComponent.toValue.text")); // NOI18N
        toValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toValueActionPerformed(evt);
            }
        });

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
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(resultLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(resultField, javax.swing.GroupLayout.PREFERRED_SIZE, 462, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(valueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(valueField, javax.swing.GroupLayout.PREFERRED_SIZE, 462, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addContainerGap(19, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(toValue, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                            .addComponent(exprRange))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane3)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(257, 257, 257)
                .addComponent(editLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(editLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(exprRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(toValue))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                } else {
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
     *
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
                } catch (Exception exc) {
                }
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
                          valueField.setText("");
                          Expression t = varsToExprs.get(matchExpr.getGlobal());
                          if (t != null) {
                              valueField.setText(t.toString(syntaxWrite));
                              valueField.requestFocus();
                          }
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
                          valueField.setText("");
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
                  } else {
                      displayWarning("non conforme au modèle", NotifyDescriptor.INFORMATION_MESSAGE);
                  }
              } else {
                  displayWarning(e + " n'est pas dans la liste", NotifyDescriptor.INFORMATION_MESSAGE);
              }
          } catch (Exception ex) {
              displayWarning(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
          }
      } else {
          resultField.setText(e);
          resultField.requestFocus();
      }
  }//GEN-LAST:event_valueFieldActionPerformed

    /**
     * affiche un panel avec un message
     *
     * @param message
     * @param type
     */
    private void displayWarning(String message, int type) {
        NotifyDescriptor warning = new NotifyDescriptor.Message(message, type);
        DialogDisplayer.getDefault().notify(warning);
    }

    /**
     * ajoute une liste d'expression à styleDocument
     *
     * @param newExprs expressions à ajouter
     * @throws Exception
     */
    public void addToStyledDocument(List<ExprNode> newExprs) throws Exception {
        AttributeSet as = styleDocument.getDefaultRootElement().getAttributes();
        Position end = styleDocument.getEndPosition();
        String text = styleDocument.getText(0, end.getOffset());
        if (text.trim().endsWith("/>")) {
            int n = text.lastIndexOf("/>");
            styleDocument.insertString(n + 1, "expressions", as);
            styleDocument.insertString(n, ">\n<", as);
            end = styleDocument.getEndPosition();
            text = styleDocument.getText(0, end.getOffset());
        }
        String q = String.valueOf('"');
        Matcher matcher = Pattern.compile("<expressions.+>").matcher(text);
        if (matcher.find()) {
            int start = matcher.end();
            text = text.substring(start);
            String specific = "<generator name=" + q + generator.getName() + q + ">";
            Matcher m = Pattern.compile(specific).matcher(text);
            if (m.find()) {
                start += m.end();
                m = Pattern.compile("\n</generator>").matcher(text.substring(m.end()));
                if (m.find()) {
                    start += m.start();
                }
            } else { // ajouter l'élément
                String gentxt = "\n" + specific + "\n</generator>";
                styleDocument.insertString(start, gentxt, as);
                start += specific.length() + 1;
            }
            for (ExprNode exprNode : newExprs) {
                String parents = "", enfants = "";
                for (int[] is : exprNode.getParentList()) {
                    for (int i = 0; i < is.length; i++) {
                        String last = (i == is.length - 1) ? " " : "-";
                        parents += is[i] + last;
                    }
                }
                for (Integer integer : exprNode.getChildList()) {
                    enfants += integer + " ";
                }
                Expression e = exprNode.getE();
                String ewr = e.toString(syntaxWrite);
                String type = e.getType();
                int index = exprNodes.indexOf(exprNode);
                String elem = "\n<expr id=" + q + index + q + " type=" + q + e.getType() + q + ">";
                elem += "<![CDATA[" + ewr + "]]>" + "\n<text>" + e.toText() + "</text>";
                if (!parents.isEmpty()) {
                    elem += "\n<parents>" + parents.trim() + "</parents>";
                }
                if (!enfants.isEmpty()) {
                    elem += "\n<children>" + enfants.trim() + "</children>";
                }
                elem += "\n</expr>";
                styleDocument.insertString(start, elem, as);
                start += elem.length();
            }
        }
    }

    /**
     * ajoute les expressions cachées à la liste exrpDiscards
     *
     * @param exprNode
     * @throws Exception
     */
    /*
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
     //*/

    private void resultRangesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_resultRangesStateChanged
        int range = (Integer) resultRanges.getValue();
        updateGenItem(genItem, range);
        if (genItem.getMatchExprs().isEmpty()) {
            resultField.requestFocus();
        } else {
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
                    listen = false;
                    level = 1;
                    boolean once = true;
                    int oldsize = exprNodes.size();
                    do {
                        for (GenItem genItem : generator.getGenItems()) {
                            if ((!once && genItem.getMatchExprs().isEmpty())
                                    || genItem.getResultExprs().isEmpty()) {
                                continue;
                            }
                            oldsize = exprNodes.size();
                            int matchsize = genItem.getMatchExprs().size();
                            int[] genpList = new int[matchsize];
                            HashMap<Expression, Expression> evars = new HashMap<>();
                            ArrayList<Integer> childList = new ArrayList<>();
                            ArrayList<int[]> parentList = new ArrayList<>();
                            parentList.add(genpList);
                            ExprNode en = new ExprNode(null, childList, parentList);
                            genItem.generate(0, limit, level, syntax, en, evars, exprNodes, exrpDiscards);
                            List<ExprNode> newExprs = exprNodes.subList(oldsize, exprNodes.size());
                            addToStyledDocument(newExprs);
                        }
                        once = false;
                    } while (exprNodes.size() < limit && oldsize < exprNodes.size());
                    goButton.setText("execute");
                    updateEditor();
                    listen = true;
                    return exprNodes;
                }
            };
            worker.execute();
        }
    }//GEN-LAST:event_goButtonActionPerformed

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
                listen = false;
                addToStyledDocument(exprNodes.subList(n - 1, n));
                exprRange.setModel(new SpinnerNumberModel(n, 1, n, 1));
                editorPane.setText(e.toString(syntaxWrite));
            }
        } catch (Exception ex) {
            displayWarning(ex.toString(), NotifyDescriptor.ERROR_MESSAGE);
        }
        listen = true;
        resultField.setText("");
    }//GEN-LAST:event_resultFieldActionPerformed

    private void exprRangeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_exprRangeStateChanged
        Integer range = (Integer) exprRange.getValue();
        try {
            editorPane.setText(exprNodes.get(range - 1).getE().toString(syntaxWrite));
        } catch (Exception ex) {
            NotifyDescriptor error = new NotifyDescriptor.Message(ex);
        }
    }//GEN-LAST:event_exprRangeStateChanged

    private void toValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toValueActionPerformed
        valueField.setText(editorPane.getText());
        valueField.requestFocus();
    }//GEN-LAST:event_toValueActionPerformed

    private void updateGenerator(Generator gen) {
        exprNodes = new ArrayList<>();
        ArrayList<GenItem> genItems = gen.getGenItems();
        String[] itemStrings = new String[genItems.size()];
        for (int i = 0; i < itemStrings.length; i++) {
            itemStrings[i] = genItems.get(i).getName();
        }
        docToExprs(generator);
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
    private javax.swing.JLabel editLabel;
    private javax.swing.JTextPane editorPane;
    private javax.swing.JSpinner exprRange;
    private javax.swing.JComboBox genItemBox;
    private javax.swing.JLabel genItemNameLabel;
    private javax.swing.JComboBox generatorsBox;
    private javax.swing.JButton goButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
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
    private javax.swing.JButton toValue;
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
        mdo.getMathOpenSupport().close();
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
