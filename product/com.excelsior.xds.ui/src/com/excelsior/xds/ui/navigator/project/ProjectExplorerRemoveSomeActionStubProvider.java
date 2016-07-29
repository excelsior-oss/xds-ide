package com.excelsior.xds.ui.navigator.project;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.navigator.CommonActionProvider;

public class ProjectExplorerRemoveSomeActionStubProvider extends
        CommonActionProvider {

    public ProjectExplorerRemoveSomeActionStubProvider() {
    }

    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
        
        // remove Copy/Paste contributions
//        
//        IContributionItem[] cis = menu.getItems();
//        for (IContributionItem ci : cis) {
//            System.out.println(ci);
//        }
//        
//        menu.remove("org.eclipse.ui.file.import");
//        menu.remove("org.eclipse.ui.file.export");
    }

}
