package com.excelsior.xds.parser.modula.ast.pragmas;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

public class AstPragma extends ModulaAstNode 
{
    public AstPragma(ModulaCompositeType<? extends AstPragma> elementType) {
        super(null, elementType);
    }
}
