package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public class AstDecoratedName extends AstName<IModulaSymbol> {

    public AstDecoratedName(ModulaCompositeType<AstDecoratedName> elementType) {
        super(null, elementType);
    }
    
}
