package com.excelsior.xds.builder.internal.buildsettings.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;


public final class OptionParser implements ISettingsParser<Boolean>
{
	private Collection<ISettingsParserListener<Boolean>> listeners = new HashSet<ISettingsParserListener<Boolean>>(2);
	
	public void addListener(ISettingsParserListener<Boolean> listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ISettingsParserListener<Boolean> listener) {
		listeners.remove(listener);
	}
	
	public void parse(String options) throws IOException {
		String line;
		StringBuilder optionNameBuf = new StringBuilder();
		BufferedReader br = new BufferedReader(new StringReader(options));
		while ((line = br.readLine()) != null) {
			line = line.trim();
			boolean val;
			while ((val = line.startsWith("+")) || line.startsWith("-")) { //$NON-NLS-1$ //$NON-NLS-2$
				optionNameBuf.setLength(0);
				int i;
				for (i=1; i<line.length(); ++i) {
					char ch = line.charAt(i);
					if (ch == ' ' || ch == '\t') break; //$NON-NLS-1$ //$NON-NLS-1$
					optionNameBuf.append(ch);
				}
				line = line.substring(i).trim();
				if (optionNameBuf.length() > 0) {
					String optionName = optionNameBuf.toString();
					notifyListeners(optionName, val);
				}
			}
		}
	}
	
	private void notifyListeners(String optionName, Boolean value) {
		for (ISettingsParserListener<Boolean> listener : listeners) {
			listener.settingParsed(optionName, value);
		}
	}
}
