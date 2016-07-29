package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsRecordField;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class XdsRecordField extends    XdsElementWithAnonymusCompositeType<IRecordFieldSymbol> 
                            implements IXdsRecordField
{
	public XdsRecordField( String name, IXdsProject project
	                     , XdsCompilationUnit compilationUnit
	                     , IXdsContainer parent
	                     , IModulaSymbolReference<IRecordFieldSymbol> symbolRef
	                     , SourceBinding sourceBinding ) 
	{
		super(name, project, compilationUnit, parent, symbolRef, sourceBinding);
	}
	
}
