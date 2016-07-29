package com.excelsior.xds.parser.commons.pst;

import com.excelsior.xds.parser.commons.ast.TokenType;

/**
 * A leaf node in the Program Structure Tree. 
 * This nodes match individual tokens returned by scanner.
 */
public class PstLeafNode extends PstNode {

    /**
     * A character index into the original source string.
     */
    private final int offset;

    /**
     * A character length.
     */
    private final int length;
    
    
    private final TokenType token; 

    
    public PstLeafNode(PstCompositeNode parent, TokenType token, int offset, int length) {
        super(parent, token);
        this.token  = token;
        this.offset = offset;
        this.length = length;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getOffset() {
        return offset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLength() {
        return length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PstNode getPstNodeAt(int index) {
        boolean isThisNode = (offset <= index) && (index < (offset + length))
                          && (offset >= 0); 
        if (isThisNode) {
            return this;
        }
        return null;
    }

    
    public TokenType getToken() {
        return token;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(PstVisitor visitor) {
        visitor.visit(this);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return token.toString();
    }
    
}
