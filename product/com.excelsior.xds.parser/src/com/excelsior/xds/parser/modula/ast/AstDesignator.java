package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public class AstDesignator extends AstSymbolRef<IModulaSymbol>
{
    public AstDesignator(ModulaCompositeType<AstDesignator> elementType) {
        super(null, elementType);
    }

}
