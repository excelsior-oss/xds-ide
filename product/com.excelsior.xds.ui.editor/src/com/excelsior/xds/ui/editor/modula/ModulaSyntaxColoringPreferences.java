package com.excelsior.xds.ui.editor.modula;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;

import com.excelsior.xds.ui.commons.syntaxcolor.TokenManager;
import com.excelsior.xds.ui.commons.utils.XStyledString;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.commons.ITokens;
import com.excelsior.xds.ui.editor.commons.PersistentTokenDescriptor;
import com.excelsior.xds.ui.editor.commons.RgbStyle;
import com.excelsior.xds.ui.editor.commons.preferences.AbstractSyntaxColoring;
import com.excelsior.xds.ui.editor.commons.preferences.ITokenModification;
import com.excelsior.xds.ui.editor.internal.nls.Messages;

public class ModulaSyntaxColoringPreferences extends AbstractSyntaxColoring {
	public ModulaSyntaxColoringPreferences() {
	}

	@Override
	public String getLanguageId() {
		return "Modula";//$NON-NLS-1$ 
	}

	@Override
	public String getLanguageName() {
		return Messages.SyntaxColoringPreferencePage_Modula2;
	}
	
	@Override
	public String getTemplateText() {
		return getTemplateText("Modula.txt"); //$NON-NLS-1$
	}
	
	@Override
	public ITokens getDefaultToken() {
		return ModulaTokens.Default;
	}

	@Override
	public List<ITokens> getTokens() {
		return Arrays.asList(ModulaTokens.values());
	}

	@Override
	public XStyledString doColor(TokenManager tokenManager, String text,
			Map<PersistentTokenDescriptor, RgbStyle> colorConversion)
			throws IOException {
		ModulaSyntaxColorer colorer = new ModulaSyntaxColorer(tokenManager);
		return colorer.color(text, colorConversion);
	}

	@Override
	public void save(List<ITokenModification> tokenModifications) {
		IPreferenceStore store = XdsEditorsPlugin.getDefault().getPreferenceStore();
		doSave(store, ModulaTokens.PREFS_ID, tokenModifications);
		ModulaTokens.updateTokensFromStore(store, true);
	}
}