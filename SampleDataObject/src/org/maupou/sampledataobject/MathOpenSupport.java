/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.maupou.sampledataobject;

import java.io.IOException;
import javax.swing.JButton;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.OpenSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.windows.CloneableTopComponent;

/**
 *
 * @author Patrice
 */
public class MathOpenSupport extends OpenSupport implements OpenCookie, CloseCookie {
    private CloneableTopComponent tc;
    private MathDataObject mdo;

    public MathOpenSupport(MathDataObject.Entry entry) {
        super(entry);
    }

    /**
     *
     * @return le MultiViewComponent
     */
    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        mdo = (MathDataObject) entry.getDataObject();
        MathEditorDescriptor mathDescriptor = new MathEditorDescriptor(mdo);
        MathVisualDescriptor visualDescriptor = new MathVisualDescriptor(mathDescriptor);
        MultiViewDescription[] descArry = { mathDescriptor , visualDescriptor };
        tc = MultiViewFactory.createCloneableMultiView(descArry, mathDescriptor, createCloseOperationHandler());
        tc.setDisplayName(mdo.getName());
        tc.setIcon(ImageUtilities.loadImage("org/maupou/sampledataobject/edit-mathematics.png"));
        return tc;
    }
    
    private CloseOperationHandler createCloseOperationHandler(){
        return (CloseOperationState[] elements) -> {
          if(mdo.isModified()) {
            JButton saveOption = new JButton("Save");
            JButton discardOption = new JButton("Discard");
            discardOption.setMnemonic('D');
            NotifyDescriptor nd = new NotifyDescriptor(	"File is modified. Save ?", "Question",
                    NotifyDescriptor.YES_NO_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] {saveOption, discardOption, NotifyDescriptor.CANCEL_OPTION},
                    saveOption);
            Object ret = DialogDisplayer.getDefault().notify(nd);
            if (NotifyDescriptor.CANCEL_OPTION.equals(ret) ||
                    NotifyDescriptor.CLOSED_OPTION.equals(ret)) {
              return false;
            }
            if (saveOption.equals(ret)) {
              SaveCookie saver = mdo.getLookup().lookup(SaveCookie.class);
              try {
                saver.save();
              } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
              }
            }
          }
          return true;
        };
    }

    /*
    @Override
    public boolean close() {
        return tc.close();
    }
    //*/
}
