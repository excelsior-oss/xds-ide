package com.excelsior.xds.ui.editor.modula.facade;

import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.editor.modula.utils.ModulaEditorSymbolUtils;

public abstract class ActiveEditorFacade 
{
	public static PstLeafNode getPstLeafNode(int offset) {
		return ModulaEditorSymbolUtils.getPstLeafNode(WorkbenchUtils.getActiveFile(), offset);
	}
	
	public static ModulaAst getAst() {
		return ModulaEditorSymbolUtils.getModulaAst(WorkbenchUtils.getActiveFile());
	}

}