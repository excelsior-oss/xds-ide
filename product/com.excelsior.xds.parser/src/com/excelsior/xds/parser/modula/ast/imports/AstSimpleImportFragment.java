package com.excelsior.xds.parser.modula.ast.imports;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstModuleName;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstSimpleImportFragment extends AstImportFragment implements IAstNodeWithIdentifier
{
    public AstSimpleImportFragment(ModulaCompositeType<AstSimpleImportFragment> elementType) {
        super(null, elementType);
    }
    
    public AstModuleName getAstModuleName() {
    	return findFirstChild( ModulaElementTypes.MODULE_NAME
    	                     , ModulaElementTypes.MODULE_NAME.getNodeClass() );
    }
    
    public AstImportAliasDeclaration getImportAliasDeclaration() {
    	return findFirstChild( ModulaElementTypes.ALIAS_DECLARATION
    	                     , ModulaElementTypes.ALIAS_DECLARATION.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
    	if (visitor.visit(this)) {
    		acceptChild(visitor, getAstModuleName());
    		acceptChild(visitor, getImportAliasDeclaration());
    	}
    }

	@Override
	public PstNode getIdentifier() {
		return getAstModuleName();
	}
}
