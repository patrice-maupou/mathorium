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
import org.openide.DialogDisplayer;
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
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
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
            MathDataObject mdo = (MathDataObject) MathDataObject.find(pf);
            MathOpenSupport lookup = mdo.getLookup().lookup(MathOpenSupport.class);
            lookup.createCloneableTopComponent();
            lookup.open();
            
        } catch (Exception ex) {
            NotifyDescriptor.Message m = new NotifyDescriptor.Message(ex);
            DialogDisplayer.getDefault().notify(m);
        }

    }
}
