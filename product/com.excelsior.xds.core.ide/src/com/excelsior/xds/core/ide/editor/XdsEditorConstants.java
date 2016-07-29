package com.excelsior.xds.core.ide.editor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class XdsEditorConstants {
	public static final String OBERON_DEFINITION_MODULE_EDITOR_ID = "com.excelsior.xds.ui.editor.oberon.DefinitionModuleEditor";
	public static final String OBERON_MODULE_EDITOR_ID = "com.excelsior.xds.ui.editor.oberon.ModuleEditor";
	public static final String MODULA_DEFINITION_MODULE_EDITOR_ID = "com.excelsior.xds.ui.editor.modula.DefinitionModuleEditor";
	public static final String MODULA_PROGRAM_MODULE_EDITOR_ID = "com.excelsior.xds.ui.editor.modula.ProgramModuleEditor";
	
	public static final Set<String> XDS_EDITOR_IDS = new HashSet<String>(Arrays.asList(OBERON_DEFINITION_MODULE_EDITOR_ID, OBERON_MODULE_EDITOR_ID, MODULA_DEFINITION_MODULE_EDITOR_ID, MODULA_PROGRAM_MODULE_EDITOR_ID));
	public static final Set<String> XDS_PROGRAM_MODULE_EDITOR_IDS = new HashSet<String>(Arrays.asList(OBERON_MODULE_EDITOR_ID, MODULA_PROGRAM_MODULE_EDITOR_ID));

	private XdsEditorConstants() {
	}
}
