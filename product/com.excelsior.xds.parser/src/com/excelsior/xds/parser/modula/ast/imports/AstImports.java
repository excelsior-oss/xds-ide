package com.excelsior.xds.parser.modula.ast.imports;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.excelsior.xds.parser.modula.ast.AstBlock;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstImports extends AstBlock
{
    public AstImports(ModulaCompositeType<AstImports> elementType) {
        super(null, elementType);
    }

    public List<AstImportStatement> getAstImportStatements() {
        return findChildren(newImportElementHasSet(), AstImportStatement.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getAstImportStatements());
        }
    }

    
    private static Set<ModulaCompositeType<? extends AstImportStatement>> newImportElementHasSet() 
    {
        Set<ModulaCompositeType<? extends AstImportStatement>> set = 
                new HashSet<ModulaCompositeType<? extends AstImportStatement>>();
        set.add(ModulaElementTypes.SIMPLE_IMPORT);
        set.add(ModulaElementTypes.UNQUALIFIED_IMPORT);
        return set;
    }

}
