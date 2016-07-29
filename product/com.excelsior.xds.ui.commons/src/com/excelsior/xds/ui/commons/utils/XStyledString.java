package com.excelsior.xds.ui.commons.utils;

import java.util.ArrayList;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;

import com.excelsior.xds.ui.commons.swt.resources.ResourceRegistry;

/**
 * Universal container for text and its style ranges
 * 
 * Used in hovers, requires StyledText.addPaintObjectListener() to paint images
 * in StyledText when it is need. 
 */
public class XStyledString {
    private StringBuilder sb = new StringBuilder();
    private ArrayList<StyleRange> srarr = new ArrayList<StyleRange>();
    private Object userData;
    private int usedStyles = 0;
    
    private final ResourceRegistry resourceRegistry;
    
    public XStyledString(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	public void append(String s) {
        sb.append(s);
    }
    
    public void append(String s, int style) {
        StyleRange sr = mkSRange(s, null, style);
        sb.append(s);
        srarr.add(sr);
    }
    
    public void append(String s, Color fgColor) {
        StyleRange sr = mkSRange(s, fgColor, SWT.NORMAL);
        sr.foreground = fgColor;
        sb.append(s);
        srarr.add(sr);
    }
    
    public void append(String s, Color fgColor, int style) {
        StyleRange sr = mkSRange(s, fgColor, style);
        sb.append(s);
        srarr.add(sr);
    }
    
    public void append(Image im) {
        StyleRange sr = mkSRange(" ", null, SWT.NORMAL);
        sr.data = im;
        Rectangle rect = im.getBounds();
        sr.metrics = new GlyphMetrics(rect.height, 0, rect.width);
        sb.append(" ");
        srarr.add(sr);
    }

    public void append(XStyledString ss) {
        int shift = sb.length();
        for (StyleRange sr : ss.getStyleRanges()) {
            sr.start += shift;
            srarr.add(sr);
        }
        sb.append(ss.getText());
    }
    
    public String getText() {
        return sb.toString();
    }
    
    /**
     * 
     * @return style ranges. User may change settings inside ranges, it will affect on the string
     */
    public ArrayList<StyleRange> getStyleRanges() {
        return srarr;
    }
    
    public void setUserData(Object data) {
        userData = data;
    }
    
    public Object getUserData() {
        return userData;
    }
    
    public StyledString convertToStyledString(final Font font) {
        Font italicFontF = null;
        Font boldFontF = null;
        Font boldItalicFontF = null;

        if ((usedStyles & (SWT.BOLD | SWT.ITALIC)) == SWT.ITALIC) {
            if (italicFontF == null) {
                FontData[] data= font.getFontData();
                for (int i= 0; i < data.length; i++) {
                    data[i].setStyle(SWT.ITALIC);
                }
                italicFontF = resourceRegistry.createFont(data);
            }
        } else if ((usedStyles & (SWT.BOLD | SWT.ITALIC)) == SWT.BOLD) {
            if (boldFontF == null) {
                FontData[] data= font.getFontData();
                for (int i= 0; i < data.length; i++) {
                    data[i].setStyle(SWT.BOLD);
                }
                boldFontF = resourceRegistry.createFont(data);
            }
        } else if ((usedStyles & (SWT.BOLD | SWT.ITALIC)) == (SWT.BOLD | SWT.ITALIC)) {
            if (boldItalicFontF == null) {
                FontData[] data= font.getFontData();
                for (int i= 0; i < data.length; i++) {
                    data[i].setStyle(SWT.BOLD | SWT.ITALIC);
                }
                boldItalicFontF = resourceRegistry.createFont(data);
            }
        }  

        
        StyledString ss = new StyledString();
        int pos = 0;
        for (StyleRange sr : srarr) {
            if (sr.start > pos) {
                ss.append(sb.substring(pos, sr.start));
                pos = sr.start;
            }
            final StyleRange srF = sr;
            final Font italicFont1 = italicFontF;
            final Font boldFont1 = boldFontF;
            final Font boldItalicFont1 = boldItalicFontF;
            
            ss.append(sb.substring(sr.start, sr.start + sr.length), new Styler() {
                @Override
                public void applyStyles(TextStyle textStyle) {
                    if ((srF.fontStyle & (SWT.BOLD | SWT.ITALIC)) == SWT.BOLD) {
                        textStyle.font = boldFont1;
                    } else if ((srF.fontStyle & (SWT.BOLD | SWT.ITALIC)) == SWT.ITALIC) {
                        textStyle.font = italicFont1;
                    } else if ((srF.fontStyle & (SWT.BOLD | SWT.ITALIC)) == (SWT.BOLD | SWT.ITALIC)) {
                        textStyle.font = boldItalicFont1;
                    }  
                    if (srF.foreground != null) { 
                        textStyle.foreground = srF.foreground;
                    }
                    if (srF.metrics != null) {
                        textStyle.metrics = srF.metrics;
                    }
                    if (srF.data != null) {
                        textStyle.data = srF.data;
                    }
                    textStyle.underline = srF.underline;
                    textStyle.strikeout = srF.strikeout;
                    textStyle.borderStyle = srF.borderStyle;
                    textStyle.borderColor = srF.borderColor;
                }
            });
            pos = ss.length();
        }
        if (pos < sb.length()) {
            ss.append(sb.substring(pos));
        }
        return ss;
    }
    
    private StyleRange mkSRange(String s, Color fgColor, int style) {
        //NOTE: new used attributes in StyleRange must be reconcoled in convertToStyledString()
        StyleRange sr = new StyleRange();
        sr.start = sb.length();
        sr.length = s.length();
        sr.foreground = fgColor;
        sr.fontStyle = (style & (SWT.BOLD | SWT.ITALIC));
        sr.underline = (style & TextAttribute.UNDERLINE) != 0;
        sr.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
        usedStyles |= style;
        return sr;
    }
}
