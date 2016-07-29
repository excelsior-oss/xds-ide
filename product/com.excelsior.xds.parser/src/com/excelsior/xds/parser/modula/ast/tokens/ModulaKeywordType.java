package com.excelsior.xds.parser.modula.ast.tokens;


/**
 * Base class for token corresponding to Modula-2/Oberon-2 keywords.
 */
public class ModulaKeywordType extends ModulaTokenType {

    public ModulaKeywordType(String debugName) {
        super(debugName + "_Keyword", debugName);    //$NON-NLS-1$
    }

}
