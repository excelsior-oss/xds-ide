package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.AstSymbolRef;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

/**
 * Type of the formal parameter in the procedure definition and declaration. 
 */
public class AstFormalType extends AstSymbolRef<ITypeSymbol> {

    public AstFormalType(ModulaCompositeType<AstFormalType> elementType) {
        super(null, elementType);
    }

}
