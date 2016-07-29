package com.excelsior.xds.parser.commons.ast;

import java.util.Collection;

import com.excelsior.xds.parser.commons.pst.PstNode;

/**
 * Frame of the AST node
 */
public interface IAstFrameNode
{
    /**
     * Returns nodes which constitute the frame of this AST node.
     * 
     * @return collection of PST nodes
     */
    public Collection<PstNode> getFrameNodes();


    /**
     * Adds node to the frame of this AST node.
     * 
     * @param node
     */
    public void addFrameNode(PstNode node);
    
    /**
     * Returns name of the construction ('IF', 'PROCEDURE' etc.)
     * @return
     */
    public String getFrameName();

}
