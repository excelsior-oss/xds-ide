package com.excelsior.xds.ui.editor.commons.preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.preference.IPreferenceStore;

public abstract class AbstractSyntaxColoring implements ISyntaxColoringPreferences {
	protected void doSave(IPreferenceStore store, String preferenceId, List<ITokenModification> tokenModifications) {
		List<String> lines = new ArrayList<>();
		tokenModifications.forEach(m -> {
			m.getToken().getToken().preferenciesToIni(lines, m.getStyle(), m.isDisabled(), m.getRgb());
		});
		
		store.setValue(preferenceId, String.join(System.lineSeparator(), lines));
	}
	
	protected String getTemplateText(String templateResourceName){
		try {
			return IOUtils.toString(getClass().getResourceAsStream(templateResourceName)); 
		} catch (IOException e) {
			return null;
		}
	}
}