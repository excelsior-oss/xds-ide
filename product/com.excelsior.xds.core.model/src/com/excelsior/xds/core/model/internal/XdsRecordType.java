package com.excelsior.xds.core.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsProcedure;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsRecordField;
import com.excelsior.xds.core.model.IXdsRecordType;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;

public class XdsRecordType extends    SimpleXdsElementWithSymbol<IRecordTypeSymbol> 
                           implements IXdsRecordType 
{
	private final List<IXdsRecordField> fields = new ArrayList<IXdsRecordField>();
    private final List<IXdsProcedure> methods = new ArrayList<IXdsProcedure>();

    public XdsRecordType( IXdsProject project, XdsCompilationUnit compilationUnit
            , IXdsContainer parent
            , String name, IModulaSymbolReference<IRecordTypeSymbol> symbolRef
            , SourceBinding sourceBinding) {
    	this(project, compilationUnit, parent, name, true, symbolRef, sourceBinding);
    }
	
	public XdsRecordType( IXdsProject project, XdsCompilationUnit compilationUnit
	                    , IXdsContainer parent
			            , String name, boolean isUseNameFromSymbol, IModulaSymbolReference<IRecordTypeSymbol> symbolRef
			            , SourceBinding sourceBinding ) 
	{
        super(name, isUseNameFromSymbol, project, compilationUnit, parent, symbolRef, sourceBinding);        
	}

	@Override
	public synchronized Collection<IXdsElement> getChildren() {
		Collection<IXdsElement> children = new ArrayList<IXdsElement>();
		children.addAll(getFields());
	    children.addAll(getProcedures());
		return children;
	}

	
	public synchronized void addField(IXdsRecordField field) {
        fields.add(field);
    }

    @Override
    public synchronized Collection<IXdsRecordField> getFields() {
    	return CollectionsUtils.unmodifiableArrayList(fields);
    }


	public synchronized void addProcedure(XdsProcedure procedureElement) {
        methods.add(procedureElement);
    }

    @Override
    public synchronized Collection<IXdsProcedure> getProcedures() {
    	return CollectionsUtils.unmodifiableArrayList(methods);
    }
    
    @Override
	public SourceBinding getSourceBinding() {
    	SourceBinding sourceBinding = super.getSourceBinding();
		if (sourceBinding == null) {
			return null;
		}
		
		List<ITextRegion> elementRegions = new ArrayList<ITextRegion>();
		Collection<IXdsProcedure> procedures = getProcedures();
		for (IXdsProcedure proc : procedures) {
			SourceBinding procSourceBinding = proc.getSourceBinding();
			if (procSourceBinding != null) {
				elementRegions.addAll(procSourceBinding.getElementRegions());
			}
		}
		
		elementRegions.addAll(sourceBinding.getElementRegions());
		
		return new SourceBinding(elementRegions, sourceBinding.getNameTextRegion(), sourceBinding.getDeclarationTextRegion());
	}
}