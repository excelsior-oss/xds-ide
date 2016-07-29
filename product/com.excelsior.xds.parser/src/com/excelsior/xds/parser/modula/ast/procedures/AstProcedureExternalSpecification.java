package com.excelsior.xds.parser.modula.ast.procedures;

import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;


public class AstProcedureExternalSpecification extends AstProcedureDefinition 
{
    public AstProcedureExternalSpecification(ModulaCompositeType<AstProcedureExternalSpecification> elementType) {
        super(elementType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
    	if (visitor.visit(this)) {
    		acceptChildren(visitor);
    	}
    }
    
}