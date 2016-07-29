package com.excelsior.xds.ui.editor.modula;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

public interface IModulaEditorActionDefinitionIds extends
		ITextEditorActionDefinitionIds {
	/**
	 * Action definition ID of the open declaration action
	 * (value <code>"com.excelsior.xds.ui.editor.modula.opendecl"</code>).
	 */
	public static final String OPEN_DECL = "com.excelsior.xds.ui.editor.modula.opendecl"; //$NON-NLS-1$
	public static final String FIND_DECL = "com.excelsior.xds.ui.editor.modula.finddecl"; //$NON-NLS-1$
	public static final String FIND_REFS = "com.excelsior.xds.ui.editor.modula.findrefs"; //$NON-NLS-1$
}
