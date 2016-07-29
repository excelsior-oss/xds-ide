package com.excelsior.xds.parser.modula.ast.procedures;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstFormalParameterDeclaration extends ModulaAstNode 
{
    public AstFormalParameterDeclaration(ModulaCompositeType<AstFormalParameterDeclaration> elementType) {
        super(null, elementType);
    }
    
    public AstFormalParameterList getAstFormalParameterList() {
        return findFirstChild( ModulaElementTypes.FORMAL_PARAMETER_LIST
                             , ModulaElementTypes.FORMAL_PARAMETER_LIST.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChild(visitor, getAstFormalParameterList());
        }
    }
    
}
