package com.excelsior.xds.parser.modula.ast.imports;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstSimpleName;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;


public class AstUnqualifiedImportFragment extends AstImportFragment implements IAstNodeWithIdentifier 
{
    public AstUnqualifiedImportFragment(ModulaCompositeType<AstUnqualifiedImportFragment> elementType) {
        super(null, elementType);
    }

    public AstSimpleName getAstSimpleName() {
        return findFirstChild( ModulaElementTypes.SIMPLE_NAME
                             , ModulaElementTypes.SIMPLE_NAME.getNodeClass() );
    }
    
    public AstUnqualifiedImportStatement getUnqualifiedImportStatement() {
    	PstNode parent = getParent();
    	while(parent != null) {
    		if (parent.getElementType() == ModulaElementTypes.UNQUALIFIED_IMPORT) {
    			return (AstUnqualifiedImportStatement)parent;
    		}
    		parent = parent.getParent();
    	}
    	return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getAstSimpleName());
        }
    }

	@Override
	public PstNode getIdentifier() {
		return getAstSimpleName();
	}
}
