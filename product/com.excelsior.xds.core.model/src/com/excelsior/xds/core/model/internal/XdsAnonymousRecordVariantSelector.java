package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsAnonymousRecordVariantSelector;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.model.internal.nls.Messages;
import com.excelsior.xds.parser.modula.symbol.IRecordVariantSelectorSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class XdsAnonymousRecordVariantSelector extends    XdsRecordVariantSelector
                                               implements IXdsAnonymousRecordVariantSelector
{
    public XdsAnonymousRecordVariantSelector( IXdsProject project
                                            , XdsCompilationUnit compilationUnit
                                            , IXdsContainer parent
                                            , IModulaSymbolReference<IRecordVariantSelectorSymbol> symbolRef
                                            , SourceBinding sourceBinding )
    {
        super( Messages.XdsRecordVariantSelector_Name
             , project, compilationUnit, parent
             , symbolRef, sourceBinding );
    }

}
