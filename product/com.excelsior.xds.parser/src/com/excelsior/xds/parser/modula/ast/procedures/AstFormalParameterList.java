package com.excelsior.xds.parser.modula.ast.procedures;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstFormalParameterList extends ModulaAstNode 
{
    public AstFormalParameterList(ModulaCompositeType<AstFormalParameterList> elementType) {
        super(null, elementType);
    }
    
    public List<AstFormalParameter> getFormalParameters() {
        return findChildren(ModulaElementTypes.FORMAL_PARAMETER);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getFormalParameters());
        }
    }
    
}
