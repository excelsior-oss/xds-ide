package com.excelsior.xds.parser.modula.ast.imports;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.modules.AstModuleBody;

public class AstImportFragmentList extends ModulaAstNode 
{
    public AstImportFragmentList(ModulaCompositeType<AstModuleBody> elementType) {
        super(null, elementType);
    }

    public List<AstSimpleImportFragment> getSimpleImportFragments() {
        return findChildren(ModulaElementTypes.SIMPLE_IMPORT_FRAGMENT);
    }

    public List<AstUnqualifiedImportFragment> getUnqualifiedImportFragments() {
        return findChildren(ModulaElementTypes.UNQUALIFIED_IMPORT_FRAGMENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChildren(visitor, getSimpleImportFragments());
            acceptChildren(visitor, getUnqualifiedImportFragments());
        }
    }
	
}
