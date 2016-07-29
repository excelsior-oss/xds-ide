package com.excelsior.xds.ui.editor.modula;

import org.eclipse.jface.text.rules.FastPartitioner;

import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.ui.editor.modula.scanner.rules.ModulaRuleBasedPartitionScanner;

public class ModulaFastPartitioner extends FastPartitioner {

	public ModulaFastPartitioner(ModulaRuleBasedPartitionScanner scanner, String[] legalContentTypes) {
		super(scanner, legalContentTypes);
	}
	
	private ModulaRuleBasedPartitionScanner getScanner() {
		return (ModulaRuleBasedPartitionScanner) fScanner;
	}
	
	public void setModulaAst(ModulaAst ast) {
		ModulaRuleBasedPartitionScanner scanner = getScanner();
		scanner.setModulaAst(ast);
	}

	public void setShowInactiveCode(boolean isShowInactiveCode) {
		ModulaRuleBasedPartitionScanner scanner = getScanner();
		scanner.setShowInactiveCode(isShowInactiveCode);
	}
}
