package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;

public class AstDecoratedIdentifier extends ModulaAstNode 
{
    public AstDecoratedIdentifier(ModulaCompositeType<AstDecoratedIdentifier> elementType) {
        super(null, elementType);
    }
    
    public PstNode getIdentifier() {
        return findFirstChild(ModulaTokenTypes.IDENTIFIER, PstNode.class);
    }
    
}
