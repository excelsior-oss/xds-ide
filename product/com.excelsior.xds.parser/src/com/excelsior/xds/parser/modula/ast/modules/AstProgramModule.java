package com.excelsior.xds.parser.modula.ast.modules;

import com.excelsior.xds.parser.modula.ast.AstDeclarations;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstProgramModule extends AstModule 
{
    public AstProgramModule(ModulaCompositeType<? extends AstProgramModule> elementType) {
        super(elementType);
    }

    public AstModuleBody getModuleBody() {
        return findFirstChild( ModulaElementTypes.MODULE_BODY
                             , ModulaElementTypes.MODULE_BODY.getNodeClass() );
    }
    
    public AstFinallyBody getFinallyBody() {
        return findFirstChild( ModulaElementTypes.FINALLY_BODY
                             , ModulaElementTypes.FINALLY_BODY.getNodeClass() );
    }

    public AstDeclarations getAstDeclarations() {
        return findFirstChild( ModulaElementTypes.DECLARATIONS
                             , ModulaElementTypes.DECLARATIONS.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void acceptChildren(ModulaAstVisitor visitor) {
        super.acceptChildren(visitor);
        acceptChild(visitor, getAstDeclarations());
        acceptChild(visitor, getModuleBody());
        acceptChild(visitor, getFinallyBody());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor);
        }
    }
    
}
