package com.excelsior.xds.core.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsFormalParameter;
import com.excelsior.xds.core.model.IXdsProcedure;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.ProcedureType;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;

public class XdsProcedure extends    XdsElementWithDefinitions 
                          implements IXdsProcedure 
{
	private final IModulaSymbolReference<IProcedureSymbol> symbolRef;
	private final ProcedureType procedureType;

    private final List<IXdsFormalParameter> xdsParameters = new ArrayList<IXdsFormalParameter>(4);
	private final boolean isForwardDeclaration;
	
	public XdsProcedure( ProcedureType procedureType
	                   , IXdsProject xdsProject
	                   , IXdsCompilationUnit compilationUnit
	                   , IXdsContainer parent
	                   , String procedureName
	                   , IModulaSymbolReference<IProcedureSymbol> symbolRef
	                   , SourceBinding sourceBinding, boolean isForwardDeclaration ) 
	{
		super(xdsProject, compilationUnit, parent, procedureName, sourceBinding);
		
		this.symbolRef = symbolRef;
		this.procedureType = procedureType;
		this.isForwardDeclaration = isForwardDeclaration;

		allChildren.addComposited(xdsParameters);
	}

	@Override
	public IProcedureSymbol getSymbol() {
		return ReferenceUtils.resolve(symbolRef);
	}

	@Override
    public ProcedureType getProcedureType() {
        return procedureType;
    }
	
    @Override
    public Collection<IXdsFormalParameter> getParameters() {
        return xdsParameters;
    }
	
    public void addParameter(IXdsFormalParameter parameter) {
        xdsParameters.add(parameter);
    }

    @Override
	public boolean isForwardDeclaration() {
		return isForwardDeclaration;
	}
}