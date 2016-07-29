package com.excelsior.xds.parser.modula.ast.statements;

import com.excelsior.xds.parser.modula.ast.IAstSymbolScope;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;

/**
 * WithStatement = "WITH" RecordDesignator "DO" StatementSequence "END" <br>
 */
public class AstModulaWithStatement extends    AstStatement
                                    implements IAstSymbolScope
{
    private IModulaSymbolScope scope; 
    
    public AstModulaWithStatement(ModulaCompositeType<AstModulaWithStatement> elementType) {
        super(null, elementType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbolScope getScope() {
        return scope;
    }
    
    public void setScope(IModulaSymbolScope scope) {
        this.scope = scope;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFrameName() {
        return "WITH";
    }
}
