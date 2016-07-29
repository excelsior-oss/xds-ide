package com.excelsior.xds.parser.modula.ast.imports;

import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstSimpleImportStatement extends AstImportStatement 
{
    public AstSimpleImportStatement(ModulaCompositeType<AstSimpleImportStatement> elementType) {
        super(null, elementType);
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
    }
    
}
