/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.maupou.sampledataobject;

import java.awt.Image;
import java.io.Serializable;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;

/**
 *
 * @author Patrice
 */
public class MathEditorDescriptor implements Serializable, MultiViewDescription {
    
    private MathDataObject mdo;
    private MathTopComponent mathElement;

    public MathEditorDescriptor(MathDataObject mdo) {
        this.mdo = mdo;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public String getDisplayName() {
        return "Edit";
    }

    @Override
    public Image getIcon() {
        return null;   
    }

    @Override
    public HelpCtx getHelpCtx() {
        return mdo.getHelpCtx();
    }

    @Override
    public String preferredID() {
        return "MathTopComponent";
    }

    @Override
    public MultiViewElement createElement() {
        mathElement = new MathTopComponent(mdo);
        return mathElement;
    }

    public MathTopComponent getMathElement() {
        return mathElement;
    }

    public MathDataObject getMdo() {
        return mdo;
    }

    public void setMdo(MathDataObject mdo) {
        this.mdo = mdo;
    }

    public void setMathElement(MathTopComponent mathElement) {
        this.mathElement = mathElement;
    }
    
}
