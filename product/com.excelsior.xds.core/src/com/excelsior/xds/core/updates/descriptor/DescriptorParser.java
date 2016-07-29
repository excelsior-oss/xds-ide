package com.excelsior.xds.core.updates.descriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.osgi.framework.Version;

import com.excelsior.xds.core.log.LogHelper;

public final class DescriptorParser {
	private static final String DIRECTORY_VERSION_TAG = "version"; //$NON-NLS-1$
	private static final String DIRECTORY_DESCRIPTION_TAG = "description"; //$NON-NLS-1$
	private static final String DIRECTORY_SOURCE_TAG = "source"; //$NON-NLS-1$
	private static final String DIRECTORY_NAME_TAG = "name"; //$NON-NLS-1$
	private static final String VERSION_TAG = DIRECTORY_VERSION_TAG;
	private static final String PLUGIN_DESCRIPTION = DIRECTORY_DESCRIPTION_TAG;
	private static final String SITE_URL = "url"; //$NON-NLS-1$
	private static final String PLUGIN = "plugin"; //$NON-NLS-1$
	private static final String PLUGIN_SOURCE = DIRECTORY_SOURCE_TAG;
	private static final String SITE_TAG = "site"; //$NON-NLS-1$
	private static final String GENERAL_INI_SECTION = "General"; //$NON-NLS-1$
	
	private static final String VERIFIER_TAG = "verifier"; //$NON-NLS-1$
	private static final String SOURCE_TAG = PLUGIN_SOURCE; //$NON-NLS-1$
	private static final String FILE_NAME_TAG = DIRECTORY_NAME_TAG; //$NON-NLS-1$
	private static final String FILE_UPDATE_TAG = "file"; //$NON-NLS-1$
	private static final String PLUGIN_NAME_TAG = PLUGIN; //$NON-NLS-1$
	private static final String RESOURCES_TAG = "resources"; //$NON-NLS-1$
	private static final String UPDATE_DIR_PROPERTY_NAME = "UpdateDescriptor"; //$NON-NLS-1$
	private static final String UPDATE_FILE_NAME = "update.ini"; //$NON-NLS-1$
	private static final String ECLIPSE_HOME_LOCATION_PROPERTY = "eclipse.home.location"; //$NON-NLS-1$
	
	private static final String DIRECTORY_TAG = "directory"; //$NON-NLS-1$
	private static final String DIRECTORY_FILE_TAG = "file"; //$NON-NLS-1$
	
	private DescriptorParser(){}

	/**
	 * @return null on incorrect update descriptor
	 */
	public static UpdateDirDescriptor parse() {
		String eclipseInstallDir = System.getProperty(ECLIPSE_HOME_LOCATION_PROPERTY);
		try {
			File updatePropertiesFile = new File(new URL(FilenameUtils.concat(
					eclipseInstallDir, UPDATE_FILE_NAME)).toURI());
			if (updatePropertiesFile.exists()) {
				String updateDescriptorURI = getUpdateDescrpitorURI(updatePropertiesFile);
				File updateDescFile = null;
				try {
					updateDescFile = new File(new URI(updateDescriptorURI));
				}
				catch(IllegalArgumentException e) { // URI not valid
					return null;
				}
				
				return parsePendingUpdates(updateDescFile);
			}
		} catch (FileNotFoundException e) {
			LogHelper.logError(e);
		} catch (WorkbenchException e) {
			LogHelper.logError(e);
		} catch (IOException e) {
			LogHelper.logError(e);
		} catch (URISyntaxException e) {
			LogHelper.logError(e);
		}
		
		return null;
	}

	private static String getUpdateDescrpitorURI(File updatePropertiesFile)
			throws InvalidFileFormatException, IOException {
		final Ini ini = new Ini(updatePropertiesFile);
		return ini.get(GENERAL_INI_SECTION, UPDATE_DIR_PROPERTY_NAME);
	}
	
