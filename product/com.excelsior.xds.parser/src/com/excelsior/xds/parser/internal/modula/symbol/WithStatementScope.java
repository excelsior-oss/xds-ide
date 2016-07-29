package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Iterator;

import org.apache.commons.collections.iterators.EmptyIterator;

import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.reference.IModulaSymbolReference;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;

/**
 * A scope of Modula-2 WITH statement.
 */
public class WithStatementScope implements IModulaSymbolScope  
{
    private final IModulaSymbolScope parentScope;
    private IModulaSymbolReference<IRecordTypeSymbol> withExpressionTypeRef;
    
    public WithStatementScope(IModulaSymbolReference<IRecordTypeSymbol> withExpressionTypeRef, IModulaSymbolScope parentScope) {
        this.withExpressionTypeRef = withExpressionTypeRef;
        this.parentScope = parentScope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbolScope getParentScope() {
        return parentScope;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol resolveName(String symbolName) {
        IModulaSymbol symbol = findSymbolInScope(symbolName);
        if (symbol == null) {
            IModulaSymbolScope parentScope = getParentScope();
            if (parentScope != null) {
                symbol = parentScope.resolveName(symbolName);
            }
        }
        return symbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String symbolName) {
        IModulaSymbol symbol = null;
        IRecordTypeSymbol withExpressionType = ReferenceUtils.resolve(withExpressionTypeRef);
        if (withExpressionType != null) {
            symbol = withExpressionType.findSymbolInScope(symbolName);
        }
        if (symbol == null) {
            IModulaSymbolScope parentScope = getParentScope();
            if (parentScope != null) {
                symbol = parentScope.findSymbolInScope(symbolName);
            }
        }
        return symbol;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbol findSymbolInScope(String symbolName, boolean isPublic) {
        IModulaSymbol symbol = findSymbolInScope(symbolName);
        if (symbol != null) {
            if (isPublic != symbol.isAttributeSet(SymbolAttribute.PUBLIC)) {
                symbol = null;
            }
        }
        return symbol;
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<IModulaSymbol> iterator() {
        IRecordTypeSymbol withExpressionType = ReferenceUtils.resolve(withExpressionTypeRef);
        if (withExpressionType == null) {
            return EmptyIterator.INSTANCE;
        }
        return withExpressionType.iterator();
    }
    
}
