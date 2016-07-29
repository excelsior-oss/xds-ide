package com.excelsior.xds.parser.internal.modula.symbol.type;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.excelsior.xds.core.utils.collections.BaseTypeIterator;
import com.excelsior.xds.parser.internal.modula.symbol.reference.IReferenceFactory;
import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;
import com.excelsior.xds.parser.modula.type.EnumType;

public class EnumTypeSymbol extends    OrdinalTypeSymbol<EnumType> 
                            implements IEnumTypeSymbol
{
    private final Map<String, IEnumElementSymbol> elements;
    
    public EnumTypeSymbol(String name, ISymbolWithScope parentScope) {
        super(name, parentScope);
        elements = new LinkedHashMap<String, IEnumElementSymbol>();
    }
    
    @Override
    public IEnumTypeSymbol createSynonym(String name, ISymbolWithScope parentScope, IReferenceFactory refFactory) {
        return new EnumTypeSynonymSymbol(name, parentScope, refFactory.createRef(this), refFactory.createRef((IModulaSymbol)this)); 
    }
    /**
     * Finalizes the symbol construction and creates type for this symbol.  
     */
    public void finalizeDefinition() {
        setType(new EnumType(getName(), elements.size())); 
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IEnumElementSymbol> getElements() {
        return elements.values();
    }
    
    public void addElement(IEnumElementSymbol s) {
        elements.put(s.getName(), s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getElementCount() {
        return elements.size();
    }

    
    //--------------------------------------------------------------------------
    // Manipulation with attributes
    //--------------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addAttribute(SymbolAttribute attr) {
        super.addAttribute(attr);
        if (attr == SymbolAttribute.PUBLIC) {
            addAttributeToElements(SymbolAttribute.PUBLIC);
        }
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addAttributes(EnumSet<SymbolAttribute> attrs) {
        super.addAttributes(attrs);
        if (attrs.contains(SymbolAttribute.PUBLIC)) {
            addAttributeToElements(SymbolAttribute.PUBLIC);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(EnumSet<SymbolAttribute> attrs) {
        super.setAttributes(attrs);
        if (attrs.contains(SymbolAttribute.PUBLIC)) {
            addAttributeToElements(SymbolAttribute.PUBLIC);
        }
        else {
            removeAttributeFromElements(SymbolAttribute.PUBLIC);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute(SymbolAttribute attr) {
        super.removeAttribute(attr);
        if (attr == SymbolAttribute.PUBLIC) {
            removeAttributeFromElements(SymbolAttribute.PUBLIC);
        }
    }
    
    private void addAttributeToElements(SymbolAttribute attr) {
        for (IEnumElementSymbol element : getElements()) {
            element.addAttribute(attr);
        }
    }
    
    private void removeAttributeFromElements(SymbolAttribute attr) {
        for (IEnumElementSymbol element : getElements()) {
            element.removeAttribute(attr);
        }
    }

    
    //--------------------------------------------------------------------------
    // Implementation of IModulaSymbolScope interface
    //--------------------------------------------------------------------------

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
    public IEnumElementSymbol findSymbolInScope(String symbolName) {
        return elements.get(symbolName);
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IEnumElementSymbol findSymbolInScope(String symbolName, boolean isPublic) {
        IEnumElementSymbol symbol = findSymbolInScope(symbolName);
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
    @Override
    public Iterator<IModulaSymbol> iterator() {
        return new BaseTypeIterator<IModulaSymbol>(elements.values().iterator());
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
	protected void doAccept(ModulaSymbolVisitor visitor) {
        boolean isVisitChildren = visitor.visit(this);
        if (isVisitChildren) {
            acceptChildren(visitor, getElements());
        }
	}

}
