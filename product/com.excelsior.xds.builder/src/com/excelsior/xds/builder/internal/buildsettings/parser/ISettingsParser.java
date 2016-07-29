package com.excelsior.xds.builder.internal.buildsettings.parser;

import java.io.IOException;

/**
 * Simple streaming parser of the options. Each (settingName,settingValue) is reported to listeners
 * @author lsa80
 */
public interface ISettingsParser<T extends Object> {
	void addListener(ISettingsParserListener<T> listener);
	void removeListener(ISettingsParserListener<T> listener);
	void parse(String equations) throws IOException;
}
