package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.internal.modula.symbol.reference.ReferenceFactory;
import com.excelsior.xds.parser.modula.XdsLanguage;
import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;
import com.excelsior.xds.parser.modula.symbol.IImplemantationModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;

public class ImplementationModuleSymbol extends    ProgramModuleSymbol 
                                        implements IImplemantationModuleSymbol
{
    private IModulaSymbolReference<DefinitionModuleSymbol> definitionModuleRef;

    public ImplementationModuleSymbol(String name, XdsLanguage language, ParsedModuleKey key, BuildSettings buildSettings, IFileStore sourceFile) {
        super(name, language, key, buildSettings, sourceFile);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasConstructor() {
        DefinitionModuleSymbol definitionModule = ReferenceUtils.resolve(definitionModuleRef);
        return (definitionModule == null)
            || ! definitionModule.isAttributeSet(SymbolAttribute.NOMODULEINIT);
    }

    
    /**
     * {@inheritDoc}
     * TODO : remove when possible
     */
    @Deprecated
    public DefinitionModuleSymbol getDefinitionModule() {
        return ReferenceUtils.resolve(definitionModuleRef);
    }
    
    /**
     * TODO : remove when possible
     * 
     * @param definitionModule
     */
    @Deprecated
    public void setDefinitionModule(DefinitionModuleSymbol definitionModule) {
        definitionModuleRef = ReferenceFactory.createRef(definitionModule);
    }    
        

    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String name) {
        IModulaSymbol symbol = super.findSymbolInScope(name);
        DefinitionModuleSymbol definitionModule = ReferenceUtils.resolve(definitionModuleRef);
        if ((symbol == null) && (definitionModule != null)) {
            symbol = definitionModule.findExportedSymbol(name);
            if (symbol instanceof IEnumElementSymbol)
            	symbol = null;
        }
        return symbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IModulaSymbol> getExportedSymbols() {
        DefinitionModuleSymbol definitionModule = ReferenceUtils.resolve(definitionModuleRef);
        if (definitionModule != null) {
            return definitionModule.getExportedSymbols(); 
        }
        return Collections.emptyList();
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
		boolean isVisitChildren = visitor.visit(this);
		if (isVisitChildren) {
			super.acceptChildren(visitor);
		}
	}

}
