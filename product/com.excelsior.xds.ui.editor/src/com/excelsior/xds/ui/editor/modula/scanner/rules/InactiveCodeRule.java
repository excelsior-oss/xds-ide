package com.excelsior.xds.ui.editor.modula.scanner.rules;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;
import com.excelsior.xds.core.utils.collections.ISearchDirector;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.ui.editor.modula.ModulaPartitionTokens;

public class InactiveCodeRule implements IPredicateRule {
	private List<ITextRegion> inactiveCodeRegions = Collections.emptyList();
	
	private ModulaAst modulaAst;

	private boolean isShowInactiveCode;
	
	public InactiveCodeRule() {
	}

	private void getInactiveCodeRegions() {
		if (modulaAst != null) {
			inactiveCodeRegions = modulaAst.getInactiveCodeRegions();
		}
	}

	public void setModulaAst(ModulaAst modulaAst) {
		this.modulaAst = modulaAst;
		getInactiveCodeRegions();
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		return evaluate(scanner, false);
	}
	
	private ITextRegion findInactiveCodeRegion(final int offset) {
		return CollectionsUtils.binarySearch(inactiveCodeRegions, new ISearchDirector<ITextRegion>(){
			@Override
			public int direct(ITextRegion reg) {
				if (offset < reg.getOffset()) {
	                return -1;
	            }
				else if (offset > reg.getOffset() + reg.getLength() - 1) {
					return 1;
				}
				else { // offset between [key.getOffset(),
					// key.getOffset() + key.getLength() - 1] -
					// i.e. inside region covered by key
					return 0;
				}
			}
		});
	}

	@Override
	public IToken getSuccessToken() {
		return ModulaPartitionTokens.DISABLED_CODE;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		if (scanner instanceof ITokenScanner) {
			if (isShowInactiveCode && !CollectionUtils.isEmpty(inactiveCodeRegions)) {
				ITokenScanner tokenScanner = (ITokenScanner) scanner;
				ITextRegion reg = findInactiveCodeRegion(tokenScanner.getTokenOffset());
				if (reg != null) {
					for (int i = tokenScanner.getTokenOffset(); i < reg.getOffset() + reg.getLength(); i++) {
						scanner.read();
					}
					return ModulaPartitionTokens.DISABLED_CODE;
				}
			}
		}
		return Token.UNDEFINED;
	}

	public void setShowInactiveCode(boolean isShowInactiveCode) {
		this.isShowInactiveCode = isShowInactiveCode;
	}
}
