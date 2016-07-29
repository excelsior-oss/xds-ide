package com.excelsior.xds.parser.commons.pst;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.ast.IElementType;

/**
 * A node in the Program Structure Tree.
 */
public abstract class PstNode 
{
    public static final int UNKNOW_OFFSET = -1;
    
    private PstCompositeNode parent;
    
    private IElementType elementType;

    
    public PstNode(PstCompositeNode parent, IElementType elementType2) {
        this.parent      = parent;
        this.elementType = elementType2;
    }

    public PstNode(ElementType elementType) {
        this(null, elementType);
    }
    
    public IElementType getElementType() {
        return elementType;
    }
    
    public void setElementType(IElementType elementType) {
        this.elementType = elementType;
    }
    
    
    /**
     * Returns the character index into the original source file indicating
     * where the source fragment corresponding to this node begins.
     * <p>
     * The parser supplies useful well-defined source ranges to the nodes it creates.
     * See {@link ASTParser#setKind(int)} for details
     * on precisely where source ranges begin and end.
     * </p>
     *
     * @return the 0-based character index, or <code>-1</code>
     *    if no source position information is recorded for this node
     * @see #getLength()
     * @see ASTParser
     */
    public abstract int getOffset();

    /**
     * Returns the length in characters of the original source file indicating
     * where the source fragment corresponding to this node ends.
     * <p>
     * The parser supplies useful well-defined source ranges to the nodes it creates.
     * See {@link ASTParser#setKind(int)} methods for details
     * on precisely where source ranges begin and end.
     * </p>
     *
     * @return a (possibly 0) length, or <code>0</code>
     *    if no source position information is recorded for this node
     * @see #getOffset()
     * @see ASTParser
     */
    public abstract  int getLength();

    /**
     * Returns the <code>PstNode</code> at the specified character index 
     * in the original source file.
     * 
     * @param offset the 0-based character index of the <code>PstNode</code> to return
     * @return the <code>PstNode</code> at the specified character index or
     *         <tt>null</tt> if <code>PstNode</code> was not found.
     */
    public abstract PstNode getPstNodeAt(int offset);
    
    
    public PstCompositeNode getParent() {
        return parent;
    }
    
    public void setParent(PstCompositeNode parent) {
        this.parent = parent;
    }

    
    /**
     * Accepts the given visitor on a visit of the current node.
     *
     * @param visitor the visitor object
     */
    public void accept(PstVisitor visitor) {
        // begin with the generic pre-visit
        if (visitor.preVisit(this)) {
            // dynamic dispatch to internal method for type-specific visit/endVisit
            doAccept(visitor);
        }
        // end with the generic post-visit
        visitor.postVisit(this);
    }
    
    /**
     * Accepts the given visitor on a type-specific visit of the current node.
     * This method must be implemented in all concrete PST node types.
     * 
     * @param visitor the visitor object
     */
    protected abstract void doAccept(PstVisitor visitor);

    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return elementType.toString();
    }
}
