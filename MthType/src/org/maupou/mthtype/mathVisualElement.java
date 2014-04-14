/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.mthtype;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.text.DataEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.w3c.dom.Element;

@MultiViewElement.Registration(
        displayName = "#LBL_math_VISUAL",
        iconBase = "org/maupou/mthtype/loupesn.gif",
        mimeType = "text/x-math+xml",
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "mathVisual",
        position = 2000
)
@Messages("LBL_math_VISUAL=Visual")
public final class mathVisualElement extends JPanel implements MultiViewElement {

    private final mathDataObject mdo;
    private final JToolBar toolbar = new JToolBar();
    private transient MultiViewElementCallback callback;
    private StyledDocument styledDocument;
    private DocumentListener documentListener;

    public mathVisualElement(Lookup lkp) {
        mdo = lkp.lookup(mathDataObject.class);
        assert mdo != null;
        initComponents();
        DataEditorSupport dataEditorSupport = mdo.getLookup().lookup(DataEditorSupport.class);
        if (dataEditorSupport != null) {
            styledDocument = dataEditorSupport.getDocument();
            documentListener = new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    textPane.setText(updateText(styledDocument));
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    textPane.setText(updateText(styledDocument));
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }
            };
            styledDocument.addDocumentListener(documentListener);
        }
    }

    /**
     * réécrit le texte d'après le StyledDocument
     *
     * @param doc le document de référence pour l'édition
     */
    private static String updateText(StyledDocument doc) {
        String text = "";
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException ex) {
        }
        StringBuilder output = new StringBuilder();
        String[] result = readTag("expressions", text, null);
        if (!result[1].isEmpty()) { // texte intérieur
            text = result[1];
            while (!text.isEmpty()) {
                result = readTag("generator", text, null);
                HashMap<String, String> attrs = readAttributes(result[0]);
                String gname = attrs.get("name");
                if (gname != null) {
                    output.append("<h1>").append(gname).append("</h1>");
                    parse(result[1], output);
                }
                text = result[2]; // texte restant
            }
        }
        return output.toString();
    }

    /**
     * extrait et traite les expressions d'un generator
     *
     * @param text chaîne à analyser
     * @param output chaîne de sortie
     */
    private static void parse(String text, StringBuilder output) {
        String[] result;
        while (!text.isEmpty()) {
            result = readTag("expr", text, null);
            String[] tag = readTag("comment", result[1], null);
            if (!tag[1].isEmpty()) {
                output.append("<div>").append(tag[1]).append("</div>");
            }
            readCDATA(result[1], output);
            text = result[2];
        }
    }

    /**
     * Recherche la première balise de la chaîne text ayant pour nom tag
     *
     * @param tag le nom de la balise
     * @param text la partie du texte à analyser
     * @param specific liste particulière d'attributs
     * @return liste de 3 chaînes : les attributs, l'intérieur, et le reste
     */
    public static String[] readTag(String tag, String text, String specific) {
        String[] result = new String[]{"", "", ""};
        String attribs = "(|(\\s\\w+=\\u0022.+?\\u0022)+)";
        if (specific != null) {
            attribs = "(\\s" + specific + ")";
        }
        String regex = "<" + tag + attribs + ">(.+?)</" + tag + ">";
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);
        Matcher m = p.matcher(text);
        if (m.find()) {
            if (specific == null) {
                result[0] = m.group(2);
                result[1] = m.group(3);
            } else {
                result[0] = specific;
                result[1] = m.group(2);
            }
            if (m.end() < text.length()) {
                result[2] = text.substring(m.end());
            }
        }
        return result;
    }

    /**
     *
     * @param text chaîne de caractères définissant des attributs
     * @return table des attributs
     */
    public static HashMap<String, String> readAttributes(String text) {
        HashMap<String, String> map = new HashMap<>();
        String[] params = text.split("\\s");
        for (String param : params) {
            String[] entry = param.split("=");
            if (entry.length == 2) {
                map.put(entry[0], entry[1].substring(1, entry[1].length() - 1));
            }
        }
        return map;
    }

    /**
     * lit le contenu du segment CDATA et l'écrit avec la balise "var" dans
     * output
     *
     * @param text
     * @param output
     */
    private static void readCDATA(String text, StringBuilder output) {
        int start = text.indexOf("[CDATA[");
        if (start != -1) {
            start += 7;
            int end = text.indexOf("]]", start);
            if (end != -1) {
                output.append("<var>").append(text.substring(start, end)).append("</var><br>");
            }
        }
    }

    @Override
    public String getName() {
        return "mathVisualElement";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        textPane = new javax.swing.JTextPane();

        setLayout(new java.awt.BorderLayout());

        scrollPane.setViewportView(textPane);

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextPane textPane;
    // End of variables declaration//GEN-END:variables
    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
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
    public void componentOpened() {
        HTMLEditorKit kit = new HTMLEditorKit();
        textPane.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        try {
            Position end = styledDocument.getEndPosition();
            String text = styledDocument.getText(0, end.getOffset());
            Matcher matcher = Pattern.compile("syntax=\\u0022(.+)\\u0022>").matcher(text);
            if (matcher.find()) {
                String syntaxPath = matcher.group(1);
                String path = (new File(syntaxPath)).getParent() + "/mth.css";
                styleSheet.importStyleSheet(new URL("file:/" + path));
                kit.setStyleSheet(styleSheet);
            }
        } catch (BadLocationException | MalformedURLException ex) {
            styleSheet.addRule("var {color:blue; font-size:12; font-style:normal; margin: 4px; }");
            styleSheet.addRule("div {color:grey; font-size:12; font-style:italic; }");
            Exceptions.printStackTrace(ex);
        }
        textPane.setText(updateText(styledDocument));
        requestFocus();
    }

    @Override
    public void componentClosed() {
        mdo.getMathOpenSupport().close();
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

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

}
