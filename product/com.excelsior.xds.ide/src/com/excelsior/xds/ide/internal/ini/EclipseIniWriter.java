package com.excelsior.xds.ide.internal.ini;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Platform;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.utils.collections.XMapUtils;

public class EclipseIniWriter {
	private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
	private Map<String, String> changeOptionName2Value;
	private boolean isWriteIfNotKeyPresent;

	public EclipseIniWriter(String name, String value, boolean isWriteIfNotKeyPresent) {
		this(XMapUtils.newLinkedHashMap(name, value), isWriteIfNotKeyPresent);
	}

	public EclipseIniWriter(Map<String, String> changeOptionName2Value, boolean isWriteIfNotKeyPresent) {
		this.changeOptionName2Value = new LinkedHashMap<String, String>(changeOptionName2Value);
	}

	public void write() throws Throwable {
		if (!MapUtils.isEmpty(changeOptionName2Value)) {
			String installPath = Platform.getInstallLocation().getURL()
					.getPath();
			String iniFile = FilenameUtils.concat(installPath, "xds-ide.ini"); //$NON-NLS-1$
			if (!new File(iniFile).exists()) {
				LogHelper.logError(String.format("Ini file not found : %s", iniFile));
				return;
			}
			File backupIniFile = File.createTempFile("xds-ide", ".ini"); //$NON-NLS-1$ //$NON-NLS-2$
			File tempIniFile = File.createTempFile("xds-ideTemp", ".ini"); //$NON-NLS-1$ //$NON-NLS-2$
			FileUtils.copyFile(new File(iniFile), backupIniFile);

			boolean isNeedRestoreOfIni = false;

			BufferedReader reader = new BufferedReader(new FileReader(iniFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempIniFile));
			
			Map<String, String> key2Value = new LinkedHashMap<String, String>();
			
			try {
				String line;
				String key = null;
				String value = null;
				
				boolean hasChanges = false;
				
				try {
					while ((line = reader.readLine()) != null) {
						String trimmedLine = line.trim();
						if (trimmedLine.startsWith("-")) { // we have an option name //$NON-NLS-1$
							if (changeOptionName2Value.containsKey(trimmedLine)) {
								value = changeOptionName2Value.get(trimmedLine);
								changeOptionName2Value.remove(trimmedLine);
								if (value != null) { // if null value specified - remove corresponding key /value
									key2Value.put(trimmedLine, value);
								}
								hasChanges = true;
								key = null; // skip this key
							}
							else {
								key = trimmedLine;
								key2Value.put(trimmedLine, ""); //$NON-NLS-1$
							}
						} else { // we have an option value
							if (key != null) {
								key2Value.put(key, line);
							}

							value = null;
							key = null;
						}
					}
					
					if (hasChanges || isWriteIfNotKeyPresent) {
						for(Map.Entry<String, String> pair : key2Value.entrySet()) {
							writer.write(pair.getKey() + LINE_SEPARATOR);
							if (!StringUtils.isBlank(pair.getValue())) {
								writer.write(pair.getValue() + LINE_SEPARATOR);
							}
						}
						hasChanges = true;
					}
					
					if (isWriteIfNotKeyPresent && changeOptionName2Value.size() > 0) {
						for(Map.Entry<String, String> pair : changeOptionName2Value.entrySet()) {
							writer.write(pair.getKey() + LINE_SEPARATOR + pair.getValue() + LINE_SEPARATOR);
						}
						hasChanges = true;
					}
				} finally {
					writer.close();
					reader.close();
				}
				
				if (hasChanges) {
					isNeedRestoreOfIni = true;
					FileUtils.copyFile(tempIniFile, new File(iniFile));
				}
			} catch (Throwable t) {
				if (isNeedRestoreOfIni) {
					FileUtils.copyFile(backupIniFile, new File(iniFile));
				}
				throw t;
			}
		}
	}
}
