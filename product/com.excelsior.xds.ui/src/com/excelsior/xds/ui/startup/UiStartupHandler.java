package com.excelsior.xds.ui.startup;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;

import com.excelsior.xds.core.sdk.SdkUtils;
import com.excelsior.xds.ui.internal.update.AutomaticUpdateScheduler;
import com.excelsior.xds.ui.internal.update.UpdateManager;

public class UiStartupHandler implements IStartup {
    
    private final Set<String> actionSetsToRemove = new HashSet<String>();
    private AutomaticUpdateScheduler automaticUpdateScheduler;
    
    public UiStartupHandler() {
        actionSetsToRemove.add("org.eclipse.ui.cheatsheets.actionSet"); //$NON-NLS-1$
        actionSetsToRemove.add("org.eclipse.ui.edit.text.actionSet.annotationNavigation"); //$NON-NLS-1$
        actionSetsToRemove.add("org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo"); //$NON-NLS-1$
    }

    @Override
    public void earlyStartup() {
        /*
         * The registration of the listener should have been done in the UI thread
         * since  PlatformUI.getWorkbench().getActiveWorkbenchWindow() returns null
         * if it is called outside of the UI thread.
         * */
         Display.getDefault().asyncExec(new Runnable() {
           /* (non-Javadoc)
           * @see java.lang.Runnable#run()
           */
           public void run() {
               SdkUtils.autoLoadSdksOnEarlyStart();
        	   UpdateManager.activateXdsUpdateContext();
        	   
        	   automaticUpdateScheduler = new AutomaticUpdateScheduler();
        	   automaticUpdateScheduler.earlyStartup();
        		
               final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
               if (workbenchWindow != null) {
                 removeActionSets(workbenchWindow, workbenchWindow.getActivePage().getPerspective());
                 workbenchWindow.addPerspectiveListener(new PerspectiveAdapter() {
                   /* (non-Javadoc)
                   * @see org.eclipse.ui.PerspectiveAdapter#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
                   */
                   @Override
                   public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspectiveDescriptor) {
                     super.perspectiveActivated(page, perspectiveDescriptor);
//                     removeActionSets(workbenchWindow, perspectiveDescriptor);
                   }
                 });
               }
           }
         });
    }
    
    private void removeActionSets(final IWorkbenchWindow workbenchWindow,
            IPerspectiveDescriptor perspectiveDescriptor) {
    	// TODO : FIXME not working in e4
//        if (perspectiveDescriptor.getId().indexOf(
//                XdsPerspectiveFactory.DEVELOPMENT_PERSPECTIVE_ID) > -1) {
//            if (workbenchWindow.getActivePage() instanceof WorkbenchPage) {
//                WorkbenchPage worbenchPage = (WorkbenchPage) workbenchWindow
//                        .getActivePage();
//                // Get the perspective
//                Perspective perspective = worbenchPage.findPerspective(perspectiveDescriptor);
//                
//                // hide some item at Project menu
//                perspective.getHiddenMenuItems().addAll(Arrays.asList(
//                		"org.eclipse.ui.internal.ide.actions.BuildSetMenu", //$NON-NLS-1$
//                		"org.eclipse.ui.project.buildProject",  //$NON-NLS-1$
//                		"org.eclipse.ui.project.buildAll",  //$NON-NLS-1$
//                		"org.eclipse.ui.project.closeProject",  //$NON-NLS-1$
//                		"org.eclipse.ui.project.openProject",  //$NON-NLS-1$
//                		"org.eclipse.ui.project.cleanAction")); //$NON-NLS-1$
//                
//                ArrayList toRemove = new ArrayList();
//                if (perspective != null) {
//                    
//                    for (IActionSetDescriptor actionSetDescriptor : perspective.getAlwaysOnActionSets()) {
//                        if (actionSetsToRemove.contains(actionSetDescriptor.getId())) {
//                            toRemove.add(actionSetDescriptor);
//                        }
//                    }
//                    perspective.turnOffActionSets((IActionSetDescriptor[]) toRemove.toArray(new IActionSetDescriptor[toRemove.size()]));
//                }
//            }
//        }
    }
}
