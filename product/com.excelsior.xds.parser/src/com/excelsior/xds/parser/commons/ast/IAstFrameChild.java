package com.excelsior.xds.parser.commons.ast;

/**
 * Not all IAstFrameNode may be found in the tree as a parent of its children nodes
 * (f.ex. - pragma if/elsif/end  in some cases). In this case this interface may be used to
 * link framed node to its IAstFrameNode
 *
 */

public interface IAstFrameChild {
    
    public IAstFrameNode getAstFrameNode();
    public void setAstFrameNode(IAstFrameNode frameNode);
}
