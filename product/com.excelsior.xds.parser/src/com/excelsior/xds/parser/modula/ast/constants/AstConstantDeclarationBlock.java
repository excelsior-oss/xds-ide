package com.excelsior.xds.parser.modula.ast.constants;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstConstantDeclarationBlock extends ModulaAstNode 
{
    public AstConstantDeclarationBlock(ModulaCompositeType<AstConstantDeclarationBlock> elementType) {
        super(null, elementType);
    }
    
    public List<AstConstantDeclaration> getConstantDeclarations() {
    	return findChildren( ModulaElementTypes.CONSTANT_DECLARATION
    	                   , ModulaElementTypes.CONSTANT_DECLARATION.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
    	 boolean visitChildren = visitor.visit(this);
         if (visitChildren) {
             acceptChildren(visitor, getConstantDeclarations());
         }
    }
    
}
