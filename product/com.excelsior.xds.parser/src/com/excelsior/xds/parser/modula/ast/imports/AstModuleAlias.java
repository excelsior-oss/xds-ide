package com.excelsior.xds.parser.modula.ast.imports;

import com.excelsior.xds.parser.modula.ast.AstNameAlias;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;

public class AstModuleAlias extends AstNameAlias<IModuleAliasSymbol> 
{
    public AstModuleAlias(ModulaCompositeType<AstModuleAlias> elementType) {
        super(null, elementType);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
    	visitor.visit(this);
    }

}
