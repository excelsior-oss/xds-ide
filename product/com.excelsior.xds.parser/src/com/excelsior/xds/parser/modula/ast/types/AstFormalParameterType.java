package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.AstSymbolRef;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;

/**
 * Type of the formal parameter in the procedure type definition. 
 */
public class AstFormalParameterType extends AstSymbolRef<IFormalParameterSymbol> {

    public AstFormalParameterType(ModulaCompositeType<AstFormalParameterType> elementType) {
        super(null, elementType);
    }

}
