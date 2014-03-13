/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.mthtype;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.maupou.expressions.Syntax;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.maupou.syntaxtype.syxDataObject;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@ActionID(
        category = "Edit",
        id = "org.maupou.mthtype.syxAction")
@ActionRegistration(
        displayName = "#CTL_syxAction")
@ActionReference(path = "Loaders/text/x-syx+xml/Actions", position = 200)
@Messages("CTL_syxAction=New math File")
public final class syxAction implements ActionListener {

    private final syxDataObject syxObj;

    public syxAction(syxDataObject syxObj) {
        this.syxObj = syxObj;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            FileObject fo = syxObj.getPrimaryFile();
            String path = fo.getPath();
            File syntaxFile = new File(path);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document syxdoc = documentBuilder.parse(syntaxFile);
            Syntax syntax = new Syntax(syxdoc);
            syntax.addGenerators(syxdoc);
            Document document = documentBuilder.newDocument();
            document.setXmlVersion("1.0");
            Element root = document.createElement("expressions");
            document.appendChild(root);
            root.setAttribute("syntax", path);
            
            FileChooserBuilder fcb = new FileChooserBuilder("user-dir").setTitle("New File");
            fcb.setFileFilter(new FileNameExtensionFilter("math files", "math"));
            File file = fcb.showSaveDialog();
            Result result = new StreamResult(file);
            Source source = new DOMSource(document);
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(source, result);
            FileObject pf = FileUtil.createData(file);
            mathDataObject mdo = (mathDataObject) mathDataObject.find(pf);
            MathOpenSupport lookup = mdo.getLookup().lookup(MathOpenSupport.class);
            lookup.createCloneableTopComponent();
            lookup.open();
            
        } catch (Exception ex) {
            NotifyDescriptor.Message m = new NotifyDescriptor.Message(ex);
        }

    }
}
