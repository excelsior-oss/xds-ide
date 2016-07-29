package com.excelsior.xds.parser.modula.ast.variables;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstVariableDeclarationBlock extends ModulaAstNode 
{
    public AstVariableDeclarationBlock(ModulaCompositeType<AstVariableDeclarationBlock> elementType) {
        super(null, elementType);
    }

    public List<AstVariableDeclaration> getVariableDeclarations() {
        return findChildren(ModulaElementTypes.VARIABLE_DECLARATION);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getVariableDeclarations());
        }
    }

}
