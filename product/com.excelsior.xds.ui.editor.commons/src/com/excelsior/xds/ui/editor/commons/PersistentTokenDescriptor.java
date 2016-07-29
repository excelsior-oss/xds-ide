package com.excelsior.xds.ui.editor.commons;

import java.util.List;

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;
import org.ini4j.Config;
import org.ini4j.Ini;

import com.excelsior.xds.ui.commons.syntaxcolor.TextAttributeDescriptor;
import com.excelsior.xds.ui.commons.syntaxcolor.TokenDescriptor;

/**
 * <code>TextAttributeToken</code> which stores its data in persistence store.
 */
public class PersistentTokenDescriptor extends TokenDescriptor {
    
    private final String id;
    private final String colorId;
    private final String styleId;
    private final String disabledId;
    
    private int styleWhenEnabled;
    private boolean isDisabled;
    private RGB rgbWhenEnabled;
    private ITokens iTokens;
    
    /**
     * 
     * @param name    - human-readable name (with localization)
     * @param id      - unique id for IPreferenceStore key
     * @param rgb     - color
     * @param style   - style
     * @param iTokens - tokens set to ask for 'default' color for disabled elements (or null when element cat't be
     *                  disabled)
     */
    public PersistentTokenDescriptor( String name, String id 
                                       , RGB rgb, int style 
                                       , ITokens iTokens) 
    {
        super(name, rgb, style); // default values
        this.id    = id;
        
        colorId    = id + ".COLOR"; //$NON-NLS-1$ 
        styleId    = id + ".STYLE"; //$NON-NLS-1$ 
        disabledId = id + ".IS_DISABLED"; //$NON-NLS-1$ 
        
        isDisabled = false;
        rgbWhenEnabled   = rgb;
        styleWhenEnabled = style;
        this.iTokens     = iTokens;
    }
    
    public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	public PersistentTokenDescriptor( PersistentTokenDescriptor other){
    	this(other.getName(), other.getId(), other.getDefaultRgb(), other.getDefaultStyle(), other.iTokens);
    }
    
    public String getId() {
        return id;
    }

    public String getColorId() {
        return colorId;
    }
    
    public String getStyleId() {
        return styleId;
    }
    
    public String getDisabledId() {
        return disabledId;
    }
    
    public boolean mayBeDisabled() {
        return iTokens != null;
    }
    
    public boolean isDisabled() {
        return isDisabled && iTokens != null;
    }

    public int getStyleWhenEnabled() {
        return styleWhenEnabled;
    }
    
    public RGB getRgbWhenEnabled() {
        return rgbWhenEnabled;
    }


    public void preferenciesToIni(List<String> al, int style, boolean isDisabled, RGB rgb) {
        al.add(styleId    + "=" + style);      //$NON-NLS-1$
        al.add(disabledId + "=" + (isDisabled ? "1" : "0"));      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        al.add(colorId    + "=" + StringConverter.asString(rgb));      //$NON-NLS-1$
    }
    
    
    public void preferenciesToIni(List<String> al) {
        preferenciesToIni(al, getStyleWhenEnabled(), isDisabled(), getRgbWhenEnabled());
    }

    public void preferenciesFromIni(Ini ini) {
        String s = ini.get(Config.DEFAULT_GLOBAL_SECTION_NAME, styleId);
        if (s != null) {
            styleWhenEnabled = Integer.parseInt(s);
        } else {
            styleWhenEnabled = getDefaultStyle();
        }
        
        s = ini.get(Config.DEFAULT_GLOBAL_SECTION_NAME, disabledId);
        if (s != null) {
            isDisabled = (Integer.parseInt(s) != 0);
        } else {
            isDisabled = false;
        }
        
        s = ini.get(Config.DEFAULT_GLOBAL_SECTION_NAME, colorId);
        if (s != null) {
            rgbWhenEnabled = StringConverter.asRGB(s, getDefaultRgb());
        } else {
            rgbWhenEnabled = getDefaultRgb();
        }
        
        if (isDisabled && iTokens != null) {
            PersistentTokenDescriptor pt = iTokens.getDefaultColoring();
            TextAttributeDescriptor ta = pt.getTextAttribute();
            if (ta != null) {
                setTextAttribute(new TextAttributeDescriptor(ta.getForeground(), null, ta.getStyle()));
            }
        } else {
        	setTextAttribute(new TextAttributeDescriptor(rgbWhenEnabled, null, styleWhenEnabled));
        }
    }
}
