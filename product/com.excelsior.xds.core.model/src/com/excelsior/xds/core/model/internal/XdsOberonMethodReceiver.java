package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsOberonMethodReceiver;
import com.excelsior.xds.core.model.IXdsProcedure;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodReceiverSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;

public class XdsOberonMethodReceiver extends    SimpleXdsElementWithSymbol<IOberonMethodReceiverSymbol>
                                     implements IXdsOberonMethodReceiver
{
    public XdsOberonMethodReceiver( IXdsProject project
                                  , XdsCompilationUnit compilationUnit
                                  , IXdsProcedure parent
                                  , String name 
                                  , IModulaSymbolReference<IOberonMethodReceiverSymbol> symbolRef
                                  , SourceBinding sourceBinding ) 
      {
          super(name, project, compilationUnit, parent, symbolRef, sourceBinding);
      }

}
