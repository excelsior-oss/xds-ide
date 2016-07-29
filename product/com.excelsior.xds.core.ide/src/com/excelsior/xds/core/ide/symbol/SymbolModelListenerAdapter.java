package com.excelsior.xds.core.ide.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

public class SymbolModelListenerAdapter implements ISymbolModelListener {
	private final Iterable<ParsedModuleKey> targetModuleKeys;
	
	public SymbolModelListenerAdapter(ParsedModuleKey targetModuleKey) {
		this.targetModuleKeys = Arrays.asList(targetModuleKey);
	}

	public SymbolModelListenerAdapter(Iterable<ParsedModuleKey> targetModuleKeys) {
		this.targetModuleKeys = targetModuleKeys;
	}

	@Override
	public Iterable<ParsedModuleKey> getModulesOfInterest() {
		List<ParsedModuleKey> emptyList = Collections.emptyList();
		return targetModuleKeys != null ? targetModuleKeys : emptyList;
	}

	@Override
	public void parsed(ParsedModuleKey key, IModuleSymbol moduleSymbol,
			ModulaAst ast) {
	}

	@Override
	public void removed(ParsedModuleKey key) {
	}

	@Override
	public void error(Throwable error) {
	}

	@Override
	public void modelUpToDate() {
	}

	@Override
	public boolean isInterestedInModelUpToDateEvent() {
		return false;
	}
}
