package com.excelsior.xds.parser.modula.ast.procedures;

import com.excelsior.xds.parser.modula.ast.AstSymbolRef;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class AstResultType extends AstSymbolRef<ITypeSymbol> {

    public AstResultType(ModulaCompositeType<AstResultType> elementType) {
        super(null, elementType);
    }

}
