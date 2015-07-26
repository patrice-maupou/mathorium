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
