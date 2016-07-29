package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public abstract class AstNameAlias<T extends IModulaSymbol> extends AstName<T> 
{
    protected AstNameAlias(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }

}
