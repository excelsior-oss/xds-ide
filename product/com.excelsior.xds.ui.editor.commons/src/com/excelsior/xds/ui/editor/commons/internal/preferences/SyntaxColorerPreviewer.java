package com.excelsior.xds.ui.editor.commons.internal.preferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.excelsior.xds.ui.commons.syntaxcolor.TokenManager;
import com.excelsior.xds.ui.commons.utils.XStyledString;
import com.excelsior.xds.ui.editor.commons.PersistentTokenDescriptor;
import com.excelsior.xds.ui.editor.commons.RgbStyle;
import com.excelsior.xds.ui.editor.commons.internal.preferences.SyntaxColoringPreferencePage.HighlightingColorTreeItem;
import com.excelsior.xds.ui.editor.commons.preferences.ISyntaxColoringPreferences;

public class SyntaxColorerPreviewer extends StyledText {
    private ISyntaxColoringPreferences syntaxColoring;
    private List<HighlightingColorTreeItem> fListTreeItems;
    private Color defBackgroundColor;
    private IPreferenceStore preferenceStore;
    private IPropertyChangeListener prefListener;
	private TokenManager tokenManager;
    
    public SyntaxColorerPreviewer(Composite parent, final IPreferenceStore preferenceStore) {
        super(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
        this.preferenceStore = preferenceStore;
        this.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Font font= JFaceResources.getTextFont();
        this.setFont(font);
        
        defBackgroundColor = getEditorBackgroundColor(preferenceStore);
        this.setBackground(defBackgroundColor);
        
        prefListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				try { // Widget may be disposed here
				    defBackgroundColor = getEditorBackgroundColor(preferenceStore);
	                setBackground(defBackgroundColor);
	                updateColors();
		        } catch (Exception e) {} 
			}
        };
        
        preferenceStore.addPropertyChangeListener(prefListener);
        
        tokenManager = new TokenManager();
    }
    
    @Override
    public void dispose() {
    	preferenceStore.removePropertyChangeListener(prefListener);
    	tokenManager.dispose();
    	super.dispose();
    }


    private Color getEditorBackgroundColor(IPreferenceStore preferenceStore) {
        if (preferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
            return null;
        } else {
            RGB rgb = PreferenceConverter.getColor(preferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
            return new Color(Display.getDefault(), rgb);
        }
    }
    
    
    /**
     * Read and parse file colorPreview_<contentName>.txt
     * Show its content in the colorer previviewer window
     * See file syntax description in the parser
     * @param contentName - content ident
     * @param hcItems - list of subtree items for the given content type
     */
    public void activateContent( ISyntaxColoringPreferences syntaxColoring, List<HighlightingColorTreeItem> hcItems) {
      	fListTreeItems = hcItems;
        if (this.syntaxColoring == syntaxColoring) {
            return;
        }
        this.syntaxColoring = syntaxColoring;
        
        String text = syntaxColoring.getTemplateText();
        if (text == null){
        	text = "** Internal error, preview not available. See error log for details."; //$NON-NLS-1$
        }
        this.setText(text);
    }
    
    public void updateColors() {
        try {
            Map<PersistentTokenDescriptor, RgbStyle> colorConvertationMap = new HashMap<PersistentTokenDescriptor, RgbStyle>();

            // Default style:
            PersistentTokenDescriptor defTok = syntaxColoring.getDefaultToken().getToken();
            
            int defStyle = defTok.getStyleWhenEnabled();
            RGB defRgb = defTok.getRgbWhenEnabled();
            for (HighlightingColorTreeItem hcit : fListTreeItems) {
                if (hcit.getToken().getId().equals(defTok.getId())) {
                    defStyle = hcit.style;
                    defRgb = hcit.rgb;
                    break;
                }
            }
            {
                StyleRange sr = new StyleRange();
                sr.fontStyle = (defStyle & (SWT.BOLD | SWT.ITALIC));
                sr.underline = (defStyle & TextAttribute.UNDERLINE) != 0;
                sr.strikeout = (defStyle & TextAttribute.STRIKETHROUGH) != 0;
                sr.foreground = new Color(Display.getDefault(), defRgb);
                sr.background = defBackgroundColor;
                sr.start = 0;
                sr.length = this.getText().length();
                this.setStyleRange(sr);
            }

            // Color/style convertation map:
            for (HighlightingColorTreeItem hcit : fListTreeItems) {
                RGB rgb = hcit.rgb;
                int style = hcit.style;
                if (hcit.isDisabled) {
                    rgb = defRgb;
                    style = defStyle;
                }
                colorConvertationMap.put(hcit.getToken(), new RgbStyle(rgb, style));
            }

            XStyledString xss = syntaxColoring.doColor(tokenManager, getText(), colorConvertationMap);
            this.setStyleRange(null);
            for (StyleRange sr : xss.getStyleRanges()) {
                this.setStyleRange(sr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
