package com.excelsior.xds.ui.navigator.project;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

import com.excelsior.xds.ui.actions.OpenAction;
import com.excelsior.xds.ui.actions.OpenEditorActionGroup;

public class ProjectExplorerOpenActionProvider extends CommonActionProvider {

	private OpenEditorActionGroup fOpenGroup;
    private OpenAndExpand fOpenAndExpand;
    private boolean fInViewPart;

    public ProjectExplorerOpenActionProvider() {
    }
    
    @Override
    public void fillContextMenu(IMenuManager menu) {
		if (fInViewPart) {
			if (fOpenGroup.getOpenAction().isEnabled()) {
				fOpenGroup.fillContextMenu(menu);
			}
		}
    }
    
    @Override
    public void fillActionBars(IActionBars actionBars) {
    	if (fInViewPart) {
			fOpenGroup.fillActionBars(actionBars);

			if (fOpenAndExpand == null && fOpenGroup.getOpenAction().isEnabled())
				actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, fOpenGroup.getOpenAction());
			else if (fOpenAndExpand != null && fOpenAndExpand.isEnabled())
				actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, fOpenAndExpand);
		}
    }

    @Override
    public void init(ICommonActionExtensionSite site) {
        ICommonViewerWorkbenchSite workbenchSite = null;
        if (site.getViewSite() instanceof ICommonViewerWorkbenchSite) {
            workbenchSite = (ICommonViewerWorkbenchSite) site.getViewSite();
        }
        
        if (workbenchSite != null) {
            if (workbenchSite.getPart() != null && workbenchSite.getPart() instanceof IViewPart) {
                IViewPart viewPart = (IViewPart) workbenchSite.getPart();
                
                fOpenGroup = new OpenEditorActionGroup(viewPart);

                if (site.getStructuredViewer() instanceof TreeViewer)
                    fOpenAndExpand = new OpenAndExpand(workbenchSite.getSite(), (OpenAction) fOpenGroup.getOpenAction(), (TreeViewer) site.getStructuredViewer());
                fInViewPart = true;
            }
        }
    }

	@Override
	public void setContext(ActionContext context) {
		super.setContext(context);
		if (fInViewPart) {
			fOpenGroup.setContext(context);
		}
	}

	@Override
	public void dispose() {
		if (fOpenGroup != null)
			fOpenGroup.dispose();
		super.dispose();
	}
}
