package com.excelsior.xds.parser.modula.ast.imports;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstModuleName;
import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstImportAliasDeclaration extends ModulaAstNode implements IAstNodeWithIdentifier
{
    public AstImportAliasDeclaration(ModulaCompositeType<AstImportAliasDeclaration> elementType) {
        super(null, elementType);
    }
	
    public AstModuleAlias getModuleAlias() {
        return findFirstChild( ModulaElementTypes.MODULE_ALIAS_NAME
                             , ModulaElementTypes.MODULE_ALIAS_NAME.getNodeClass() );
    }
	
    public AstModuleName getAstModuleName() {
        return findFirstChild( ModulaElementTypes.MODULE_NAME
    	                     , ModulaElementTypes.MODULE_NAME.getNodeClass() );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getModuleAlias());
            acceptChild(visitor, getAstModuleName());
        }
    }

	@Override
	public PstNode getIdentifier() {
		return getModuleAlias();
	}
}