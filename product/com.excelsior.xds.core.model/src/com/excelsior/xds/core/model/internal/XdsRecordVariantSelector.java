package com.excelsior.xds.core.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsRecordVariant;
import com.excelsior.xds.core.model.IXdsRecordVariantSelector;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;
import com.excelsior.xds.parser.modula.symbol.IRecordVariantSelectorSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class XdsRecordVariantSelector extends    SimpleXdsElementWithSymbol<IRecordVariantSelectorSymbol> 
                                      implements IXdsRecordVariantSelector
{
    private final List<IXdsElement> variants = new ArrayList<IXdsElement>();
    

    public XdsRecordVariantSelector( String name, IXdsProject project
                                   , XdsCompilationUnit compilationUnit
                                   , IXdsContainer parent
                                   , IModulaSymbolReference<IRecordVariantSelectorSymbol> symbolRef
                                   , SourceBinding sourceBinding )
    {
        super(name, project, compilationUnit, parent, symbolRef, sourceBinding);
    }
    
    public synchronized void addVariant(IXdsRecordVariant variant) {
        variants.add(variant);
    }

    @Override
    public synchronized Collection<IXdsElement> getChildren() {
    	return CollectionsUtils.unmodifiableArrayList(variants);
    }
    
}
