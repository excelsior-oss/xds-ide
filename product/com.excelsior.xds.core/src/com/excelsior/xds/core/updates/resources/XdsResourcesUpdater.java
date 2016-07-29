package com.excelsior.xds.core.updates.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.WorkbenchException;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.updates.RepositoryUtils;
import com.excelsior.xds.core.updates.descriptor.DescriptorParser;
import com.excelsior.xds.core.updates.descriptor.InstanceDirectoryUpdate;
import com.excelsior.xds.core.updates.descriptor.InstanceDirectoryUpdate.FileUpdate;
import com.excelsior.xds.core.updates.descriptor.Update;
import com.excelsior.xds.core.updates.descriptor.UpdateDirDescriptor;
import com.excelsior.xds.core.updates.descriptor.UpdateSite;
import com.excelsior.xds.core.utils.xml.ValidationResult;
import com.excelsior.xds.core.utils.xml.XmlUtils;

public class XdsResourcesUpdater {
	private static final String XSD_FILE_EXT = "xsd"; //$NON-NLS-1$
	private static final String ECLIPSE_HOME_LOCATION_PROPERTY = "eclipse.home.location"; //$NON-NLS-1$

	private static class XdsResourcesUpdaterHolder {
		static XdsResourcesUpdater INSTANCE = new XdsResourcesUpdater();
	}

	public static XdsResourcesUpdater getInstance() {
		return XdsResourcesUpdaterHolder.INSTANCE;
	}

	public void updateResources() {
		UpdateDirDescriptor desc = DescriptorParser.parse();
		if (desc != null) {
			try {
				applyPluginResourceUpdates(desc);
				applyInstanceResourceUpdates(desc);
			} catch (MalformedURLException e) {
				LogHelper.logError(e);
			} catch (URISyntaxException e) {
				LogHelper.logError(e);
			} catch (WorkbenchException e) {
				LogHelper.logError(e);
			} catch (IOException e) {
				LogHelper.logError(e);
			}
			addUpdateSitesIfNotPresent(desc.updateSites);
		}
	}

	private void addUpdateSitesIfNotPresent(List<UpdateSite> updateSites) {
		for (UpdateSite updateSite : updateSites) {
			RepositoryUtils.addUpdateSite(updateSite.url);
		}
	}

	private void applyPluginResourceUpdates(UpdateDirDescriptor desc) throws WorkbenchException, IOException {
		InstalledUpdatesRegistry installedUpdatesRegistry = InstalledUpdatesManager.getInstance().loadInstalledUpdatesRegistry();
		
		boolean isNewUpdatesInstalled = false;
		
		String updateDirPath = desc.updateDirPath;
		List<Update> updates = desc.updates;
		Map<String, Bundle> pluginName2Bundle = new HashMap<String, Bundle>();
		Set<String> unresolvedBundleNames = new HashSet<String>();
		for (Update update : updates) {
			Bundle bundle = pluginName2Bundle.get(update.targetPluginName);
			if (bundle == null) {
				bundle = Platform.getBundle(update.targetPluginName);
				if (bundle == null) {
					unresolvedBundleNames.add(update.targetPluginName);
					continue;
				}
				pluginName2Bundle.put(update.targetPluginName, bundle);
			}
			if (updateResource(installedUpdatesRegistry, bundle, updateDirPath, update)) {
				isNewUpdatesInstalled = true;
			}
		}
		if (isNewUpdatesInstalled) {
			InstalledUpdatesManager.getInstance().saveInstalledUpdatesRegistry(installedUpdatesRegistry);
		}
		
		for (String unresolvedBundleName : unresolvedBundleNames) {
			LogHelper.logError(String.format("Bundle '%s' was not found during update", unresolvedBundleName)); //$NON-NLS-1$
		}
	}
	
	private void applyInstanceResourceUpdates(UpdateDirDescriptor desc) throws URISyntaxException, WorkbenchException, IOException {
		InstalledUpdatesRegistry installedUpdatesRegistry = InstalledUpdatesManager.getInstance().loadInstalledUpdatesRegistry();
		boolean isNewUpdatesInstalled = false;
		
		String eclipseInstallDir = System.getProperty(ECLIPSE_HOME_LOCATION_PROPERTY);
		for (InstanceDirectoryUpdate instanceDirectoryUpdate : desc.instanceDirectoryUpdates) {
			File targetDirectory = new File(new URL(FilenameUtils.concat(eclipseInstallDir, instanceDirectoryUpdate.name)).toURI());
			if (!targetDirectory.exists()) {
				LogHelper.logError(String.format("Resource update : incorrect instance directory specified : '%s'", targetDirectory)); //$NON-NLS-1$
				continue;
			}
			List<FileUpdate> fileUpdates = instanceDirectoryUpdate.fileUpdates;
			for (FileUpdate fileUpdate : fileUpdates) {
				if (updateResource(installedUpdatesRegistry, desc.updateDirPath, fileUpdate.source, targetDirectory, 
						 fileUpdate.name, null, fileUpdate.version, false)) {
					isNewUpdatesInstalled = true;
				}
			}
		}
		if (isNewUpdatesInstalled) {
			InstalledUpdatesManager.getInstance().saveInstalledUpdatesRegistry(installedUpdatesRegistry);
		}
	}

