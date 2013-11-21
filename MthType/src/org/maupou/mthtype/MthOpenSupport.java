/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.mthtype;

import org.maupou.expressions.Syntax;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.OpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 *
 * @author Patrice
 */
public class MthOpenSupport extends OpenSupport implements OpenCookie, CloseCookie {

    GeneratorViewTopComponent gvtc;
    MthTopComponent tc;
    Syntax syntax;
    String name;

    public MthOpenSupport(MthDataObject.Entry entry) {
        super(entry);
        MthDataObject mdo = (MthDataObject) entry.getDataObject();
        syntax = mdo.getSyntax();
        if (syntax != null) {
            gvtc = new GeneratorViewTopComponent(mdo.getSyntax());
        }
    }

    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        MthDataObject mdo = (MthDataObject) entry.getDataObject();
        tc = new MthTopComponent(mdo);
        name = mdo.getName();
        tc.setDisplayName(name);
        gvtc.setTc(tc);
        return tc;
    }

    @Override
    public void open() {
        super.open();
        if (gvtc != null) {
            Mode mode = WindowManager.getDefault().findMode("properties");
            mode.dockInto(gvtc);
            gvtc.setName(name);
            gvtc.open();
            gvtc.requestActive();
        }
    }
}
