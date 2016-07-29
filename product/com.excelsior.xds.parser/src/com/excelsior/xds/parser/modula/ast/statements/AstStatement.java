package com.excelsior.xds.parser.modula.ast.statements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstNode;

public abstract class AstStatement extends ModulaAstNode 
                                   implements IAstFrameNode
{
    private List<PstNode> frame = new ArrayList<PstNode>(4);

    public AstStatement(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<PstNode> getFrameNodes() {
        return frame;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addFrameNode(PstNode node) {
        frame.add(node);
    }

}
