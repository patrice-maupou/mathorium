/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.mthtype;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.maupou.expressions.Syntax;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@Messages({
    "LBL_mth_LOADER=Files of mth"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_mth_LOADER",
        mimeType = "text/x-mth+xml",
        extension = {"mth"})
@DataObject.Registration(
        mimeType = "text/x-mth+xml",
        iconBase = "org/maupou/mthtype/loupesn.gif",
        displayName = "#LBL_mth_LOADER",
        position = 300)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-mth+xml/Actions",
            id =
            @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200),
    @ActionReference(
            path = "Loaders/text/x-mth+xml/Actions",
            id =
            @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300),
    @ActionReference(
            path = "Loaders/text/x-mth+xml/Actions",
            id =
            @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500),
    @ActionReference(
            path = "Loaders/text/x-mth+xml/Actions",
            id =
            @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600),
    @ActionReference(
            path = "Loaders/text/x-mth+xml/Actions",
            id =
            @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800),
    @ActionReference(
            path = "Loaders/text/x-mth+xml/Actions",
            id =
            @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000),
    @ActionReference(
            path = "Loaders/text/x-mth+xml/Actions",
            id =
            @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200),
    @ActionReference(
            path = "Loaders/text/x-mth+xml/Actions",
            id =
            @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300),
    @ActionReference(
            path = "Loaders/text/x-mth+xml/Actions",
            id =
            @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400)
})
public class MthDataObject extends MultiDataObject {

    private Syntax syntax;
    private Document document;

    public MthDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        //registerEditor("text/x-mth+xml", false);
        CookieSet cookies = getCookieSet();
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream is = pf.getInputStream();
            document = documentBuilder.parse(is);
            if (document != null) {
                Element root = document.getDocumentElement();
                String path = root.getAttribute("syntax");
                if (path.isEmpty()) {
                } else {
                    File syntaxFile = new File(path);
                    Document syxdoc = documentBuilder.parse(syntaxFile);
                    syntax = new Syntax(syxdoc);
                    syntax.addGenerators(syxdoc);
                    cookies.add(new MthOpenSupport(getPrimaryEntry()));
                }
            }
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    /**
     * @return the syntax
     */
    public Syntax getSyntax() {
        return syntax;
    }

    /**
     * @return the document
     */
    public Document getDocument() {
        return document;
    }
}
