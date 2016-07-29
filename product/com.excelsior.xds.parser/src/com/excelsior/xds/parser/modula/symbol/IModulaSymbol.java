package com.excelsior.xds.parser.modula.symbol;

import java.util.EnumSet;

import com.excelsior.xds.parser.commons.symbol.ISymbol;
import com.excelsior.xds.parser.commons.symbol.ISymbolTextBinding;
import com.excelsior.xds.parser.modula.XdsLanguage;

/**
 * An object that represents a Modula-2/Oberon-2 entity from the source code.  
 */
public interface IModulaSymbol extends ISymbol, ISymbolTextBinding
{
    /**
     * {@inheritDoc}
     */
    @Override
    public ISymbolWithScope getParentScope();


    public XdsLanguage getLanguage();
    
    public void setLanguage(XdsLanguage language);
    
    
    public EnumSet<SymbolAttribute> getAttributes();
    
    public void addAttribute(SymbolAttribute attr);

    public void addAttributes(EnumSet<SymbolAttribute> attrs);
    
    public boolean isAttributeSet(SymbolAttribute attr);

    public void setAttributes(EnumSet<SymbolAttribute> attrs);

    public void removeAttribute(SymbolAttribute attr);
    
    
    /**
     * @return qualified name of this symbol. 
     */
    public String getQualifiedName();
        
    
    public boolean isAnonymous();
    
    /**
     * Tag to mark anonymous symbols 
     */
    public final String ANONYMOUS_NAME_TAG = "$";     //$NON-NLS-1$
    
    
    public void accept(ModulaSymbolVisitor visitor);
    
}
