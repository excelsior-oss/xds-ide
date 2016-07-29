package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.XdsLanguage;
import com.excelsior.xds.parser.modula.symbol.IMainModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;

public class MainModuleSymbol extends ProgramModuleSymbol
                              implements IMainModuleSymbol
{
    public MainModuleSymbol(String name, XdsLanguage language, ParsedModuleKey key, BuildSettings buildSettings, IFileStore sourceFile) 
    {
        super(name, language, key, buildSettings, sourceFile);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasConstructor() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IModulaSymbol> getExportedSymbols() {
        return Collections.emptyList();
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		boolean isVisitChildren = visitor.visit(this);
		if (isVisitChildren) {
			super.acceptChildren(visitor);
		}
	}

}
