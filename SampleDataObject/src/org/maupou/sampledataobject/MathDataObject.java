/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.sampledataobject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;

@Messages({
    "LBL_Math_LOADER=Files of Math"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_Math_LOADER",
        mimeType = "text/x-math",
        extension = {"math"}
)
@DataObject.Registration(
        mimeType = "text/x-math",
        iconBase = "org/maupou/sampledataobject/address-book-open.png",
        displayName = "#LBL_Math_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-math/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/x-math/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/x-math/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/x-math/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/x-math/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/x-math/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/x-math/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/x-math/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/x-math/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class MathDataObject extends MultiDataObject {

    private String content;
    private final Saver saver = new Saver();

    public MathDataObject(FileObject pf, MultiFileLoader loader)
            throws DataObjectExistsException, IOException {
        super(pf, loader);
        getCookieSet().add((Node.Cookie) new MathOpenSupport(getPrimaryEntry()));
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
    
    public String getContent() {
        return content;
    }

    synchronized void setContent(String text) {
    this.content = text;
    if (text != null) {
      setModified(true);
      getCookieSet().add(saver);
    } else {
      setModified(false);
      getCookieSet().remove(saver);
    }
  }

    
    private class Saver implements SaveCookie {
    @Override
    public void save() throws IOException {
      String txt;
      synchronized (MathDataObject.this) {
        //synchronize access to the content field
        txt = content;
        setContent(null);
      }
      FileObject fo = getPrimaryFile();
      String fileDisplayName = FileUtil.getFileDisplayName(fo);
      File file = new File(fileDisplayName);
      try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
        writer.print(txt);
      } 
    }
  }

}
