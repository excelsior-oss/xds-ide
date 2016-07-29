package com.excelsior.xds.parser.modula.ast.procedures;

import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.IProcedureDefinitionSymbol;

public class AstProcedureDefinition extends AstProcedure<IProcedureDefinitionSymbol> 
{
    public AstProcedureDefinition(ModulaCompositeType<? extends AstProcedureDefinition> elementType) {
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
