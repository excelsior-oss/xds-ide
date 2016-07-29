package com.excelsior.xds.ui.editor.modula.outline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.excelsior.xds.core.model.IXdsCompilationUnit;
import com.excelsior.xds.core.model.IXdsConstant;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsModule;
import com.excelsior.xds.core.model.IXdsProcedure;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsType;
import com.excelsior.xds.core.model.IXdsVariable;
import com.excelsior.xds.core.model.LoadingXdsElement;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.text.TextRegion;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

/**
 * Root of outline content being loaded 
 */
public class LoadingContentRoot implements IXdsModule
{
    private static final SourceBinding SOURCE_BINDING = new SourceBinding(new TextRegion(0, 0), new TextRegion(0, 0));
	private final String name;
    private final List<IXdsElement> children;

    public LoadingContentRoot(String name) {
        this(name, name);
    }
    
    public LoadingContentRoot(String name, String childName) {
        this.name = name;
        children = new ArrayList<IXdsElement>(1);
        children.add(new LoadingXdsElement(childName, null, this));
    }

    @Override
    public String getElementName() {
        return name;
    }

    @Override
    public Collection<IXdsElement> getChildren() {
        return children;
    }

    
    @Override
    public IXdsCompilationUnit getCompilationUnit() {
        return null;
    }

    @Override
    public Collection<IXdsModule> getModules() {
        return Collections.emptyList();
    }

    @Override
    public Collection<IXdsProcedure> getProcedures() {
        return Collections.emptyList();
    }

    @Override
    public Collection<IXdsVariable> getVariables() {
        return Collections.emptyList();
    }

    @Override
    public Collection<IXdsConstant> getConstants() {
        return Collections.emptyList();
    }

    @Override
    public Collection<IXdsType> getTypes() {
        return Collections.emptyList();
    }

    @Override
    public IXdsProject getXdsProject() {
        return null;
    }

    @Override
    public IXdsContainer getParent() {
        return null;
    }

    @Override
    public SourceBinding getSourceBinding() {
        return SOURCE_BINDING;
    }

    @Override
    public IModuleSymbol getSymbol() {
        return null;
    }

}
