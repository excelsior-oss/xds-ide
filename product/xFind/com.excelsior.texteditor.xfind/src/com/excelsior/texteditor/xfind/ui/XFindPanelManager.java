package com.excelsior.texteditor.xfind.ui;

import java.lang.reflect.Field;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;

import com.excelsior.texteditor.xfind.internal.LogHelper;

public class XFindPanelManager 
{
    public static XFindPanel getXFindPanel(IEditorPart activeEditorPart, boolean create) 
    {
        // parent composite for XFindPanel. In case of Java editors, it is the parent of
        // breadcrumb panel; otherwise (e.g. for XML editors), it is created
        Composite parent = getParentPanel(activeEditorPart);

        if (parent != null) {
            // check if already created
            for (Control c : parent.getChildren()) {
                if (c instanceof XFindPanel) {
                    // found
                    return (XFindPanel)c;
                }
            }

            if (create) {
                // create new panel
                return new XFindPanel(parent, activeEditorPart);
            }
        }

        return null;
    }
    
    public static boolean isXFindPanelOpened(IEditorPart part) {
    	XFindPanel xFindPanel = getXFindPanel(part, false);
    	if (xFindPanel == null) {
    		return false;
    	}
    	return xFindPanel.isVisible();
    }

    public static Composite getParentPanel(IEditorPart activeEditorPart) {
        if (activeEditorPart == null) {
            // not active 
            return null;
        }

        // parent composite for XFindPanel. In case of Java editors, it is the parent of
        // breadcrumb panel; otherwise (e.g. for XML editors), it is created
        Composite parent = null;

        // NOTE: currently, we can insert XFindPanel only to StatusTextEditor

        try {
            Class<?> cl = activeEditorPart.getClass();
            Field ffDefaultComposite = null;
            while (cl != null && ffDefaultComposite == null) {
                try {
                    ffDefaultComposite = cl.getDeclaredField("fDefaultComposite");   //$NON-NLS-1$
                } catch (NoSuchFieldException e) {
                    cl = cl.getSuperclass();
                }
            }

            Composite fDefaultComposite = null;
            if (ffDefaultComposite != null) {
                ffDefaultComposite.setAccessible(true);
                fDefaultComposite = (Composite) ffDefaultComposite.get(activeEditorPart);

                boolean found = false, allReparentable = true;
                Control[] defaultChildren = fDefaultComposite.getChildren();
                for (Control child : defaultChildren) {
                    // if a child with GridLayout is found in fDefaultComposite, we use it
                    // as parent (e.g. JavaEditor or already processed other editors)
                    if (child instanceof Composite) {
                        Composite c = (Composite) child;
                        if (c.getLayout() instanceof GridLayout) {
                            parent = (Composite) child;
                            found = true;
                            break;
                        } else if (!c.isReparentable()) {
                            allReparentable = false;
                        }
                    }
                }

                if (!found && allReparentable) {
                    // create own composite, move existing children to new parent
                    parent = new Composite(fDefaultComposite, SWT.NONE);

                    Composite editorComposite= new Composite(parent, SWT.NONE);
                    editorComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                    FillLayout fillLayout= new FillLayout(SWT.VERTICAL);
                    fillLayout.marginHeight= 0;
                    fillLayout.marginWidth= 0;
                    fillLayout.spacing= 0;
                    editorComposite.setLayout(fillLayout);

                    for (Control c : defaultChildren) {
                        c.setParent(editorComposite);
                    }

                    GridLayout layout = new GridLayout(1, true);
                    layout.marginHeight= 0;
                    layout.marginWidth= 0;
                    layout.horizontalSpacing= 0;
                    layout.verticalSpacing= 0;

                    parent.setLayout(layout);
                    fDefaultComposite.layout();
                }
            }
        } catch (Exception e) {
            LogHelper.logError(e);
        }

        return parent; 
    }

}
