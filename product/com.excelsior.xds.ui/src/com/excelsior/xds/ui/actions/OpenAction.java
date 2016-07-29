package com.excelsior.xds.ui.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;

import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsExternalCompilationUnit;
import com.excelsior.xds.core.model.IXdsFolderContainer;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.internal.nls.Messages;

/**
 * This action opens a XDS editor on a XDS element or file.
 * <p>
 * The action is applicable to selections containing elements of
 * type <code>ICompilationUnit</code>, <code>IMember</code>
 * or <code>IFile</code>.
 *
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @since 2.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class OpenAction extends SelectionDispatchAction {

	private TextEditor fEditor;

	/**
	 * Creates a new <code>OpenAction</code>. The action requires
	 * that the selection provided by the site's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 *
	 * @param site the site providing context information for this action
	 */
	public OpenAction(IWorkbenchSite site) {
		super(site);
		setText(Messages.OpenAction_Open);
		setToolTipText(Messages.OpenAction_Open);
		setDescription(Messages.OpenAction_Open);
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 * @param editor the Java editor
	 *
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
	public OpenAction(TextEditor editor) {
		this(editor.getEditorSite());
		fEditor= editor;
		setText(Messages.OpenAction_Open);
		setEnabled(CoreEditorUtils.getEditorInputXdsElement(fEditor) != null);
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	@Override
	public void selectionChanged(ITextSelection selection) {
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	@Override
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(checkEnabled(selection));
	}

	private boolean checkEnabled(IStructuredSelection selection) {
		if (selection.isEmpty())
			return false;
		for (Iterator<?> iter= selection.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof IXdsElement && !(element instanceof IXdsFolderContainer))
				continue;
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	@Override
	public void run(ITextSelection selection) {
	}

	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	@Override
	public void run(IStructuredSelection selection) {
		if (!checkEnabled(selection))
			return;
		run(selection.toArray());
	}

	/**
	 * Note: this method is for internal use only. Clients should not call this method.
	 *
	 * @param elements the elements to process
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void run(Object[] elements) {
		if (elements == null)
			return;
		
		ArrayList<String> absentFiles = new ArrayList<String>(); 
        for (Object el : elements) {
            IPath ip = null;
            if (el instanceof IXdsExternalCompilationUnit) {
                ip = ((IXdsExternalCompilationUnit)el).getFullPath();
            }
            if (el instanceof IFile) {
                ip = ((IFile)el).getFullPath();
            }
            if (ip != null) {
                String path = ip.toOSString();
                if (!(new File(path).isFile())) {
                    absentFiles.add(path);
                }
            }
        }
        if (!absentFiles.isEmpty()) {
            String msg = Messages.OpenAction_0;
            if (absentFiles.size() == 1) {
                msg = String.format(Messages.OpenAction_FileNotFound, absentFiles.get(0));
            } else {
                for (String s : absentFiles) {
                    msg += "\n    "; //$NON-NLS-1$
                    msg += s;
                }
                msg = String.format(Messages.OpenAction_FilesNotFound, msg);
            }
            SWTFactory.ShowMessageBox(null, Messages.OpenAction_CantOpenFiles, msg, SWT.OK|SWT.ICON_ERROR);
            return;
        }
		
		for (int i= 0; i < elements.length; i++) {
			Object element= elements[i];
			try {
				boolean activateOnOpen= fEditor != null ? true : OpenStrategy.activateOnOpen();
				CoreEditorUtils.openInEditor(element, activateOnOpen);
			} catch (PartInitException e) {
			} catch (CoreException e) {
				LogHelper.logError(e);
			}
		}
	}
}