	public static UpdateDirDescriptor parsePendingUpdates(File updateDescFile) throws IOException, WorkbenchException {
		if (!updateDescFile.exists()) {
			return null;
		}
		List<Update> updates = new ArrayList<Update>();
		List<UpdateSite> updateSites = new ArrayList<UpdateSite>();
		
		try(InputStreamReader reader = new FileReader(updateDescFile)) {
			XMLMemento updatePropertiesMemento = XMLMemento.createReadRoot(reader);
			
			IMemento[] resourcesChildren = updatePropertiesMemento.getChildren(RESOURCES_TAG);
			for (IMemento resourcesChild : resourcesChildren) {
				String pluginName = resourcesChild.getString(PLUGIN_NAME_TAG);
				
				IMemento[] updateChildren = resourcesChild.getChildren(FILE_UPDATE_TAG);
				for (IMemento updateChild : updateChildren) {
					String pluginRootRelativeLocation = updateChild.getString(FILE_NAME_TAG);
					
					String newResourceLocation = updateChild.getString(SOURCE_TAG);
					String xmlSchemaLocation = updateChild.getString(VERIFIER_TAG);
					String versionStr = updateChild.getString(VERSION_TAG);
					Version version = null;
					try {
						version = new Version(versionStr);
						updates.add(new Update(pluginName, newResourceLocation, pluginRootRelativeLocation, xmlSchemaLocation, version));
					}
					catch (IllegalArgumentException e) {
						LogHelper.logError(String.format("Incorrect version format specified for the file element %s", pluginRootRelativeLocation)); //$NON-NLS-1$
					}
				}
			}
			
			IMemento[] updateSiteChildren = updatePropertiesMemento.getChildren(SITE_TAG);
			for (IMemento updateSiteChild : updateSiteChildren) {
				String url = updateSiteChild.getString(SITE_URL);
				updateSites.add(new UpdateSite(url));
			}
			
			List<PluginUpdate> pluginUpdates = new ArrayList<PluginUpdate>();
			IMemento[] pluginChildren = updatePropertiesMemento.getChildren(PLUGIN);
			for (IMemento pluginChild : pluginChildren) {
				String source = pluginChild.getString(PLUGIN_SOURCE);
				String desc = pluginChild.getString(PLUGIN_DESCRIPTION);
				PluginUpdate pluginUpdate = new PluginUpdate(source, desc);
				pluginUpdates.add(pluginUpdate);
			}
			
			List<InstanceDirectoryUpdate> instanceDirectoryUpdates = new ArrayList<InstanceDirectoryUpdate>();
			
			IMemento[] directoryChildren = updatePropertiesMemento.getChildren(DIRECTORY_TAG);
			for (IMemento directoryChild : directoryChildren) {
				String dirName = directoryChild.getString(DIRECTORY_NAME_TAG);
				IMemento[] fileChildren = directoryChild.getChildren(DIRECTORY_FILE_TAG);
				List<InstanceDirectoryUpdate.FileUpdate> fileUpdates = new ArrayList<InstanceDirectoryUpdate.FileUpdate>();
				for (IMemento fileChild : fileChildren) {
					String name = fileChild.getString(DIRECTORY_NAME_TAG);
					String source = fileChild.getString(DIRECTORY_SOURCE_TAG);
					String description = fileChild.getString(DIRECTORY_DESCRIPTION_TAG);
					String versionStr = fileChild.getString(DIRECTORY_VERSION_TAG);
					Version version = null;
					try{
						version = new Version(versionStr);
						InstanceDirectoryUpdate.FileUpdate fileUpdate = new InstanceDirectoryUpdate.FileUpdate(name, source, description, version);
						fileUpdates.add(fileUpdate);
					}
					catch(IllegalArgumentException e){
						LogHelper.logError(String.format("Incorrect format of the <%s> element", DIRECTORY_TAG)); //$NON-NLS-1$
					}
				}
				InstanceDirectoryUpdate instanceDirectoryUpdate = new InstanceDirectoryUpdate(dirName, fileUpdates);
				instanceDirectoryUpdates.add(instanceDirectoryUpdate);
			}
			return new UpdateDirDescriptor(updateDescFile, updates, updateSites, pluginUpdates, instanceDirectoryUpdates);
		}
	}
}
