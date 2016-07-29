package com.excelsior.xds.parser.modula.ast.types;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstTypeDeclarationBlock extends ModulaAstNode 
{
    public AstTypeDeclarationBlock(ModulaCompositeType<AstTypeDeclarationBlock> elementType) {
        super(null, elementType);
    }

    public List<AstTypeDeclaration> getTypeDeclarations() {
        return findChildren( ModulaElementTypes.TYPE_DECLARATION
                           , ModulaElementTypes.TYPE_DECLARATION.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
         boolean visitChildren = visitor.visit(this);
         if (visitChildren) {
             acceptChildren(visitor, getTypeDeclarations());
         }
    }
    
}
