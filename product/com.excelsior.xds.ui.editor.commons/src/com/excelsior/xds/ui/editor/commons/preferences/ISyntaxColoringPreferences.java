package com.excelsior.xds.ui.editor.commons.preferences;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.excelsior.xds.ui.commons.syntaxcolor.TokenManager;
import com.excelsior.xds.ui.commons.utils.XStyledString;
import com.excelsior.xds.ui.editor.commons.ITokens;
import com.excelsior.xds.ui.editor.commons.PersistentTokenDescriptor;
import com.excelsior.xds.ui.editor.commons.RgbStyle;

/**
 * Syntax coloring preferences support
 * @author lsa80
 */
public interface ISyntaxColoringPreferences {
	String getLanguageId();
	String getLanguageName();
	String getTemplateText();
	ITokens getDefaultToken();
	List<ITokens> getTokens();
	
	XStyledString doColor(TokenManager tokenManager, String text,
			Map<PersistentTokenDescriptor, RgbStyle> colorConversion) throws IOException;
	
	void save(List<ITokenModification> tokenModifications);
}