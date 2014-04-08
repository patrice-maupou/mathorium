/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.mthtype;

import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.OpenSupport;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 *
 * @author Patrice
 */
public class MathOpenSupport extends OpenSupport implements OpenCookie, CloseCookie {

    private final mathDataObject mdo;
    private GeneratorViewTopComponent gvtc;

    public MathOpenSupport(MultiDataObject.Entry entry) {
        super(entry);
        mdo = (mathDataObject) entry.getDataObject();
    }

    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        gvtc = new GeneratorViewTopComponent(mdo);
        Mode mode = WindowManager.getDefault().findMode("properties");
        mode.dockInto(gvtc);
        gvtc.setName(mdo.getName());
        return gvtc;
    }

    @Override
    public void open() {
        super.open();
        WindowManager.getDefault().setTopComponentFloating(gvtc, false);
        gvtc.requestActive();
    }
    
    @Override
    public boolean close() {
        boolean ret = super.close();
        DataEditorSupport support = mdo.getLookup().lookup(DataEditorSupport.class);
        if(support != null) {
            support.close();
        }
        return ret;
    }

}
