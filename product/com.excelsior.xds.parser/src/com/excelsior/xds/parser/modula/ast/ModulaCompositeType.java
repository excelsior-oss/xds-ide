package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.ast.CompositeType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;

/**
 * Base class for node types in the AST tree. 
 */
public class ModulaCompositeType<T extends PstCompositeNode> extends CompositeType<T> 
                                                             implements IModulaElementType 
{
    public ModulaCompositeType(String debugName, Class<T> nodeClass) {
        super(debugName, nodeClass);
    }
    
}
