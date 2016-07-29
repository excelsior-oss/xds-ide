package com.excelsior.xds.ui.perspectives;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

import com.excelsior.xds.core.ide.ui.consts.IUiConstants;

public class XdsPerspectiveFactory implements IPerspectiveFactory {
    public static final String DEVELOPMENT_PERSPECTIVE_ID = IUiConstants.DEVELOPMENT_PERSPECTIVE_ID; //$NON-NLS-1$

    @Override
    public void createInitialLayout(IPageLayout layout) {
        layout.addNewWizardShortcut("com.excelsior.xds.ui.project.NewProjectFromScratchWizard"); //$NON-NLS-1$
        layout.addNewWizardShortcut("com.excelsior.xds.ui.project.NewProjectFromSourcesWizard"); //$NON-NLS-1$
        
        String editorArea = layout.getEditorArea();
        
        layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT, 0.20f, editorArea);
        
        // the Tasks view.
        IFolderLayout bottom =
              layout.createFolder("bottom", IPageLayout.BOTTOM, 0.80f, editorArea); //$NON-NLS-1$
        bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
        bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
        
        IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, 0.80f, editorArea); //$NON-NLS-1$
        right.addView(IPageLayout.ID_OUTLINE);

        layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
        layout.addActionSet("org.eclipse.debug.ui.profileActionSet"); //Bug: not included in IDebugUIConstants  //$NON-NLS-1$ 
        
        // Put the Outline view on the left.
//        layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.01f, editorArea);
    }
}
