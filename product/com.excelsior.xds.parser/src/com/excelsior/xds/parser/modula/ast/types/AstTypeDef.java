package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.modula.ast.AstSymbolDef;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public abstract class AstTypeDef<T extends ITypeSymbol> extends AstSymbolDef<T>
{
    public AstTypeDef(PstCompositeNode parent, ElementType elementType) {
        super(null, elementType);
    }

}
