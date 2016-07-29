package com.excelsior.xds.ui.editor.modula;

import java.io.StringReader;
import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.ini4j.Ini;
import org.ini4j.Wini;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.commons.ITokens;
import com.excelsior.xds.ui.editor.commons.PersistentTokenDescriptor;
import com.excelsior.xds.ui.editor.commons.SourceCodeTextEditor;
import com.excelsior.xds.ui.editor.internal.nls.Messages;

/**
 * Modula-2 tokens for syntax coloring. 
 * 
 * NOTE: Names of Modula-2 tokens are used in the resource file "colorPreview_Modula.txt".
 * Refactoring actions don't care about this resource file.  
 * 
 * The order of tokens' declaration defines the order in which this items will be shown to UI dialogs.  
 * 
 * @author lion, fsa
 */
public enum ModulaTokens implements ITokens {
    Default (Messages.ModulaTokens_Text, "Text", new RGB(0, 0, 0), SWT.NONE, null), //$NON-NLS-1$

    Keyword            (Messages.ModulaTokens_Keywords,             "Keywords",             new RGB(0,    0,    0x80), SWT.BOLD, null),   //$NON-NLS-1$
    BuiltinConstant    (Messages.ModulaTokens_BuiltinConstants,     "BuiltinConstants",     new RGB(0,    0,    0xff), SWT.NONE, null),   //$NON-NLS-1$
    SystemModuleKeyword(Messages.ModulaTokens_SystemModuleKeywords, "SystemModuleKeywords", new RGB(0,    0,    0x80), SWT.BOLD, null),   //$NON-NLS-1$

    Number   (Messages.ModulaTokens_Numbers,  "Numbers",  new RGB(0, 0, 0xff), SWT.NONE, null), //$NON-NLS-1$
    String   (Messages.ModulaTokens_Strings,  "Strings",  new RGB(0, 0, 0xff), SWT.NONE, null), //$NON-NLS-1$
    Bracket  (Messages.ModulaTokens_Brackets, "Brackets", new RGB(0, 0, 0   ), SWT.NONE, null),  //$NON-NLS-1$

    // Comments
    BlockComment     (Messages.ModulaTokens_BlockComments,     "BlComments",  new RGB(0x00, 0x80, 0x00), SWT.ITALIC, Messages.ModulaTokens_Comments), //$NON-NLS-1$
    EndOfLineComment (Messages.ModulaTokens_EndOfLineComments, "EolComments", new RGB(0x00, 0x80, 0x00), SWT.ITALIC, Messages.ModulaTokens_Comments), //$NON-NLS-1$
    TodoTask         (Messages.ModulaTokens_TodoTask,          "TodoTask",    new RGB(0x00, 0x80, 0x00), SWT.ITALIC+SWT.BOLD,   Messages.ModulaTokens_Comments), //$NON-NLS-1$

    // Compiler pragmas
    Pragma         (Messages.ModulaTokens_Pragmas,        "Pragmas",        new RGB(0x80, 0x00, 0x80), SWT.ITALIC, Messages.ModulaTokens_CompilerPragmas),          //$NON-NLS-1$
    PragmaKeyword  (Messages.ModulaTokens_PragmaKeywords, "PragmaKeywords", new RGB(0x80, 0x00, 0x80), SWT.ITALIC+SWT.BOLD, Messages.ModulaTokens_CompilerPragmas), //$NON-NLS-1$
    InactiveCode   (Messages.ModulaTokens_InactiveCode,  "InactiveCode",    new RGB(0xC0, 0xC0, 0xC0), SWT.NONE, Messages.ModulaTokens_CompilerPragmas), //$NON-NLS-1$
    ;
    
    
    
    // Note: PREFS_ID used in "org.eclipse.ui.preferenceTransfer" extension point:
    public static final String PREFS_ID = XdsEditorsPlugin.PLUGIN_ID + ".ModulaTokens.Preferencies"; //$NON-NLS-1$ 
    private final PersistentTokenDescriptor token;
    private final String categoryName; // subroot in color settings dialog or null to show in root  

        
    
    ModulaTokens (String name, String id, RGB rgb, int style, String category) {
        ITokens it = (id.equals("Text")) ? null : this; //$NON-NLS-1$
        token = new PersistentTokenDescriptor(name, "ModulaToken." + id, rgb, style, it);  //$NON-NLS-1$
        categoryName = category;
    }


    /**
     * Initializes highlight colors/styles in store if they are absent in it.
     * 
     * @param store the preference store
     */
    public static void initStylesInStore(IPreferenceStore store) {
        if (!store.contains(PREFS_ID)) {
            ArrayList<String> al = new ArrayList<String>();
            for (ModulaTokens modulaToken : ModulaTokens.values()) {
                modulaToken.token.preferenciesToIni(al);
            }
            StringBuilder sb = new StringBuilder();
            for (String s : al) {
                sb.append(s).append("\n"); //$NON-NLS-1$
            }
            store.setValue(PREFS_ID, sb.toString());
        }
    }
    
    /**
     * Sets highlight colors/styles from store, 
     * refresh editors if <code>refreshEditors</code> is set.
     * 
     * @param store the preference store
     * @param refreshEditors
     */
    public static void updateTokensFromStore(IPreferenceStore store, boolean refreshEditors) {
        String strs = ""; //$NON-NLS-1$
        if (store.contains(PREFS_ID)) {
            strs = store.getString(PREFS_ID);
        }
        
        try {
            Ini ini = new Wini(new StringReader(strs));
            for (ModulaTokens modulaToken : ModulaTokens.values()) {
                modulaToken.token.preferenciesFromIni(ini);
            }
            
            if (refreshEditors) {
                SourceCodeTextEditor.refreshEditorsConfiguration(ModulaEditor.class);
            }
        } catch (Exception e) {
            LogHelper.logError(e);
        }
    }
    
    /**
     * 
     */
    public static void addStoreListener() {
        final IPreferenceStore store = XdsEditorsPlugin.getDefault().getPreferenceStore();
        store.addPropertyChangeListener(new IPropertyChangeListener() {
            // used when settings are imported via "org.eclipse.ui.preferenceTransfer" extension point
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                String prop = event.getProperty();
                if (PREFS_ID.equals(prop)) {
                    updateTokensFromStore(store, true);
                }
            }
        });
    }


    @Override
    public PersistentTokenDescriptor getDefaultColoring() {
        return Default.token;
    }


    @Override
    public PersistentTokenDescriptor getToken() {
        return token;
    }
    
	@Override
	public String getCategoryName() {
		return categoryName;
	}
}
