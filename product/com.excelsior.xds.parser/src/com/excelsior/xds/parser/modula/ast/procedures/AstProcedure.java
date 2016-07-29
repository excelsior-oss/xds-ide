package com.excelsior.xds.parser.modula.ast.procedures;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstSymbolDef;
import com.excelsior.xds.parser.modula.ast.IAstSymbolScope;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;

public abstract class      AstProcedure<T extends IProcedureSymbol> 
                extends    AstSymbolDef<T> 
                implements IAstSymbolScope, IAstNodeWithIdentifier
{
	protected AstProcedure(PstCompositeNode parent, ElementType elementType) {
		super(parent, elementType);
	}
    
    /**
     * {@inheritDoc}
     */
	@Override
    public PstNode getIdentifier() {
        return findFirstChild(ModulaElementTypes.PROCEDURE_IDENTIFIER, PstNode.class);
    }
    
    public AstResultType getAstResultType() {
        return findFirstChild( ModulaElementTypes.RESULT_TYPE
                             , ModulaElementTypes.RESULT_TYPE.getNodeClass() );
    }
    
    public AstFormalParameterBlock getProcedureParameters() {
        return findFirstChild( ModulaElementTypes.FORMAL_PARAMETER_BLOCK
                             , ModulaElementTypes.FORMAL_PARAMETER_BLOCK.getNodeClass() );
    }
    
    protected void acceptChildren(ModulaAstVisitor visitor) {
        acceptChild(visitor, getProcedureParameters());
        acceptChild(visitor, getAstResultType());
	}
 
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbolScope getScope() {
        return getSymbol();
    }
    
}
