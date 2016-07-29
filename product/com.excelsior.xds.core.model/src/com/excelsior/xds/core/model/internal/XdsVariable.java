package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsVariable;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class XdsVariable extends    XdsElementWithAnonymusCompositeType<IVariableSymbol> 
                         implements IXdsVariable
{
	public XdsVariable( IXdsProject project, XdsCompilationUnit compilationUnit
	                  , String name, IXdsContainer parent
	                  , IModulaSymbolReference<IVariableSymbol > symbolRef, SourceBinding sourceBinding ) 
	{
		super(name, project, compilationUnit, parent, symbolRef, sourceBinding);
	}
	
}