	/**
	 * @param installedUpdatesRegistry 
	 * @param dirWithUpdatesPath directory where updates are
	 * @param updateDirSubdirWithResource relative path where update is
	 * @param newResourceName update resource name
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @return true if the resource was updated
	 */
	private boolean updateResource(InstalledUpdatesRegistry installedUpdatesRegistry, Bundle resourcesBundle, String dirWithUpdatesPath, Update update) {
		String newResourceRelativePath = update.newResourceLocation;
		String existingResourceRelativePath = update.existingResourceLocation;
		String xmlSchemaRelativePath = update.xmlSchemaLocation;
		Version version = update.version;
		File resourceFolderFile = null;
		try {
			resourceFolderFile = FileLocator.getBundleFile(resourcesBundle);
		} catch (IOException e) {
			LogHelper.logError(e);
			return false;
		}
		
		return updateResource(installedUpdatesRegistry, dirWithUpdatesPath,
				newResourceRelativePath, resourceFolderFile,
				existingResourceRelativePath, xmlSchemaRelativePath, version, true);
	}

	/**
	 * @param installedUpdatesRegistry - registry of installed updates
	 * @param dirWithUpdatesPath - directory containing update resource to be copied
	 * @param newResourceRelativePath - relative to dirWithUpdatesPath location of the resource to be copied
	 * @param resourceFolderFile - folder containing old version of the resource
	 * @param existingResourceRelativePath - relative to resourceFolderFile old resource
	 * @param xmlSchemaRelativePath - optional xmlSchema validator
	 * @param version - new version of the resource
	 * @return true if the resource was updated
	 */
	private boolean updateResource(InstalledUpdatesRegistry installedUpdatesRegistry,
			String dirWithUpdatesPath, String newResourceRelativePath,
			File resourceFolderFile, String existingResourceRelativePath,
			String xmlSchemaRelativePath, Version version, boolean isDemandResourceExistence) {
		boolean isUpdated = false;
		String newResourceAbsoluteLocation = FilenameUtils.separatorsToUnix(FilenameUtils.concat(dirWithUpdatesPath, newResourceRelativePath));
		File newResourceFile = new File(newResourceAbsoluteLocation);
		try {
			
			File targetResourceFile = new File(FilenameUtils.concat(resourceFolderFile.toString(), existingResourceRelativePath));
			// TODO : maybe we need a better way to protect schema validation file, like hide it inside jar
			boolean isNotXsd = !XSD_FILE_EXT.equals(FilenameUtils.getExtension(targetResourceFile.getName()));
			boolean isNewResourceFileOK = isNormalFile(newResourceFile, true);
			if (!isNewResourceFileOK) {
				LogHelper.logError(String.format("Resource update : Incorrect update source specified : '%s'", newResourceFile)); //$NON-NLS-1$
			}
			boolean isTargetResourceFileOK = isNormalFile(targetResourceFile, isDemandResourceExistence);
			if (!isTargetResourceFileOK) {
				LogHelper.logError(String.format("Resource update : Incorrect target resource specified : '%s'", newResourceFile)); //$NON-NLS-1$
			}
			if (isNewResourceFileOK && isTargetResourceFileOK && isNotXsd) {
				if (xmlSchemaRelativePath != null) {
					File schemaPath = new File(FilenameUtils.concat(resourceFolderFile.toString(), xmlSchemaRelativePath ));
					if (!schemaPath.exists()) {
						LogHelper.logError(String.format("Resource update : The following schema resource not exists : '%s'", schemaPath)); //$NON-NLS-1$
						return isUpdated;
					}
					
					ValidationResult validationResult = XmlUtils.validateAgainstSchema(newResourceFile.toURI(), schemaPath.getAbsolutePath());
					if (!validationResult.isValid()) {
						LogHelper.logError(String.format("Resource update : XSD validation failed for file : '%s'", newResourceRelativePath)); //$NON-NLS-1$
						return isUpdated;
					}
				}
			
				if (installedUpdatesRegistry.isNewerThanInstalled(targetResourceFile.getAbsolutePath(), version)) {
					LogHelper.logInfoInLogMode(String.format("File %s updated with %s. Version %s", targetResourceFile, newResourceFile, version)); //$NON-NLS-1$
					try(FileInputStream inputStream = new FileInputStream(newResourceFile)){
						try(FileOutputStream outputStream = new FileOutputStream(targetResourceFile)){
							IOUtils.copy(inputStream, outputStream);
							installedUpdatesRegistry.add(targetResourceFile.getAbsolutePath(), version);
							isUpdated = true;
						}
					}
				}
			}
			else if (!newResourceFile.exists()){
				LogHelper.logError(String.format("Resource update : Update directory doesnot contain the following file : '%s'", newResourceRelativePath)); //$NON-NLS-1$
			}
			else if (!targetResourceFile.exists()){
				LogHelper.logError(String.format("Resource update : The following file not exists : '%s'", targetResourceFile)); //$NON-NLS-1$
			}
		} catch (IOException e) {
			LogHelper.logError(e);
		}
		return isUpdated;
	}
	
	private static boolean isNormalFile(File file, boolean isDemandResourceExistence) {
		if (!isDemandResourceExistence) {
			return file.exists() ? file.isFile() : true;
		}
		return file.isFile();
	}
}
