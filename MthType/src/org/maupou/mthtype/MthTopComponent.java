/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.mthtype;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.text.Style;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.maupou.expressions.ExprNode;
import org.maupou.expressions.Expression;
import org.maupou.expressions.Syntax;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableTopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.maupou.mthtype//Mth//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "MthTopComponent",
        iconBase = "org/maupou/mthtype/loupesn.gif",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "org.maupou.mthtype.MthTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MthAction",
        preferredID = "MthTopComponent")
@Messages({
    "CTL_MthAction=Mth",
    "CTL_MthTopComponent=Mth Window",
    "HINT_MthTopComponent=This is a Mth window"
})
public final class MthTopComponent extends CloneableTopComponent {

    private ArrayList<ExprNode> exprNodes;
    private Syntax syntax;
    private Document document;
    Style commentStyle, exprStyle;

    public MthTopComponent() {
        initComponents();
        //setName(Bundle.CTL_MthTopComponent());
        //setToolTipText(Bundle.HINT_MthTopComponent());
    }

    public MthTopComponent(Syntax syntax, Document document) {
        this();
        this.syntax = syntax;
        this.document = document;
        exprNodes = new ArrayList<>();
        /*  liste des expressions du document
        NodeList nodes = document.getElementsByTagName("expr");
        for (int i = 0; i < nodes.getLength(); i++) {
            try {
                Element e = (Element) nodes.item(i);
                String type = e.getAttribute("type");
                String etext = e.getFirstChild().getTextContent();
                Expression expr = new Expression(etext, syntax);
                expr.setType(type);
                // parents
                ArrayList<int[]> parents = new ArrayList<>();
                NodeList nl = e.getElementsByTagName("parents");
                if (nl.getLength() == 1) {
                    Element ep = (Element) nl.item(0);
                    String ps = ep.getFirstChild().getTextContent();
                    if (!ps.isEmpty()) {
                        String[] s = ps.split(" ");
                        for (int j = 0; j < s.length; j++) {
                            String[] sp = s[j].split("-");
                            int[] p = new int[sp.length];
                            for (int k = 0; k < sp.length; k++) {
                                p[k] = Integer.parseInt(sp[k]);
                            }
                            parents.add(p);
                        }
                    }
                }

                // enfants
                ArrayList<Integer> children = new ArrayList<>();
                nl = e.getElementsByTagName("children");
                if (nl.getLength() == 1) {
                    Element ep = (Element) nl.item(0);
                    String childString = ep.getFirstChild().getTextContent();
                    if (!childString.isEmpty()) {
                        String[] s = childString.split(" ");
                        for (int j = 0; j < s.length; j++) {
                            children.add(Integer.parseInt(s[j]));
                        }
                    }
                }
                ExprNode en = new ExprNode(expr, children, parents, null);
                exprNodes.add(en);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }        
        //*/
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane = new javax.swing.JScrollPane();
        textPane = new javax.swing.JTextPane();

        setLayout(new java.awt.BorderLayout());

        jScrollPane.setViewportView(textPane);

        add(jScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTextPane textPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        if (syntax == null) {
            return;
        }
        StringBuilder output = new StringBuilder();
        Element root = getDocument().getDocumentElement();
        HTMLEditorKit kit = new HTMLEditorKit();
        textPane.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        try {
            NodeList nl = getDocument().getElementsByTagName("expressions");
            Element e = (Element) nl.item(0);
            String syntaxPath = e.getAttribute("syntax");
            String path = (new File(syntaxPath)).getParent() + "/mth.css";
            styleSheet.importStyleSheet(new URL("file:/" + path));
        } catch (Exception ex) {
            styleSheet.addRule("var {color:blue; font-size:12; font-style:normal; margin: 4px; }");
            styleSheet.addRule("div {color:grey; font-size:12; font-style:italic; }");
            Exceptions.printStackTrace(ex);
        }
        exprNodes = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("expr");
        for (int i = 0; i < nodes.getLength(); i++) {
            try {
                String childString;
                Element e = (Element) nodes.item(i);
                boolean show = ("no".equals(e.getAttribute("show")))? false : true;
                String type = e.getAttribute("type");
                String etext = e.getFirstChild().getTextContent();
                Expression expr = new Expression(etext, syntax);
                expr.setType(type);
                // parents
                ArrayList<int[]> parents = new ArrayList<>();
                NodeList nl = e.getElementsByTagName("parents");
                if (nl.getLength() == 1) {
                    Element ep = (Element) nl.item(0);
                    String ps = ep.getFirstChild().getTextContent();
                    if (!ps.isEmpty()) {
                        String[] s = ps.split(" ");
                        for (int j = 0; j < s.length; j++) {
                            String[] sp = s[j].split("-");
                            int[] p = new int[sp.length];
                            for (int k = 0; k < sp.length; k++) {
                                p[k] = Integer.parseInt(sp[k]);
                            }
                            parents.add(p);
                        }
                    }
                }
                // enfants
                ArrayList<Integer> children = new ArrayList<>();
                nl = e.getElementsByTagName("children");
                if (nl.getLength() == 1) {
                    Element ep = (Element) nl.item(0);
                    childString = ep.getFirstChild().getTextContent();
                    if (!childString.isEmpty()) {
                        String[] s = childString.split(" ");
                        for (int j = 0; j < s.length; j++) {
                            children.add(Integer.parseInt(s[j]));
                        }
                    }
                }
                // commentaires
                String comment = "";
                nl = e.getElementsByTagName("comment");
                for (int j = 0; j < nl.getLength(); j++) {
                    Element c = (Element) nl.item(j);
                    comment += c.getTextContent();
                }
                if (show) {
                    output.append("<p>");
                    if (!comment.trim().isEmpty()) {
                        output.append("<div>").append(comment).append("</div><br>");
                    }
                    output.append("<var>").append(etext).append("</var>");
                    output.append("</p>");
                }
                ExprNode en = new ExprNode(expr, children, parents);
                en.setVisible(show);
                exprNodes.add(en);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        textPane.setText(output.toString());
        /*
         ElementIterator iterator = new ElementIterator(textPane.getDocument());
         javax.swing.text.Element element;
         while ((element = iterator.next()) != null) {
         AttributeSet attributes = element.getAttributes();
         Object name = attributes.getAttribute(StyleConstants.NameAttribute);
         if(name == HTML.Tag.BODY) {
         break;
         }
         }
         //*/
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

    /**
     * @return the exprNodes
     */
    public ArrayList<ExprNode> getExprNodes() {
        return exprNodes;
    }

    public javax.swing.JTextPane getTextPane() {
        return textPane;
    }

    /**
     * @return the document
     */
    public Document getDocument() {
        return document;
    }
}
