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
package org.maupou.syntaxtype;

import java.io.IOException;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
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
import org.openide.util.NbBundle.Messages;
import org.xml.sax.InputSource;

/**
 *
 * @author Patrice
 */
@Messages({
  "LBL_syx_LOADER=Files of syx"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_syx_LOADER",
        mimeType = "text/x-syx+xml",
        extension = {"syx"})
@DataObject.Registration(
        mimeType = "text/x-syx+xml",
        iconBase = "org/maupou/syntaxtype/syx.gif",
        displayName = "#LBL_syx_LOADER",
        position = 300)
@ActionReferences({
  @ActionReference(
          path = "Loaders/text/x-syx+xml/Actions",
          id
          = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
          position = 0,
          separatorAfter = 200),
  @ActionReference(
          path = "Loaders/text/x-syx+xml/Actions",
          id
          = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
          position = 300),
  @ActionReference(
          path = "Loaders/text/x-syx+xml/Actions",
          id
          = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
          position = 400,
          separatorAfter = 500),
  @ActionReference(
          path = "Loaders/text/x-syx+xml/Actions",
          id
          = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
          position = 600),
  @ActionReference(
          path = "Loaders/text/x-syx+xml/Actions",
          id
          = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
          position = 700,
          separatorAfter = 800),
  @ActionReference(
          path = "Loaders/text/x-syx+xml/Actions",
          id
          = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
          position = 900,
          separatorAfter = 1000),
  @ActionReference(
          path = "Loaders/text/x-syx+xml/Actions",
          id
          = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
          position = 1100,
          separatorAfter = 1200),
  @ActionReference(
          path = "Loaders/text/x-syx+xml/Actions",
          id
          = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
          position = 1300),
  @ActionReference(
          path = "Loaders/text/x-syx+xml/Actions",
          id
          = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
          position = 1400)
})

public class syxDataObject extends MultiDataObject {

  public syxDataObject(FileObject pf, MultiFileLoader loader)
          throws DataObjectExistsException, IOException {
    super(pf, loader);
    CookieSet cookies = getCookieSet();
    InputSource is = DataObjectAdapters.inputSource(this);
    cookies.add(new CheckXMLSupport(is));
    cookies.add(new ValidateXMLSupport(is));
    registerEditor("text/x-syx+xml", false);
  }

  @Override
  protected int associateLookup() {
    return 1;
  }
}
