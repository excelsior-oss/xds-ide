package com.excelsior.xds.parser.modula.ast.imports;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstModuleName;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;


public class AstUnqualifiedImportStatement extends AstImportStatement implements IAstNodeWithIdentifier
{
    public AstUnqualifiedImportStatement(ModulaCompositeType<AstUnqualifiedImportStatement> elementType) {
        super(null, elementType);
    }
    
    public AstModuleName getModuleIdentifier() {
        return findFirstChild( ModulaElementTypes.MODULE_NAME
                             , ModulaElementTypes.MODULE_NAME.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        if (visitor.visit(this)) {
            AstImportFragmentList importFragmentList = findFirstChild(
                    ModulaElementTypes.IMPORT_FRAGMENT_LIST, 
                    ModulaElementTypes.IMPORT_FRAGMENT_LIST.getNodeClass() );
            acceptChild(visitor, importFragmentList);
        }
        visitor.postVisit(this);
    }

    @Override
    public PstNode getIdentifier() {
        return getModuleIdentifier();
    }
}
