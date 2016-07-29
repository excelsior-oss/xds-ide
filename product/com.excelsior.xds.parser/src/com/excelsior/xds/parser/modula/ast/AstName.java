package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public abstract class AstName<T extends IModulaSymbol> extends AstSymbolRef<T> {

    public AstName(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }
    
    public PstNode getIdentifier() {
        return findFirstChild(ModulaTokenTypes.IDENTIFIER, PstNode.class);
    }
    
}
