package com.excelsior.xds.ui.editor.commons;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.ui.commons.utils.SwtUtils;

/**
 * *FSA 16.09.13:
 *   Made from DefaultInformationControl.class
 */


public class HoverInformationControl extends AbstractInformationControl implements DisposeListener, IInformationControlExtension2 {

    private final int fAdditionalTextStyles;
    private Composite fParent;
    private IHoverInfo fInput;
    
    /**
     * Creates a default information control with the given shell as parent. 
     *
     * @param parent the parent shell
     * @param statusFieldText the text to be used in the status field or <code>null</code> to hide the status field
     */
    public HoverInformationControl(Shell parent, String statusFieldText) {
        super(parent, statusFieldText);
        fAdditionalTextStyles= SWT.NONE;
        create();
    }

    /**
     * Creates a resizable default information control with the given shell as
     * parent. 
     *
     * @param parent the parent shell
     * @param toolBarManager the manager or <code>null</code> if toolbar is not desired
     */
    private HoverInformationControl(Shell parent, ToolBarManager toolBarManager) {
        super(parent, toolBarManager);
        fAdditionalTextStyles= SWT.V_SCROLL | SWT.H_SCROLL;
        create();
    }

    @Override
    protected void createContent(Composite parent) {
        fParent= parent;
    }
    
    @Override
    public void setInput(Object input) {
        if (input instanceof IHoverInfo) {
            fInput = (IHoverInfo)input;
            fInput.deferredCreateContent(fParent, this);
        } else {
            LogHelper.logError(this.getClass().getName() + " setInput() : input is not instanceof IHoverInfo");
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (fInput != null) {
            fInput.setVisible(visible);
        }

        if (!visible) {
            Control[] children= fParent.getChildren();
            for (Control element : children) {
                element.dispose();
            }
            ToolBarManager toolBarManager= getToolBarManager();
            if (toolBarManager != null) {
                toolBarManager.removeAll();
            }
        }

        super.setVisible(visible);
    }


    @Override
    public Point computeSizeHint() {
        Point size = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        int wwidth = SwtUtils.getTextWidth(fParent, "W"); 
        size.x += wwidth * 5; // add some empty space around
        size.y += wwidth * 2;
        return size;
    }

    @Override
    public void setForegroundColor(Color foreground) {
        super.setForegroundColor(foreground);
        Control[] children= fParent.getChildren();
        for (Control element : children) {
            element.setForeground(foreground);
        }
    }

    @Override
    public void setBackgroundColor(Color background) {
        super.setBackgroundColor(background);
        Control[] children= fParent.getChildren();
        for (Control element : children) {
            element.setBackground(background);
        }
    }

    @Override
    public boolean hasContents() {
        return fInput != null;
    }

    @Override
    public void widgetDisposed(DisposeEvent event) {
    }

    /*
     * @see org.eclipse.jface.text.IInformationControlExtension5#getInformationPresenterControlCreator()
     * @since 3.4
     */
    @Override
    public IInformationControlCreator getInformationPresenterControlCreator() {
        return new IInformationControlCreator() {
            /*
             * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(org.eclipse.swt.widgets.Shell)
             */
            public IInformationControl createInformationControl(Shell parent) {
                return new HoverInformationControl(parent, (ToolBarManager) null);
            }
        };
    }
    
    /* -------------------- IHoverInfo calls if: --------------------------- */

    public int getAdditionalTextStyles() {
        return fAdditionalTextStyles;
    }
    
    public Point getInfoSizeConstraints() {
        return super.getSizeConstraints();
    }

}
