package com.excelsior.xds.parser.commons.ast;

/**
 * Base class for token types returned from lexical analysis and for types
 * of nodes in the AST tree. 
 */
public class ElementType implements IElementType {

    private final String debugName; 

    /**
     * Creates  a new element type.
     *
     * @param debugName the name of the element type, used for debugging purposes.
     */
    public ElementType(String debugName) {
        this.debugName = debugName;
    }
    
    /**
     * Returns a string representation of the element.
     * 
     * @return  a string representation of the element.
     */
    @Override
    public String toString() {
        return debugName;
    }
    
}
