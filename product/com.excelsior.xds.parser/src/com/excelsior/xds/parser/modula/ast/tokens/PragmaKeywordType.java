package com.excelsior.xds.parser.modula.ast.tokens;

/**
 * Base class for token corresponding to Modula-2/Oberon-2 compiler pragma keywords.
 */
public class PragmaKeywordType extends PragmaTokenType {

    public PragmaKeywordType(String debugName) {
        super(debugName + "_PragmaKeyword", debugName);    //$NON-NLS-1$
    }

}
