package com.excelsior.xds.ui.commons.syntaxcolor;

import org.eclipse.swt.graphics.RGB;

/**
 * Token to hold <code>TextAttribute</code> as data.
 */
public class TokenDescriptor {
    
    protected final String name;
    
    private final RGB defaultRGB;
    private final int defaultStyle;
        
    /** The default text attribute if non is returned as data by the current token */
    private final TextAttributeDescriptor defaultTextAttribute;
    
    private TextAttributeDescriptor textAttribute;
    
    /**
     * Used for special tokens, note package private
     */
    TokenDescriptor () {
    	this(null, null, 0);
    }

    public TokenDescriptor (String name, RGB foregroundRgb, int style) {
        defaultTextAttribute = new TextAttributeDescriptor(foregroundRgb, null, style);

        this.name    = name;
        defaultRGB   = foregroundRgb;
        defaultStyle = style; 
    }
    
    public String getName() {
        return name;
    }

    public RGB getDefaultRgb() {
        return defaultRGB;
    }
    
    public int getDefaultStyle() {
        return defaultStyle;
    }

    public void restoreDefaults() {
    	textAttribute = defaultTextAttribute; 
    }

    /**
     * Returns a text attribute encoded in this token. If the token's
     * data is not <code>null</code> and a text attribute it is assumed that
     * it is the encoded text attribute. It returns the default text attribute
     * if there is no encoded text attribute found.
     *
     * @return the token's text attribute
     */
    public TextAttributeDescriptor getTextAttribute() {
        if (textAttribute != null) {
        	return textAttribute;
        }
        return defaultTextAttribute;
    }
    
    public void setTextAttribute(TextAttributeDescriptor textAttribute) {
		this.textAttribute = textAttribute;
	}
}
