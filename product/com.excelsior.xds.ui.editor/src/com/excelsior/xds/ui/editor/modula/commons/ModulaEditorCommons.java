package com.excelsior.xds.ui.editor.modula.commons;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;

import com.excelsior.xds.core.preferences.PreferenceKeys;
import com.excelsior.xds.ui.editor.commons.PartitionUtils;
import com.excelsior.xds.ui.editor.modula.IModulaPartitions;
import com.excelsior.xds.ui.editor.modula.ModulaFastPartitioner;

public final class ModulaEditorCommons {
	public static ModulaFastPartitioner getModulaFastPartitioner(IDocument document) {
    	IDocumentPartitioner documentPartitioner = PartitionUtils.getPartitioner(document, IModulaPartitions.M2_PARTITIONING);
		if (documentPartitioner instanceof ModulaFastPartitioner) {
			return (ModulaFastPartitioner) documentPartitioner;
		}
		return null;
    }
	
	public static void configureModulaFastPartitioner(IDocument document) {
		final ModulaFastPartitioner modulaFastPartitioner = ModulaEditorCommons.getModulaFastPartitioner(document);
		if (modulaFastPartitioner != null) {
			boolean isShowInactiveCode = PreferenceKeys.PKEY_HIGHLIGHT_INACTIVE_CODE.getStoredBoolean();
			modulaFastPartitioner.setShowInactiveCode(isShowInactiveCode);
		}
	}
	
	/**
	 * Static methods only.
	 */
	private ModulaEditorCommons(){
	}
}
