package com.excelsior.xds.core.model.internal;

import java.util.Collection;
import java.util.Collections;

import com.excelsior.xds.core.model.IXdsCompositeType;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class XdsElementWithAnonymusCompositeType<T extends IModulaSymbol> 
       extends    SimpleXdsElementWithSymbol<T>
       implements IXdsContainer
{
    private IXdsCompositeType anonymousType;
    
    public XdsElementWithAnonymusCompositeType( String name, IXdsProject project
                                              , XdsCompilationUnit compilationUnit
                                              , IXdsContainer parent
                                              , IModulaSymbolReference<T> symbolRef
                                              , SourceBinding sourceBinding ) 
    {
        super(name, project, compilationUnit, parent, symbolRef, sourceBinding);
    }
    
    synchronized void setAnonymousType(IXdsCompositeType anonymousType) {
        this.anonymousType = anonymousType;
    }

    @Override
    public synchronized Collection<IXdsElement> getChildren() {
        if (anonymousType != null) {
            return anonymousType.getChildren();
        }
        return Collections.emptyList();
    }

}
