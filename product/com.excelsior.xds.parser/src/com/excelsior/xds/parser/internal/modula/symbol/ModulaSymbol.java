package com.excelsior.xds.parser.internal.modula.symbol;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.text.TextRegion;
import com.excelsior.xds.parser.commons.symbol.IMutableSymbolTextBinding;
import com.excelsior.xds.parser.commons.symbol.Symbol;
import com.excelsior.xds.parser.commons.symbol.SymbolTextBinding;
import com.excelsior.xds.parser.modula.XdsLanguage;
import com.excelsior.xds.parser.modula.symbol.IDefinitionModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IStandardModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.ModulaSymbolVisitor;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;

/**
 * Base class to represents a Modula-2/Oberon-2 entity from the source code.  
 */
public abstract class ModulaSymbol extends    Symbol 
                                   implements IModulaSymbol
                                            , IMutableSymbolTextBinding 
{
    /** Binding to the location in the source text. */
    private final IMutableSymbolTextBinding textBinding;
    
    private XdsLanguage language;
    private EnumSet<SymbolAttribute> attributes;
    
    /** unique identifier to resolve collision of qualified name */
    private String nameCollosionId = null;
    
    /**
     * Creates new Modula-2 symbol with given name and parent scope and 
     * propagates attributes from the parent scope.
     * 
     * @param name - name of the symbol
     * @param parentScope - the parent scope of the symbol
     */
    public ModulaSymbol(String name, ISymbolWithScope parentScope) {
        super(name, parentScope);
        textBinding = createSymbolTextBinding();
        language = parentScope != null ? parentScope.getLanguage() : XdsLanguage.Modula2;
        attributes = EnumSet.noneOf(SymbolAttribute.class);
        if (parentScope != null) {
            if (parentScope instanceof IDefinitionModuleSymbol) {
                attributes.add(SymbolAttribute.PUBLIC);
            }
            if (parentScope instanceof IStandardModuleSymbol) {
                attributes.add(SymbolAttribute.PERVASIVE);
            }
            if (parentScope.isAttributeSet(SymbolAttribute.RECONSTRUCTED)) {
                attributes.add(SymbolAttribute.RECONSTRUCTED);
            }
        }
        if ((name == null) || (name.contains(ANONYMOUS_NAME_TAG))) {
            attributes.add(SymbolAttribute.ANONYMOUS_NAME);
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifiedName() {
        String qualifiedName = getName() + getNameCollosionId();
        ISymbolWithScope parentScope = getParentScope();
        if (parentScope != null) {
            qualifiedName = parentScope.getQualifiedName() + "." + qualifiedName;   //$NON-NLS-1$
        }
        return qualifiedName;
    }
    
    /**
     * Sets unique identifier to resolve collision of qualified name.
     */
    public void setNameCollosionId(int uniqueId) {
        this.nameCollosionId = "#" + uniqueId;    //$NON-NLS-1$; 
    }
    
    /**
     * Returns unique identifier to resolve collision of qualified name.
     * @return unique identifier
     */
    protected String getNameCollosionId() {
        if (nameCollosionId == null) {
            return "";   //$NON-NLS-1$
        }
        return nameCollosionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISymbolWithScope getParentScope() {
        return (ISymbolWithScope)super.getParentScope();
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public XdsLanguage getLanguage() {
        return language;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLanguage(XdsLanguage language) {
        this.language = language;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<SymbolAttribute> getAttributes() {
        return this.attributes;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttributeSet(SymbolAttribute attr) {
        return attributes.contains(attr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAttribute(SymbolAttribute attr) {
        attributes.add(attr);
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addAttributes(EnumSet<SymbolAttribute> attrs) {
        this.attributes.addAll(attrs);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(EnumSet<SymbolAttribute> attrs) {
        this.attributes.clear();
        this.attributes.addAll(attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute(SymbolAttribute attr) {
        attributes.remove(attr);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnonymous() {
        return attributes.contains(SymbolAttribute.ANONYMOUS_NAME);
    }
 
    
    //-------------------------------------------------------------------------
    // Symbol's location in the source text
    //-------------------------------------------------------------------------

    protected IMutableSymbolTextBinding createSymbolTextBinding() {
        return new SymbolTextBinding();
    }
    
    public IMutableSymbolTextBinding getTextBinding() {
        return textBinding;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TextPosition getPosition() {
        return getTextBinding().getPosition();
    }
    
    /**
     * Sets position in which symbol is defined. 
     * 
     * @param defPosition position in which symbol is defined.
     */
    public void setPosition(TextPosition defPosition) {
        getTextBinding().setPosition(defPosition);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITextRegion getNameTextRegion() {
        ITextRegion nameRegion = getTextBinding().getNameTextRegion();
        if ((nameRegion == null) && !isAnonymous()) {
            TextPosition position = getTextBinding().getPosition();
            if ((position != null) && StringUtils.isNotEmpty(getName())) {
                nameRegion = new TextRegion(position.getOffset(), getName().length());
                getTextBinding().setNameTextRegion(nameRegion);
            }
        }
        return nameRegion;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameTextRegion(ITextRegion region) {
        getTextBinding().setNameTextRegion(region);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITextRegion getDeclarationTextRegion() {
        return getTextBinding().getDeclarationTextRegion();
    }
    
    /**
     * Sets text region in which symbol is defined. 
     * 
     * @param region the text region.
     */
    public void setDeclarationTextRegion(ITextRegion region) {
        getTextBinding().setDeclarationTextRegion(region);
    }
    
    
    /**
     * Accepts the given visitor on a visit of the current node.
     *
     * @param visitor the visitor object
     */
    public final void accept(ModulaSymbolVisitor visitor) {
    	if (visitor == null) {
			throw new IllegalArgumentException();
		}
		// begin with the generic pre-visit
		if (visitor.preVisit(this)) {
			// dynamic dispatch to internal method for type-specific visit/endVisit
			doAccept(visitor);
		}
		// end with the generic post-visit
		visitor.postVisit(this);
    }
    
    /**
     * Accepts the given visitor on a type-specific visit of the current node.
     * This method must be implemented in all concrete PST node types.
     * 
     * @param visitor the visitor object
     */
    protected abstract void doAccept(ModulaSymbolVisitor visitor);
    
    /**
     * Accepts the given visitor on a visit of the current node.
     * <p>
     * This method should be used by the concrete implementations of
     * <code>doAccept</code> to traverse optional child node. Equivalent
     * to <code>child.accept(visitor)</code> if <code>child</code>
     * is not <code>null</code>.
     * </p>
     *
     * @param visitor the visitor object
     * @param child the child ModulaSymbol node to dispatch too, 
     *        or <code>null</code> if none
     */
    protected final void acceptChild(ModulaSymbolVisitor visitor, IModulaSymbol child) 
    {
    	if (child != null) {
    		child.accept(visitor);
    	}
    }
    
    /**
     * Accepts the given visitor on a visit of the given collection of
     * child nodes.
     * <p>
     * This method must be used by the concrete implementations of
     * <code>doAccept</code> to traverse the collection of child nodes.
     * </p>
     *
     * @param visitor the visitor object
     * @param children the collection of child ModulaSymbol node to dispatch too,
     *        or <code>null</code> if none
     */
	protected final void acceptChildren( ModulaSymbolVisitor visitor
			                           , Collection<? extends IModulaSymbol> children ) 
	{
		if (children != null) {
			Iterator<? extends IModulaSymbol> cursor = children.iterator();
			while (cursor.hasNext()) {
				IModulaSymbol child = (IModulaSymbol) cursor.next();
				child.accept(visitor);
			}
		}
	}
	
}