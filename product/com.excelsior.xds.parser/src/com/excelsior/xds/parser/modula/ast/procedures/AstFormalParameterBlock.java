package com.excelsior.xds.parser.modula.ast.procedures;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstFormalParameterBlock extends ModulaAstNode 
{
    public AstFormalParameterBlock(ModulaCompositeType<AstFormalParameterBlock> elementType) {
        super(null, elementType);
    }
    
    public List<AstFormalParameterDeclaration> getAstFormalParameterDeclaration() {
        return findChildren(ModulaElementTypes.FORMAL_PARAMETER_DECLARATION);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getAstFormalParameterDeclaration());
        }
    }
    
}
