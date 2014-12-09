/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.maupou.sampledataobject;

import java.awt.Image;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;

/**
 *
 * @author Patrice
 */
public class MathVisualDescriptor implements MultiViewDescription {
    
    private final MathEditorDescriptor mmvd;

    public MathVisualDescriptor(MathEditorDescriptor mmvd) {
        this.mmvd = mmvd;
    }    
    

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public String getDisplayName() {
        return "View";
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return mmvd.getHelpCtx();
    }

    @Override
    public String preferredID() {
        return "MathVisualComponent";
    }

    @Override
    public MultiViewElement createElement() {
        return new MathVisualElement(mmvd);
    }
    
}
