package com.excelsior.xds.parser.modula.ast.imports;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstNode;

public abstract class AstImportStatement extends ModulaAstNode 
{
    protected AstImportStatement(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }
    
}
