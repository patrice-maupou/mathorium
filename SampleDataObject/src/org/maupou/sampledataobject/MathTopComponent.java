/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.sampledataobject;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.maupou.expressions.ExprNode;
import org.maupou.expressions.Expression;
import org.maupou.expressions.GenItem;
import org.maupou.expressions.Generator;
import org.maupou.expressions.MatchExpr;
import org.maupou.expressions.Result;
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
import org.w3c.dom.Document;

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
  private ArrayList<ExprNode> exprNodes;
  private ArrayList<ExprNode> listparents;
  private HashMap<String, ArrayList<Integer>> exprPos; // nom du générateur -> positions des expressions
  private String text; // texte de mdo
  private Syntax syntax;
  private SyntaxWrite syntaxWrite;
  private ArrayList<Generator> generators;
  private Generator generator;
  private GenItem genItem;
  private boolean resultReady = false;
  private int matchRange, level;
  private int complete; // toutes les exprNodes ont été calculées jusqu'à cet entier
  private HashMap<Expression, Expression> varsToExprs;
  private MultiViewElementCallback callback;
  private final JToolBar toolbar = new JToolBar();
  private static RequestProcessor RP;
  private static final Logger log = Logger.getLogger(MathTopComponent.class.getName());

  public MathTopComponent() {
    initComponents();
    setName(Bundle.CTL_MathTopComponent());
    setToolTipText(Bundle.HINT_MathTopComponent());
    exprNodes = new ArrayList<>();
    varsToExprs = new HashMap<>();
    listparents = new ArrayList<>();
    exprPos = new HashMap<>();
    level = 1;
    complete = -1;
    RP = new RequestProcessor("Generation of expressions", 1, true);
    log.setLevel(Level.INFO);
  }

  public MathTopComponent(MathDataObject mdo) {
    this();
    init(mdo);
  }

  /**
   * lit le fichier des expressions en détectant les générateurs alimente la table mpos
   *
   * @param mdo l'objet contenant le fichier à analyser
   */
  private void init(MathDataObject mdo) {
    this.mdo = mdo;
    try {
      text = mdo.getPrimaryFile().asText();
      findSyntax(text);
      int pos = text.indexOf("</expressions>"); // fin du texte 
      boolean newtxt = !text.contains("generator") && pos != -1; // pas de générateur
      String q = String.valueOf('"');
      StringBuilder sb = new StringBuilder(text);
      if (syntax != null) {
        syntaxWrite = syntax.getSyntaxWrite();
        generators = syntax.getGenerators();
        String[] genNnames = new String[generators.size()];
        for (int i = 0; i < generators.size(); i++) {
          genNnames[i] = generators.get(i).getName();
          if (newtxt) {
            String g = "<generator name=" + q + genNnames[i] + q + ">\n</generator>\n";
            sb.insert(pos, g);
            pos += g.length();
            ArrayList<Integer> put = exprPos.put(genNnames[i], new ArrayList<>());
          }
        }
        text = sb.toString();
        generatorBox.setModel(new DefaultComboBoxModel<>(genNnames));
        if (!generators.isEmpty()) {
          generator = generators.get(0);
          updateGenerator(generator);
        }
      }
    } catch (Exception ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  private void findSyntax(String text) throws Exception {
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
  }

  /**
   * analyse le texte et charge les expressions correspondant au générateur
   *
   * @param text la chaîne à analyser
   * @param generator
   * @throws Exception
   */
  private void readExprs(String text, Generator generator) throws Exception {
    exprNodes.clear();
    String q = String.valueOf('"');
    String g = "<generator name=" + q + generator.getName() + q + ">";
    Pattern pattern = Pattern.compile(g + "(.+)</generator>", Pattern.DOTALL);
    Matcher m = pattern.matcher(text);
    if (m.find()) {
      String txt = m.group(1);
      ArrayList<Integer> ptpos = new ArrayList<>(); // positions des fins d'expressions
      exprPos.put(generator.getName(), ptpos);
      String attribs = "(|(\\s\\w+=" + q + ".+?" + q + ")+)";
      String regex = "<expr" + attribs + ">(.+?)</expr>";
      m = Pattern.compile(regex, Pattern.DOTALL).matcher(txt);
      while (m.find()) { // boucle sur les expressions  (attribs = m.group(2);)
        ArrayList<int[]> parents = new ArrayList<>();
        ArrayList<Integer> childs = new ArrayList<>();
        int end = m.end(); // position de la fin de l'expression dans txt
        String etxt = m.group(3);
        Matcher textmatcher = Pattern.compile("<text>(.+)</text>").matcher(etxt);
        if (textmatcher.find()) {
          Expression e = new Expression(textmatcher.group(1));
          textmatcher = Pattern.compile("<parents>(.+)</parents>").matcher(etxt);
          if (textmatcher.find()) {
            String[] s = textmatcher.group(1).trim().split(" ");
            for (String string : s) {
              String[] si = string.split("-");
              int[] p = new int[2];
              if (si.length == 2) {
                p[0] = Integer.parseInt(si[0]);
                p[1] = Integer.parseInt(si[1]);
                parents.add(p);
              }
            }
          }
          textmatcher = Pattern.compile("<children>(.+)</children>").matcher(etxt);
          if (textmatcher.find()) {
            String[] s = textmatcher.group(1).trim().split(" ");
            for (String string : s) {
              childs.add(Integer.parseInt(string));
            }
          }
          ExprNode en = new ExprNode(e, childs, parents);
          exprNodes.add(en);
          ptpos.add(end); // position relative
        }
      }
      updateEditor();
    }
  }

  /**
   * met texte à jour en insérant une liste d'expressions après l'expression de rang : index TODO :
   * erreur si subList comprend plusieurs éléments
   *
   * @param subList
   * @param range insère la liste après ce rang
   * @throws Exception
   */
  private void insertToText(List<ExprNode> subList, int range) throws Exception {
    StringBuilder adding = new StringBuilder();
    if (range != -1) {
      String g = "<generator name=" + '"' + generator.getName() + '"' + ">";
      Pattern pattern = Pattern.compile(g + "(.+)</generator>", Pattern.DOTALL);
      Matcher m = pattern.matcher(text);
      if (m.find()) {
        int startpos = m.start(1);
        ArrayList<Integer> poslist = exprPos.get(generator.getName());
        int shift = (range > 0 && range <= poslist.size()) ? poslist.get(range - 1) : 0;
        int pos = startpos + shift;
        int insertpos = pos;
        int rg = exprNodes.size() - subList.size(); // taille précédente
        int remainsize = rg - range; // de index+1 à rg
        for (ExprNode en : subList) {
          Expression e = en.getE();
          rg++;
          adding.append("\n<expr id=").append('"').append(rg).append('"').append(" type=");
          adding.append('"').append(e.getType()).append('"').append("><![CDATA[");
          adding.append(e.toString(syntaxWrite)).append("]]>\n<text>");
          adding.append(e.toText()).append("</text>\n");
          String parents = "", enfants = "";
          for (int[] is : en.getParentList()) {
            for (int i = 0; i < is.length; i++) {
              String sep = (i == is.length - 1) ? " " : "-";
              parents += is[i] + sep;
            }
          }
          if (!parents.isEmpty()) {
            adding.append("<parents>").append(parents).append("</parents>\n");
          }
          enfants = en.getChildList().stream().map((integer) -> integer + " ")
                  .reduce(enfants, String::concat);
          if (!enfants.isEmpty()) {
            adding.append("<children>").append(enfants).append("</children>");
          }
          adding.append("</expr>");
          pos += adding.length();
          poslist.add(rg - 1, pos - startpos);
          startpos = pos;
        }
        StringBuilder txt = new StringBuilder(text);
        text = txt.insert(insertpos, adding).toString();
        mdo.setContent(text);
        List<Integer> subposlList = poslist.subList(range, range + remainsize);
        subposlList.stream().forEach((Integer pt) -> {
          pt += adding.length();
        });
      }
    }
  }

  /**
   * enlève la chaîne de caractères de text correspondant à l'expression de rang index
   *
   * @param index le rang de l'expression à retirer du texte
   */
  private void deleteText(int index) {
    String g = "<generator name=" + '"' + generator.getName() + '"' + ">";
    Pattern pattern = Pattern.compile(g + "(.+)</generator>", Pattern.DOTALL);
    Matcher m = pattern.matcher(text);
    if (m.find()) {
      int startpos = m.start(1);
      ArrayList<Integer> poslist = exprPos.get(generator.getName());
      int endshift = poslist.get(index);
      int endpos = endshift + startpos;
      if (index > 0) { // chercher startpos
        int startshift = 0;
        for (Integer shift : poslist) {
          if (shift > startshift && shift < endshift) {
            startshift = shift;
          }
        }
        startpos += startshift;
      }
      int size = endpos - startpos;
      text = text.substring(0, startpos) + text.substring(endpos);
      poslist.remove(index);
      for (int i = 0; i < poslist.size(); i++) { // si shift > endshift, retrancher size
        Integer shift = poslist.get(i);
        if (shift > endshift) {
          poslist.set(i, shift - size);
        }
      }      
      mdo.setContent(text);
    }
  }

  /**
   * met à jour exprRange et editField
   *
   * @throws Exception
   */
  private void updateEditor() throws Exception {
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

  private void updateGenerator(Generator generator) throws Exception {
    ArrayList<GenItem> genItems = generator.getGenItems();
    String[] itemStrings = new String[genItems.size()];
    for (int i = 0; i < itemStrings.length; i++) {
      itemStrings[i] = genItems.get(i).getName();
    }
    readExprs(text, generator);
    genItemBox.setModel(new DefaultComboBoxModel(itemStrings));
    if (!genItems.isEmpty()) {
      genItem = genItems.get(0);
      updateGenItem(genItem, 1);
    }
    genItemBox.repaint();
    resultSpinner.repaint();
  }

  private void updateGenItem(GenItem genItem, int range) {
    resultTextField.setText("");
    ArrayList<Result> resultExprs = genItem.getResultExprs();
    resultSpinner.setModel(new SpinnerNumberModel(1, 1, 1, 0));
    ArrayList<MatchExpr> matchExprs = genItem.getMatchExprs();
    int n = matchExprs.size();
    HashMap<Expression, Expression> map = (n == 0) ? new HashMap() : matchExprs.get(0).getReplaceMap();
    matchRange = 0;
    fillTable(map);
    listparents.clear();
    ArrayList<Expression> vars = (n == 0)? new ArrayList<>() : matchExprs.get(0).getVars();
    int m = vars.size();
    for (int k = 0; k < varsTable.getRowCount(); k++) {
      String name = (k < m)? vars.get(k).toString() : "";
      String type = (k < m)? vars.get(k).getType() : "";
      varsTable.setValueAt(name, k, 0);
      varsTable.setValueAt("", k, 1);
      varsTable.setValueAt(type, k, 2);
    }
    if (!resultExprs.isEmpty()) {
      resultSpinner.setModel(new SpinnerNumberModel(range, 1, resultExprs.size(), 1));
      updateResult(range, resultExprs, n);
    }
    valueTextField.setText("");
    varsToExprs.clear();
    resultReady = false;
  }

  private void updateResult(int range, ArrayList<Result> resultExprs, int n) {
    if (range < resultExprs.size() + 1) {
      String result = "";
      result += resultExprs.get(range - 1);
      resultTextField.setText(result);
      if (n == 0) {
        resultTextField.requestFocus();
      } else {
        valueTextField.requestFocus();
      }
    }
  }

  /**
   * Mise à jour de la table des modèles
   *
   * @param map table des modèles et remplacements éventuels
   */
  private void fillTable(HashMap<Expression, Expression> map) {
    int rows = patternTable.getRowCount();
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
      patternTable.setValueAt(t0, row, 0);
      patternTable.setValueAt(t1, row, 1);
    }
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
        rangeResultLabel = new javax.swing.JLabel();
        resultSpinner = new javax.swing.JSpinner();
        jScrollPane2 = new javax.swing.JScrollPane();
        patternTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        varsTable = new javax.swing.JTable();
        valueLabel = new javax.swing.JLabel();
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

        org.openide.awt.Mnemonics.setLocalizedText(rangeResultLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.rangeResultLabel.text")); // NOI18N

        resultSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                resultSpinnerStateChanged(evt);
            }
        });

        patternTable.setModel(new javax.swing.table.DefaultTableModel(
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
                "Modèle", "Remplacement"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(patternTable);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.jLabel1.text")); // NOI18N

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
                "Nom", "Valeur", "Type"
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

        org.openide.awt.Mnemonics.setLocalizedText(valueLabel, org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.valueLabel.text")); // NOI18N

        valueTextField.setText(org.openide.util.NbBundle.getMessage(MathTopComponent.class, "MathTopComponent.valueTextField.text")); // NOI18N
        valueTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueTextFieldActionPerformed(evt);
            }
        });

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(generatorLabel)
                                .addGap(330, 330, 330))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(editLabel)
                                .addGap(342, 342, 342))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(exprRange, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(commentLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)
                            .addComponent(valueLabel)
                            .addComponent(resultLabel)
                            .addComponent(itemLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(commentButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(deleteButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(toValButton)
                                .addGap(319, 319, 319))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
                                    .addComponent(generatorBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.CENTER)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(genItemBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(18, 18, 18)
                                        .addComponent(rangeResultLabel)
                                        .addGap(18, 18, 18)
                                        .addComponent(resultSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(editField, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jScrollPane3)
                                    .addComponent(valueTextField)
                                    .addComponent(resultTextField)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addComponent(cntResultsLabel)
                                        .addGap(18, 18, 18)
                                        .addComponent(cntResSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(78, 78, 78)
                                        .addComponent(levelLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(levelSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(autoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(38, 38, 38))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(editLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exprRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(toValButton)
                    .addComponent(deleteButton))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(commentLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(commentButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cntResSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cntResultsLabel)
                        .addComponent(levelLabel)
                        .addComponent(levelSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(autoButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(generatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generatorBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(itemLabel)
                    .addComponent(genItemBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rangeResultLabel)
                    .addComponent(resultSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(valueLabel)
                    .addComponent(valueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resultLabel)
                    .addComponent(resultTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(72, Short.MAX_VALUE))
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
      updateGenItem(genItem, 1);
    }//GEN-LAST:event_genItemBoxActionPerformed

    private void resultTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resultTextFieldActionPerformed
      try {
        String eText = resultTextField.getText().trim();
        Expression e = null;
        ArrayList<Integer> childList = new ArrayList<>();
        ArrayList<int[]> parentList = new ArrayList<>();
        if (genItem == null || genItem.getMatchExprs().isEmpty()) { // résultat direct
          e = new Expression(eText, syntax);
          if (genItem != null && !genItem.getResultExprs().isEmpty()) {
            int index = (Integer) resultSpinner.getValue() - 1;
            Result result = genItem.getResultExprs().get(index);
            e.setType(result.getName());
          }
          ExprNode exprNode = new ExprNode(e, childList, parentList);
          exprNodes.add(exprNode);
        } else if (resultReady) {
          e = exprNodes.get(exprNodes.size() - 1).getE();
        }
        if (e != null) {
          int n = exprNodes.size();
          Integer rg = (Integer) exprRange.getValue();
          insertToText(exprNodes.subList(n - 1, n), rg); //avant rg : n - 1
          exprRange.setModel(new SpinnerNumberModel(n, 1, n, 1));
          editField.setText(e.toString(syntaxWrite));
        }
      } catch (Exception ex) {
        displayMessage("Expression non valide", "Expression error");
      }
      resultTextField.setText("");
    }//GEN-LAST:event_resultTextFieldActionPerformed

    private void valueTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueTextFieldActionPerformed
      String e = valueTextField.getText().trim();
      if (genItem != null && !genItem.getMatchExprs().isEmpty()) {
        boolean incomplete;
        try {
          MatchExpr matchExpr = genItem.getMatchExprs().get(matchRange);
          Expression expr = new Expression(e, syntax);
          ExprNode en = new ExprNode(expr, new ArrayList<>(), new ArrayList<>());
          int i = exprNodes.indexOf(en);
          en = exprNodes.get(i);
          listparents.add(en);
          en = en.copy();
          if (matchExpr.checkExpr(en.getE(), varsToExprs, genItem.getFreevars(),
                  genItem.getListvars(), syntax)) { // expression conforme au modèle
            matchRange++;
            if (incomplete = (matchRange < genItem.getMatchExprs().size())) { // modèle suivant
              matchExpr = genItem.getMatchExprs().get(matchRange);
              fillTable(matchExpr.getReplaceMap());
              valueTextField.setText("");
              Expression t = varsToExprs.get(matchExpr.getGlobal());
              if (t != null) {
                valueTextField.setText(t.toString(syntaxWrite));
                valueTextField.requestFocus();
              }
            } else { // modèles tous conformes, check results
              int index = (Integer) resultSpinner.getValue() - 1;
              Result result = genItem.getResultExprs().get(index);
              Expression t = result.applyVars(en, varsToExprs, syntax);
              ArrayList<int[]> parentList = new ArrayList<>();
              int psize = listparents.size();
              if (psize > 0) {
                int[] p = new int[psize];
                for (int k = 0; k < p.length; k++) {
                  p[k] = exprNodes.indexOf(listparents.get(k));
                }
                parentList.add(p);
              }
              en.setParentList(parentList); // en1 est null si déjà dans la liste
              resultTextField.setText(t.toString(syntaxWrite));
              resultTextField.requestFocus();
              resultReady = true;
              fillTable(new HashMap());
              valueTextField.setText("");
            }
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
            if(incomplete) {
              ArrayList<Expression> vars = matchExpr.getVars();
              for (Expression var : vars) {
                varsTable.setValueAt(var, row, 0);
                varsTable.setValueAt(var.getType(), row, 2);
              row++;
              }
            }
          }
        } catch (Exception ex) {
          Object message = ex;
          if (ex instanceof IndexOutOfBoundsException) {
            message = e + " n'est pas dans la liste";
          }
          displayMessage(message, "Expression error");
        }
      } else { // écriture directe d'une expression quelconque
        resultTextField.setText(e);
        resultTextField.requestFocus();
      }
    }//GEN-LAST:event_valueTextFieldActionPerformed

    private void toValButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toValButtonActionPerformed
      valueTextField.setText(editField.getText());
      valueTextField.requestFocus();
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
          int oldsize, s0;
          //int limit = (int) cntResSpinner.getValue();
          ArrayList<ExprNode> exprDiscards = new ArrayList<>();
          boolean once = true;
          do {
            oldsize = exprNodes.size();
            s0 = oldsize;
            for (GenItem genItem : generator.getGenItems()) {
              ArrayList<MatchExpr> matchExprs = genItem.getMatchExprs();
              if (genItem.getResultExprs().isEmpty()) {
                continue;
              }
              int matchsize = matchExprs.size();
              int[] genpList = new int[matchsize];
              HashMap<Expression, Expression> evars = new HashMap<>();
              ArrayList<HashMap<Expression, Expression>> mvars = new ArrayList<>();
              for (int i = 0; i < matchsize; i++) {
                mvars.add(evars);
              }
              ArrayList<Integer> childList = new ArrayList<>();
              ArrayList<int[]> parentList = new ArrayList<>();
              parentList.add(genpList);
              HashMap<Integer, Integer> rangsEN = new HashMap<>();
              int m = 0, i = 0; // rangs de matchexpr et exprNodes
              do {
                try {
                  ExprNode en = new ExprNode(null, childList, parentList);
                  rangsEN.put(m, i);
                  HashMap<Expression, Expression> vars = new HashMap<>();
                  vars.putAll(evars);
                  if (matchExprs.isEmpty() && once) {
                    genItem.addResults(en, vars, syntax, level, exprNodes, exprDiscards);
                    updateEditor();
                    insertToText(exprNodes, 0);
                    oldsize = exprNodes.size();
                    break;
                  } else { // TODO : test sur rangsEN si m == matchsize - 1
                    en = genItem.genapply(level, m, i, syntax, en, vars, exprNodes);
                  }
                  if (m == matchsize - 1 || en == null) { // fin des tests
                    if (en != null) {
                      int n0 = exprNodes.size();
                      genItem.addResults(en, vars, syntax, level, exprNodes, exprDiscards);
                      updateEditor();
                      int n = exprNodes.size();
                      insertToText(exprNodes.subList(n0, n), n0);
                    }
                    while (i >= oldsize - 1 && m > -1) { // revenir en arrière
                      m--; // evars doit changer
                      if (m > -1) {
                        i = rangsEN.get(m);
                        evars = (m == 0) ? new HashMap<>() : mvars.get(m - 1);
                      }
                    }
                    i++;
                  } else { // match suivant
                    evars.putAll(vars);
                    mvars.set(m, evars);
                    m++;
                    i = 0;
                  }
                } catch (Exception ex) {
                  StringWriter sw = new StringWriter();
                  PrintWriter pw = new PrintWriter(sw);
                  ex.printStackTrace(pw);
                  String s = sw.toString();
                  NotifyDescriptor.Message d = new NotifyDescriptor.Message(s);
                  DialogDisplayer.getDefault().notify(d);
                  return;
                }
                if (Thread.interrupted()) {
                  return;
                }
              } while (m > -1);
            }
            once = false;
          } while (oldsize < exprNodes.size());
          //System.out.println("sortie normale");
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
        ExprNode remove = exprNodes.remove(index);
        try {
          updateEditor();
        } catch (Exception ex) {
          Exceptions.printStackTrace(ex);
        }
        deleteText(index);
      }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void resultSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_resultSpinnerStateChanged
      Integer range = (Integer) resultSpinner.getValue();
      ArrayList<Result> results = genItem.getResultExprs();
      updateResult(range, results, genItem.getMatchExprs().size());
    }//GEN-LAST:event_resultSpinnerStateChanged

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
    private javax.swing.JLabel itemLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel levelLabel;
    private javax.swing.JSpinner levelSpinner;
    private javax.swing.JTable patternTable;
    private javax.swing.JLabel rangeResultLabel;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JSpinner resultSpinner;
    private javax.swing.JTextField resultTextField;
    private javax.swing.JButton toValButton;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JTextField valueTextField;
    private javax.swing.JTable varsTable;
    // End of variables declaration//GEN-END:variables
    @Override
  public void componentOpened() {
    // TODO add custom code on component opening
  }

  @Override
  public void componentClosed() {
    // TODO add custom code on component closing
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
