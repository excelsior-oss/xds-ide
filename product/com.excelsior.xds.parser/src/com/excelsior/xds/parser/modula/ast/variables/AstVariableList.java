package com.excelsior.xds.parser.modula.ast.variables;

import java.util.List;

import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.types.AstTypeElement;

public class AstVariableList extends ModulaAstNode 
{
    public AstVariableList(ModulaCompositeType<AstVariableList> elementType) {
        super(null, elementType);
    }
    
    public List<AstVariable> getVariables() {
        return findChildren(ModulaElementTypes.VARIABLE);
    }
    
    public AstVariableDeclaration getVariableDeclaration() {
        return ModulaAst.findParent(this, ModulaElementTypes.VARIABLE_DECLARATION);
    }
    
    public AstTypeElement getAstTypeElement() {
        AstTypeElement typeElementAst = null;
        PstNode node = getParent();
        if (node instanceof AstVariableDeclaration) {
            typeElementAst = ((AstVariableDeclaration)node).getAstTypeElement();
        }
        return typeElementAst;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getVariables());
        }
    }
    
}
