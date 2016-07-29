package com.excelsior.xds.parser.commons.ast;

import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstNode;

/**
 * Abstract Syntax Tree (AST) builder
 */
public interface IAstBuilder
{
    /**
     * Resets the mutable states of this builder.
     * Resetting a builder discards all of its explicit state information.
     */
    public void reset();
    

    /**
     * Returns the root node of the AST.
     * 
     * @return the root node of the AST. 
     */ 
    public PstCompositeNode getAstRoot();
    
    /**
     * Returns the current production.
     * 
     * @return the current production. 
     */ 
    public PstCompositeNode getCurrentProduction();
    
    /**
     * Returns the last node added into the AST.
     * 
     * @return the last added node. 
     */ 
    public PstNode getLastNode();
   

    
    /**
     * Check if the current production has the given element type.
     * 
     * @param elementType
     * 
     * @return <code>true</code> if the current production has the given element type, or 
     *         <code>false</code> otherwise.  
     */
    public boolean isCurrentProduction(ElementType elementType);
    
    
    
    /**
     * Setups the given node as the current production. The previous current 
     * production is pushed on the stack and becomes a parent of the given one. 
     * All subsequent nodes will be added as children of the given node.
     * 
     * @param node - the production to be setup as a current one 
     * 
     * @return AST node of the created production
     */
    public <T extends PstCompositeNode> T beginProduction(T node);
    
    /**
     * Creates new production based on the given element type and   
     * setups it as the current production. The previous current production 
     * is pushed on the stack and becomes a parent of the new one. 
     * All subsequent nodes will be added as children of the given node.
     * 
     * @param elementType - the element type of the new production
     * 
     * @return AST node of the created production
     */
    public PstCompositeNode beginProduction(ElementType elementType);
    
    /**
     * Creates new production based on the given composite element type and 
     * setups it as the current production. The previous current production 
     * is pushed on the stack and becomes a parent of the new one. 
     * All subsequent nodes will be added as children of the given node.
     * 
     * @param elementType - the composite element type of the new production
     * 
     * @return AST node which represents new production
     */
    public <T extends PstCompositeNode> T beginProduction(CompositeType<T> elementType);
    
    
    
    /**
     * Finalizes the current production, pops from the stack previous production
     * and sets it as the current one. The finalized production must be equal 
     * to the passed as a argument.
     * 
     * @param node - the production to be finalized 
     * 
     * @return AST node which represents the finalized production
     */
    public PstCompositeNode endProduction(PstCompositeNode node);
    
    /**
     * Finalizes the current production, pops from the stack previous production
     * and sets it as the current one. The element type of finalized production 
     * must be equal to the passed as a argument.
     * 
     * @param elementType - the element type to verify finalized production
     * 
     * @return AST node which represents the finalized production
     */
    public PstCompositeNode endProduction(IElementType elementType);
    
    /**
     * Finalizes the current production, pops from the stack previous production
     * and sets it as the current one. The element type of finalized production 
     * must be equal to the passed as a argument.
     * 
     * @param elementType - the composite element type to verify finalized production
     * 
     * @return AST node which represents the finalized production
     */
    public <T extends PstCompositeNode> T endParentProduction(CompositeType<T> elementType);
    
    
    
    /**
     * Changes the current production by replacing it with given one.
     * All children of the current production become the children of the new one. 
     * 
     * @param node - the replacing production 
     */
    public PstCompositeNode changeProduction(PstCompositeNode node);
    
    /**
     * Changes the current production by replacing it with new one corresponding 
     * to the composite element type. All children of the current production 
     * become the children of the new one. 
     * 
     * @param elementType - composite element type of the replacing production 
     */
    public <T extends PstCompositeNode> T changeProduction(CompositeType<T> elementType);
    
    
    
    /**
     * Drops the current production from the tree.
     * All children of the current production become the children of its parent.
     *  
     * @param node - the production to be dropped 
     */
    public void dropProduction(PstCompositeNode node);

    /**
     * Drops the current production from the tree.
     * All children of the current production become the children of its parent.
     *  
     * @param elementType - the element type of the dropped production
     */
    public void dropProduction(IElementType elementType);
    
    
    
    /**
     * Adds the given token to the AST
     * 
     * @param token - the token to be added 
     * @param offset - offset of the token's text region
     * @param length - the length of the token's text region
     */
    public void addToken(TokenType token, int offset, int length);
    
    
    /**
     * Adds the last token, if it was cached, to the current production. 
     */
    public void acceptLastToken();
    
}
