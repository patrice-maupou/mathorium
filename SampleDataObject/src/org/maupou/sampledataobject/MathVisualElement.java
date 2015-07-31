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

import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.maupou.expressions.ExprNode;
import org.maupou.expressions.SyntaxWrite;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Patrice
 */
@MultiViewElement.Registration(
        displayName = "#LBL_math_VISUAL",
        iconBase = "org/maupou/sampledataobject/edit-mathematics.png",
        mimeType = "text/x-math+xml",
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "mathVisual",
        position = 2000
)
@NbBundle.Messages("LBL_math_VISUAL=Visual")

public class MathVisualElement  extends JPanel implements MultiViewElement {
    
    private final MathTopComponent mtc;
    private final ArrayList<ExprNode> exprNodes;
    private final JToolBar toolbar = new JToolBar();
    private transient MultiViewElementCallback callback;
    private final JTextPane textPane;


    public MathVisualElement(MathEditorDescriptor mmvd) {
        mtc = mmvd.getMathElement();
        exprNodes = mmvd.getMathElement().getExprNodes();
        JScrollPane scrollPane = new javax.swing.JScrollPane();
        textPane = new javax.swing.JTextPane();
        textPane.setEditable(false);
        setLayout(new java.awt.BorderLayout());
        scrollPane.setViewportView(textPane);
        add(scrollPane, java.awt.BorderLayout.CENTER);
        mtc.getListModel().addListDataListener(new ListDataListener() {
          @Override
          public void intervalAdded(ListDataEvent e) {try {
                display();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
          }
          @Override
          public void intervalRemoved(ListDataEvent e) {try {
                display();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
          }
          @Override
          public void contentsChanged(ListDataEvent e) {try {
                display();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
          }
        });
    }
    
        
    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return mtc.getLookup();
    }

    @Override
    public void componentOpened() {
        HTMLEditorKit kit = new HTMLEditorKit();
        textPane.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("var {color:blue; font-size:16; font-style:normal; margin: 4px; }");
        styleSheet.addRule("div {color:grey; font-size:16; font-style:italic; }");
        try {
            display();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void display() throws Exception {
        SyntaxWrite syntaxWrite = mtc.getSyntaxWrite();
        StringBuilder output = new StringBuilder();
        for (ExprNode exprNode : exprNodes) {
            if(!exprNode.getComment().isEmpty()) {
                output.append("<div>").append(exprNode.getComment()).append("</div>");
            }
            output.append("<var>").append(syntaxWrite.toString(exprNode.getE())).append("</var><br>");
        }        
        textPane.setText(output.toString());
    }
    
    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {        
        return CloseOperationState.STATE_OK;
    }

    
}
