package com.excelsior.xds.parser.internal.modula.symbol;

import com.excelsior.xds.core.builders.DefaultBuildSettingsHolder;
import com.excelsior.xds.parser.modula.XdsLanguage;
import com.excelsior.xds.parser.modula.symbol.IInvalidModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolUsages;

public class UnresovedModuleSymbol extends    DefinitionModuleSymbol
                                   implements IInvalidModulaSymbol
{
    public UnresovedModuleSymbol(String name, XdsLanguage language) {
        super(name, language, null, DefaultBuildSettingsHolder.DefaultBuildSettings, null, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IModulaSymbolUsages createSymbolUsages() {
        return ModulaSymbolUsages.EMPTY_USAGES;    
    }
    
}
