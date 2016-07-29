package com.excelsior.xds.parser.modula.ast.modules;

import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

public class AstLocalModule extends AstProgramModule 
{
    public AstLocalModule(ModulaCompositeType<AstLocalModule> elementType) {
        super(elementType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor);
        }
    }
    
}
