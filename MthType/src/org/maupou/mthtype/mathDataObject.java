/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.mthtype;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.maupou.expressions.Syntax;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.NotifyDescriptor;
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
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Messages({
    "LBL_math_LOADER=Files of math"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_math_LOADER",
        mimeType = "text/x-math+xml",
        extension = {"math"}
)
@DataObject.Registration(
        mimeType = "text/x-math+xml",
        iconBase = "org/maupou/mthtype/loupesn.gif",
        displayName = "#LBL_math_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-math+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/x-math+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/x-math+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/x-math+xml/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/x-math+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/x-math+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/x-math+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/x-math+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/x-math+xml/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class mathDataObject extends MultiDataObject {
    private Syntax syntax;
    private Document document;

    public mathDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor("text/x-math+xml", true);
        CookieSet cookies = getCookieSet();
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream is = pf.getInputStream();
            document = documentBuilder.parse(is);
            if (document != null) {
                Element root = document.getDocumentElement();
                String path = root.getAttribute("syntax");
                if (!path.isEmpty()) {
                    File syntaxFile = new File(path);
                    Document syxdoc = documentBuilder.parse(syntaxFile);
                    syntax = new Syntax(syxdoc);
                    syntax.addGenerators(syxdoc);
                    cookies.add(new MathOpenSupport(getPrimaryEntry()));
                }
            }
        } catch (Exception ex) {
            NotifyDescriptor error = new NotifyDescriptor.Message(ex);
        }
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @MultiViewElement.Registration(
            displayName = "#LBL_math_EDITOR",
            iconBase = "org/maupou/mthtype/loupesn.gif",
            mimeType = "text/x-math+xml",
            persistenceType = TopComponent.PERSISTENCE_NEVER,
            preferredID = "math",
            position = 1000
    )
    @Messages("LBL_math_EDITOR=Source")
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        MultiViewEditorElement multiViewEditor = new MultiViewEditorElement(lkp);
        return multiViewEditor;
    }

    public Syntax getSyntax() {
        return syntax;
    }

    public Document getDocument() {
        return document;
    }

}
