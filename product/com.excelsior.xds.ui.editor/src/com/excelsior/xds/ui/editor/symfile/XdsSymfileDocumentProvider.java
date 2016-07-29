package com.excelsior.xds.ui.editor.symfile;

import java.io.File;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import com.excelsior.xds.core.compiler.driver.CompileDriver;
import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.ui.editor.internal.nls.Messages;

public class XdsSymfileDocumentProvider extends FileDocumentProvider 
{
    public XdsSymfileDocumentProvider() {
        super();
    }
    
    /*
	 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#createEmptyDocument()
	 * @since 3.1
	 */
	@Override
	protected IDocument createEmptyDocument() {
		IDocument document= FileBuffers.getTextFileBufferManager().createEmptyDocument(null, LocationKind.IFILE);
		if (document instanceof ISynchronizable)
			((ISynchronizable)document).setLockObject(new Object());
		return document;
	}
    
	@Override
	protected boolean setDocumentContent(IDocument document,
			IEditorInput editorInput, String encoding) throws CoreException {
		String fileContents = null;

		File file = CoreEditorUtils.editorInputToFile(editorInput);
        if (file != null) {
            String filePath = file.getAbsolutePath();
            fileContents = CompileDriver.decodeSymFile(filePath);
        }
        else {
        	fileContents = Messages.SymFileEditor_CannotOpenFile;
        }
        document.set(fileContents);
		return true;
	}

	@Override
    protected IAnnotationModel createAnnotationModel(Object element)
    		throws CoreException {
    	IAnnotationModel annotationModel = super.createAnnotationModel(element);
    	if (annotationModel == null) {
    		annotationModel = new AnnotationModel();
    	}
		return annotationModel;
    }
}
