package com.excelsior.xds.ui.editor.modula.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;

import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;
import com.excelsior.xds.ui.editor.modula.hyperlink.ModulaDeclarationHyperlinkDetector;

public class OpenDeclarationsAction extends Action 
{
    public static final String ID = "com.excelsior.xds.ui.editor.actions.modula.OpenDeclaration"; //$NON-NLS-1$

    private ModulaEditor modulaEditor;

	public OpenDeclarationsAction(ModulaEditor modulaEditor) {
		this.modulaEditor = modulaEditor;
		setText(Messages.OpenDeclarations_label); 
		setToolTipText(Messages.OpenDeclarations_tooltip); 
		setDescription(Messages.OpenDeclarations_description);
	}

	@Override
	public void run() {
        ISelection selection = modulaEditor.getSelectionProvider().getSelection();
        if (selection instanceof ITextSelection) {
            ITextSelection textSelection = (ITextSelection)selection;
    	    ModulaDeclarationHyperlinkDetector hld = new ModulaDeclarationHyperlinkDetector();
    	    hld.setContext(modulaEditor);
    	    ISourceViewer viewer = (ISourceViewer)modulaEditor.getAdapter(ISourceViewer.class);
    	    IHyperlink[] links = hld.detectHyperlinks(viewer, new Region(textSelection.getOffset(), 0), false);
    	    if (links != null && links.length > 0) {
    	        links[0].open();
    	    }
        }
		super.run();
	}
}
