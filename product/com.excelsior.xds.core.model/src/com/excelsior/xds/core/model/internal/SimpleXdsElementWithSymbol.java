package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElementWithSymbol;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;

public abstract class SimpleXdsElementWithSymbol<T extends IModulaSymbol> 
                extends    SourceBoundXdsElement 
                implements IXdsElementWithSymbol 
{
	private final IModulaSymbolReference<T> symbolRef;
	private final XdsCompilationUnit compilationUnit;
	
	/**
	 * Whether getElementName should be calculated from the symbol
	 */
	private final boolean isUseNameFromSymbol; 
	
	public SimpleXdsElementWithSymbol( String name, IXdsProject project
            , XdsCompilationUnit compilationUnit
            , IXdsContainer parent
            , IModulaSymbolReference<T> symbolRef
            , SourceBinding sourceBinding){
		this(name, true, project, compilationUnit, parent, symbolRef, sourceBinding);
	}

    public SimpleXdsElementWithSymbol( String name, boolean isUseNameFromSymbol, IXdsProject project
			                         , XdsCompilationUnit compilationUnit
                                     , IXdsContainer parent
                                     , IModulaSymbolReference<T> symbolRef
                                     , SourceBinding sourceBinding ) 
	{
		super(name, project, parent, sourceBinding);
		this.compilationUnit = compilationUnit;
		this.symbolRef = symbolRef;
		
		@SuppressWarnings("unchecked")
		IModulaSymbolReference<IModulaSymbol> modulaSymbolRef = (IModulaSymbolReference<IModulaSymbol>) symbolRef;
        compilationUnit.mapSymbol2XdsElement(modulaSymbolRef, this);
        this.isUseNameFromSymbol = isUseNameFromSymbol;
	}

	@Override
	public T getSymbol() {
		return ReferenceUtils.resolve(symbolRef);
	}
	
	@Override
    public String getElementName() {
		T symbol = getSymbol();
		if (!isUseNameFromSymbol || symbol == null) {
			return super.getElementName();
		}
        return symbol.getName();
    }
	
    public IXdsCompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

}
