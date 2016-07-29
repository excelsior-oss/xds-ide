package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;

/**
 * Base class for a nodes which are used to group other nodes into big blocks 
 * to simplify differentiation of nodes.   
 */
public class AstBlock extends ModulaAstNode
{
    public AstBlock(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }

}
