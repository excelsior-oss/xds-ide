package com.excelsior.xds.parser.modula.ast.modules;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstNode;

public abstract class AstExportStatement extends ModulaAstNode 
{
    protected AstExportStatement(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }

}
