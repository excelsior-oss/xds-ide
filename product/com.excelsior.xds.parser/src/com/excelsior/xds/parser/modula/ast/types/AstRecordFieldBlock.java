package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstNode;

public abstract class AstRecordFieldBlock extends ModulaAstNode 
{
    public AstRecordFieldBlock(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }

}
