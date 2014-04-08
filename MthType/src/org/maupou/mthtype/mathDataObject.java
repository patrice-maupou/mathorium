/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.mthtype;

import java.io.IOException;
import org.maupou.expressions.Syntax;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;

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
    private final MathOpenSupport mathOpenSupport;

    public mathDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, 
            IOException {
        super(pf, loader);
        registerEditor("text/x-math+xml", true);
        CookieSet cookies = getCookieSet();
        mathOpenSupport = new MathOpenSupport(getPrimaryEntry());
        cookies.assign(OpenCookie.class, mathOpenSupport);
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
        return new MultiViewEditorElement(lkp);
    }
  

    public Syntax getSyntax() {
        return syntax;
    }
    
    public void setSyntax(Syntax syntax) {
        this.syntax = syntax;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
    
    public MathOpenSupport getMathOpenSupport() {
        return mathOpenSupport;
    }


}
