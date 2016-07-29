package com.excelsior.xds.parser.modula.ast.procedures;

import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.IProcedureDefinitionSymbol;


public class AstProcedureForwardDeclaration extends AstProcedure<IProcedureDefinitionSymbol> {

    public AstProcedureForwardDeclaration(ModulaCompositeType<AstProcedureForwardDeclaration> elementType) {
        super(null, elementType);
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
