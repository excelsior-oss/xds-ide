package com.excelsior.xds.parser.modula.symbol;

import java.util.HashSet;
import java.util.Set;

import com.excelsior.xds.parser.internal.modula.symbol.FinallyBodySymbol;
import com.excelsior.xds.parser.modula.symbol.type.IArrayTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IForwardTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IInvalidTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.INumericalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOpaqueTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IOrdinalTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IProcedureTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSynonymSymbol;
import com.excelsior.xds.parser.modula.type.NumericalType;

public abstract class ModulaSymbolVisitor 
{
	// stores already visited symbols, to handle the cyclic references
	private Set<IModulaSymbol> visitedSymbols = new HashSet<>();
	
	/**
	 * Must be called if the same instance is used for the traversal again.
	 */
	public void reset() {
		visitedSymbols.clear();
	}
	
	public boolean preVisit(IModulaSymbol symbol) {
        return visitedSymbols.add(symbol);
    }
	
	public void postVisit(IModulaSymbol symbol) {
    }
	
	protected boolean isVisitChildren(IModulaSymbol symbol) {
		return true;
	}
	
	public boolean visit(ITypeSynonymSymbol symbol) {
		return isVisitChildren(symbol);
	}
	
	public boolean visit(IDefinitionModuleSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IFormalParameterSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IModuleAliasSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IOberonMethodReceiverSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IRecordFieldSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IStandardProcedureSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IProcedureDefinitionSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IProcedureTypeSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IRecordTypeSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IStandardModuleSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IProcedureDeclarationSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IImplemantationModuleSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(ILocalModuleSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IMainModuleSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IConstantSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IVariableSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IArrayTypeSymbol symbol) {
		return isVisitChildren(symbol);
	}
	
	public boolean visit(ITypeSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IForwardTypeSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IInvalidTypeSymbol symbol) {
		return false;
	}

	public boolean visit(IOpaqueTypeSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IOrdinalTypeSymbol symbol) {
		return isVisitChildren(symbol);
	}
	
	public boolean visit(INumericalTypeSymbol<NumericalType> symbol) {
		return isVisitChildren(symbol);
	}
	
	public boolean visit(IEnumTypeSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IPointerTypeSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IProgramModuleSymbol symbol) {
		return isVisitChildren(symbol);
	}
	
	public boolean visit(IOberonMethodDefinitionSymbol symbol) {
		return isVisitChildren(symbol);
	}

	public boolean visit(IOberonMethodDeclarationSymbol symbol) {
		return isVisitChildren(symbol);
	}

    public boolean visit(IModuleBodySymbol symbol) {
        return isVisitChildren(symbol);
    }

    public boolean visit(IProcedureBodySymbol symbol) {
        return isVisitChildren(symbol);
    }

    public boolean visit(FinallyBodySymbol symbol) {
        return isVisitChildren(symbol);
    }

    public boolean visit(IInvalidModulaSymbol symbol) {
        return isVisitChildren(symbol);
   }
}