package com.excelsior.xds.parser.modula.ast.tokens;

import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.modula.ast.IModulaElementType;

/**
 * Base class for Modula-2/Oberon-2 tokens.
 */
public class ModulaTokenType extends TokenType implements IModulaElementType {

    public ModulaTokenType(String debugName, String designator) {
        super(debugName, designator);
    }
    
}
