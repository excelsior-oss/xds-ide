package com.excelsior.xds.parser.commons.symbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.text.TextPosition;

public class BlockSymbolTextBinding extends    SymbolTextBinding
                                    implements IMutableBlockSymbolTextBinding
{
    private final List<ITextRegion> nameRegions; 
    
    /**
     * Constructs an empty text binding of block symbol with an initial capacity of two.
     */
    public BlockSymbolTextBinding() {
        this(2);
    }
    
    /**
     * Constructs an empty text binding of block symbol with the specified initial capacity.
     * 
     * @param initialCapacity  the initial capacity of the name text regions
     */
    public BlockSymbolTextBinding(int initialCapacity) {
        nameRegions = new ArrayList<ITextRegion>(initialCapacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameTextRegion(ITextRegion region) {
        super.setNameTextRegion(region);
        addNameTextRegion(region);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ITextRegion> getNameTextRegions() {
        return nameRegions;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addNameTextRegion(ITextRegion region) {
        if (!nameRegions.contains(region)) {
            nameRegions.add(region);
        }
    }

    
    /**
     * The empty source text binding of symbol with repeated name usages (immutable).
     */
    public static final IMutableBlockSymbolTextBinding EMPTY_TEXT_BINDING = new EmptyTextBinding();
    
    protected static class EmptyTextBinding implements IMutableBlockSymbolTextBinding
    {
        @Override
        public TextPosition getPosition() {
            return null;
        }

        @Override
        public void setPosition(TextPosition defPosition) {
            throw new UnsupportedOperationException("Immutable empty block symbol text binding");   //$NON-NLS-1$
        }
        
        
        @Override
        public ITextRegion getNameTextRegion() {
            return null;
        }

        @Override
        public void setNameTextRegion(ITextRegion region) {
            throw new UnsupportedOperationException("Immutable empty block symbol text binding");   //$NON-NLS-1$
        }

        
        @Override
        public ITextRegion getDeclarationTextRegion() {
            return null;
        }

        @Override
        public void setDeclarationTextRegion(ITextRegion region) {
            throw new UnsupportedOperationException("Immutable empty block symbol text binding");   //$NON-NLS-1$
        }

        
        @Override
        public Collection<ITextRegion> getNameTextRegions() {
            return Collections.emptyList();
        }

        @Override
        public void addNameTextRegion(ITextRegion region) {
            throw new UnsupportedOperationException("Immutable empty block symbol text binding");   //$NON-NLS-1$
        }
    }
    
}
