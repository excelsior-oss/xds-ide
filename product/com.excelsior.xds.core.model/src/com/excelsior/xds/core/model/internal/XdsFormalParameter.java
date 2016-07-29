package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsFormalParameter;
import com.excelsior.xds.core.model.IXdsProcedure;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class XdsFormalParameter extends    SimpleXdsElementWithSymbol<IFormalParameterSymbol>
                                implements IXdsFormalParameter
{
    public XdsFormalParameter( IXdsProject project
                             , XdsCompilationUnit compilationUnit
                             , IXdsProcedure parent
                             , String name 
                             , IModulaSymbolReference<IFormalParameterSymbol> symbolRef
                             , SourceBinding sourceBinding ) 
    {
        super(name, project, compilationUnit, parent, symbolRef, sourceBinding);
    }
    
}
