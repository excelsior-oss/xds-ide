package com.excelsior.xds.core.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.collection.CompositeCollection;

import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsConstant;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsElementWithDefinitions;
import com.excelsior.xds.core.model.IXdsImportSection;
import com.excelsior.xds.core.model.IXdsModule;
import com.excelsior.xds.core.model.IXdsProcedure;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsType;
import com.excelsior.xds.core.model.IXdsVariable;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public abstract class XdsElementWithDefinitions extends    SourceBoundXdsElement
                                                implements IXdsElementWithDefinitions 
{
	protected final List<IXdsImportSection> importSections = new ArrayList<IXdsImportSection>();
	private final List<IXdsModule> xdsLocalModules = new ArrayList<IXdsModule>();
	private final List<IXdsProcedure> xdsProcedures = new ArrayList<IXdsProcedure>();
	private final List<IXdsVariable> xdsVariables = new ArrayList<IXdsVariable>();
	private final List<IXdsConstant> xdsConstants = new ArrayList<IXdsConstant>();
	private final List<IXdsType> xdsTypes = new ArrayList<IXdsType>();
	
	protected final CompositeCollection allChildren = new CompositeCollection();
	
	private final IXdsCompilationUnit compilationUnit;
	
	public XdsElementWithDefinitions( IXdsProject xdsProject
	                                , IXdsCompilationUnit compilationUnit
	                                , IXdsContainer parent
	                                , String elementName
	                                , SourceBinding sourceBinding ) 
	{
        super(elementName, xdsProject, parent, sourceBinding);
		this.compilationUnit = compilationUnit;
		
		allChildren.addComposited(importSections);
		allChildren.addComposited(xdsLocalModules);
		allChildren.addComposited(xdsProcedures);
		allChildren.addComposited(xdsVariables);
		allChildren.addComposited(xdsConstants);
		allChildren.addComposited(xdsTypes);
	}
	
	@Override
    public String getElementName() {
	    IModulaSymbol symbol = getSymbol();
        return symbol != null? symbol.getName() : super.getElementName();
    }
	
	@Override
	public IXdsCompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

	@Override
    @SuppressWarnings("unchecked")
	public synchronized Collection<IXdsElement> getChildren() {
		return CollectionsUtils.unmodifiableArrayList(allChildren);
	}

	@Override
	public synchronized Collection<IXdsModule> getModules() {
		return CollectionsUtils.unmodifiableArrayList(xdsLocalModules);
	}
	
	public synchronized void addLocalModule(IXdsModule module) {
		xdsLocalModules.add(module);
	}

	@Override
	public synchronized Collection<IXdsProcedure> getProcedures() {
		return CollectionsUtils.unmodifiableArrayList(xdsProcedures);
	}
	
	public synchronized void addProcedure(IXdsProcedure procedure) {
		xdsProcedures.add(procedure);
	}

	@Override
	public synchronized Collection<IXdsVariable> getVariables() {
		return CollectionsUtils.unmodifiableArrayList(xdsVariables);
	}
	
	public synchronized void addVariable(IXdsVariable variable) {
		xdsVariables.add(variable);
	}

	@Override
	public synchronized Collection<IXdsConstant> getConstants() {
		return CollectionsUtils.unmodifiableArrayList(xdsConstants);
	}
	
	public synchronized void addConstant(IXdsConstant constant) {
		xdsConstants.add(constant);
	}

	@Override
	public synchronized Collection<IXdsType> getTypes() {
		return CollectionsUtils.unmodifiableArrayList(xdsTypes);
	}
	
	public synchronized void addType(IXdsType type) {
		xdsTypes.add(type);
	}

}
