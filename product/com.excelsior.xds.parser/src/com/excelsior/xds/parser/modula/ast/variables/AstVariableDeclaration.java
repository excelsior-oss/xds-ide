package com.excelsior.xds.parser.modula.ast.variables;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.types.AstTypeElement;

public class AstVariableDeclaration extends ModulaAstNode 
{
    public AstVariableDeclaration(ModulaCompositeType<AstVariableDeclaration> elementType) {
        super(null, elementType);
    }

    public List<AstVariableList> getVariableList() {
        return findChildren(ModulaElementTypes.VARIABLE_LIST);
    }
    
    public AstTypeElement getAstTypeElement() {
        return findFirstChild( ModulaElementTypes.TYPE_ELEMENT
                             , ModulaElementTypes.TYPE_ELEMENT.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getVariableList());
            acceptChild(visitor, getAstTypeElement());
        }
    }

}
