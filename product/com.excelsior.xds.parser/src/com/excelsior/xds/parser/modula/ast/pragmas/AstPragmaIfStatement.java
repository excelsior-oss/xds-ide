package com.excelsior.xds.parser.modula.ast.pragmas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

public class AstPragmaIfStatement extends AstPragmaConditionalStatement implements IAstFrameNode
{
    private List<PstNode> frame = new ArrayList<PstNode>(4);

    public AstPragmaIfStatement(ModulaCompositeType<AstPragmaIfStatement> elementType) {
        super(elementType);
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getFrameName() {
        return "IF";
    }

}
