package com.excelsior.xds.builder.internal.buildsettings.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;


public class EquationParser implements ISettingsParser<String>{
	private Collection<ISettingsParserListener<String>> listeners = new HashSet<ISettingsParserListener<String>>(2);
	
	public void addListener(ISettingsParserListener<String> listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ISettingsParserListener<String> listener) {
		listeners.remove(listener);
	}
	
	public void parse(String equations) throws IOException {
		 String line;
	        BufferedReader br = new BufferedReader(new StringReader(equations));
	        while ((line = br.readLine()) != null) {
	            line = line.trim();
	            if (line.startsWith("-")) { //$NON-NLS-1$
	                int eqpos = line.indexOf('='); //$NON-NLS-1$
	                if (eqpos > 1) {
	                    String name = line.substring(1, eqpos).trim();
	                    String val = line.substring(eqpos+1).trim();
	                    notifyListeners(name, val);
	                }
	            }
	        }
	}
	
	private void notifyListeners(String optionName, String value) {
		for (ISettingsParserListener<String> listener : listeners) {
			listener.settingParsed(optionName, value);
		}
	}
}
