package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public abstract class AstSymbolDef<T extends IModulaSymbol> extends AstSymbolRef<T> 
{
    public AstSymbolDef(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }

}
