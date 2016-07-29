package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.ast.ElementType;

/**
 * Base class for token types returned from lexical analysis and for types
 * of nodes in the AST tree for Modula-2/Oberon-2 languages. 
 */
public class ModulaElementType extends ElementType implements IModulaElementType {

    public ModulaElementType(String debugName) {
        super(debugName);
    }

}
