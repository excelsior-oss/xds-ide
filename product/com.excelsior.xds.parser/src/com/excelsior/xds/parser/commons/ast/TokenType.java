package com.excelsior.xds.parser.commons.ast;


/**
 * The base class for standard scanner token types common to all languages.
 */
public class TokenType extends ElementType {

    private final String designator; 

    /**
     * Creates a new token type.
     *
     * @param debugName the name of the token type, used for debugging purposes.
     * @param designator  human readable representation of the token type.
     */
    public TokenType(String debugName, String designator) {
        super(debugName);
        this.designator = designator;
    }

    public String getDesignator() {
        return designator;
    }
    
}
