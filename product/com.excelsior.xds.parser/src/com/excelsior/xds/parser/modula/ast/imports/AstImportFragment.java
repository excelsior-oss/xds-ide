package com.excelsior.xds.parser.modula.ast.imports;

import com.excelsior.xds.parser.commons.ast.ElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.modula.ast.AstSymbolRef;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public abstract class AstImportFragment extends AstSymbolRef<IModulaSymbol> 
{
    protected AstImportFragment(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }
    
    public AstImports getImports() {
        return ModulaAst.findParent(this, ModulaElementTypes.IMPORTS);
    }
}
