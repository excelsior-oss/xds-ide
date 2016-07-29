package com.excelsior.xds.ui.console;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

import com.excelsior.xds.core.console.IXdsConsole;
import com.excelsior.xds.core.utils.IClosure;
import com.excelsior.xds.ui.XdsPlugin;
import com.excelsior.xds.ui.actions.ToolbarActionButton;
import com.excelsior.xds.ui.images.ImageUtils;

/*
 * This class required to embed red "Terminate" button in XdsConsole 
 */

public class XdsConsoleParticipant implements IConsolePageParticipant {
    
    private static final String CONSOLE_DLG_ID = "com.excelsior.xds.ui.console.XdsConsoleParticipant_ID";
	
	private IPageBookViewPage fPage;
	private XdsConsole fConsole;
	private XdsConsoleTerminateAction fTerminate;
    ToolbarActionButton fTbaPrev;
    ToolbarActionButton fTbaNext;
    ToolbarActionButton fTbaFilter;
    static ToolbarActionButton fTbaSync;

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		if (!(console instanceof IXdsConsole)) {
			return;
		}
		
        fPage = page;
        fConsole = (XdsConsole) console;

        IToolBarManager mgr = fPage.getSite().getActionBars().getToolBarManager();

        fTerminate = new XdsConsoleTerminateAction(page.getSite().getWorkbenchWindow(), fConsole);
		mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fTerminate);
		
		String some_id = CONSOLE_DLG_ID + this.toString(); // unical, not used now to save state

		if (fTbaSync == null) {
		    // It is single action for all our consoles
            fTbaSync = new ToolbarActionButton(
                    "Link with editor", 
                    CONSOLE_DLG_ID + "_LinkWithEditor", true,    
                    ImageUtils.SYNC_WITH_EDITOR, false, getDialogSettings(),  
                    new IClosure<Boolean>(){   
                        @Override
                        public void execute(Boolean isChecked) {
                            XdsConsole.setLinkWithEditor(isChecked);
                        }
                    });
            XdsConsole.setLinkWithEditor(fTbaSync.isChecked()); // init from DialogSettings 
		}
		
        fTbaPrev = new ToolbarActionButton(
                "Previous message", 
                some_id + "_Prev", false,    
                ImageUtils.SEARCH_PREV, false, null,  
                new IClosure<Boolean>(){   
                    @Override
                    public void execute(Boolean isChecked) {
                        fConsole.goPrevNextMessage(true);
                    }
                });
        fTbaNext = new ToolbarActionButton(
                "Next message", 
                some_id + "_Next", false,    
                ImageUtils.SEARCH_NEXT, false, null,  
                new IClosure<Boolean>(){   
                    @Override
                    public void execute(Boolean isChecked) {
                        fConsole.goPrevNextMessage(false);
                    }
                });
        fTbaFilter = new ToolbarActionButton(
                "Filtered log", 
                CONSOLE_DLG_ID + "_FilteredLog", true,    
                ImageUtils.BTN_FILTER, false, getDialogSettings(),  
                new IClosure<Boolean>(){   
                    @Override
                    public void execute(Boolean isChecked) {
                        fConsole.setFilteredLog(isChecked);
                    }
                });
        if (console instanceof XdsConsole) {
            ((XdsConsole)console).setFilteredLogAction(fTbaFilter);
            ((XdsConsole)console).setFilteredLogInternal(fTbaFilter.isChecked(), false); // init from DialogSettings
        }

        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fTbaFilter);
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fTbaPrev);
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fTbaNext);
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fTbaSync);
	}

	@Override
	public void dispose() {
		if (fTerminate != null) {
		    fTerminate.dispose();
		    fTerminate = null;
		}
	}

	@Override
	public void activated() {
	}

	@Override
	public void deactivated() {
	}

    private IDialogSettings getDialogSettings() {
        IDialogSettings settings = XdsPlugin.getDefault().getDialogSettings().getSection(CONSOLE_DLG_ID);
        if (settings == null) {
            settings = XdsPlugin.getDefault().getDialogSettings().addNewSection(CONSOLE_DLG_ID);
        }
        return settings;
    }
}
