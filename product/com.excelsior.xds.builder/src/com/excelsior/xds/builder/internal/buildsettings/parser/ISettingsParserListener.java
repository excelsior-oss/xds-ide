package com.excelsior.xds.builder.internal.buildsettings.parser;

/**
 * Notification about <settingName, settingValue> was parsed
 * 
 * @author lsa80
 * @param <T> - type of the option value
 */
public interface ISettingsParserListener<T> {
	void settingParsed(String settingName, T settingValue);
}
