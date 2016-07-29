package com.excelsior.xds.core.updates.dropins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;

import com.excelsior.xds.core.updates.descriptor.PluginUpdate;
import com.excelsior.xds.core.updates.descriptor.UpdateDirDescriptor;

/**
 * Update com.excelsior.xds at dropins folders, using specified updateDirectory as jar source
 * 
 * @author lsa80
 */
public class XdsPluginUpdater {

	private static final String DROPINS_DIR_NAME = "dropins";//$NON-NLS-1$
	
	public static boolean areNewPluginsPending(UpdateDirDescriptor desc) {
		if (desc == null) return false; // update dir was not specified
		boolean result = false;
		Collection<File> xdsPluginJarFiles = getXdsPluginJars(desc);

		if (xdsPluginJarFiles.isEmpty()) {
			result = false;
		} else {
			xdsPluginJarFiles = getNewestVersions(xdsPluginJarFiles);
			
			for (File xdsPluginJarFile : xdsPluginJarFiles) {
				if (areSimilarInstalledPluginsOlder(xdsPluginJarFile)) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	public static void doUpdate(UpdateDirDescriptor desc) throws IOException {
		String installPath = Platform.getInstallLocation().getURL().getPath();
		File dropinsDir = new File(FilenameUtils.concat(installPath, DROPINS_DIR_NAME));
		
		Collection<File> xdsPluginJarFiles = getXdsPluginJars(desc);
		xdsPluginJarFiles = getNewestVersions(xdsPluginJarFiles);
		
		List<String> copiedJarFiles = new ArrayList<String>();
		try {
			for (File xdsPluginJarFile : xdsPluginJarFiles) {
				if (areSimilarInstalledPluginsOlder(xdsPluginJarFile)) {
					FileUtils.copyFileToDirectory(xdsPluginJarFile, dropinsDir);
					final String targetJarFilePath = FilenameUtils.concat(dropinsDir.getAbsolutePath(), FilenameUtils.getName(xdsPluginJarFile.getAbsolutePath()));
					copiedJarFiles.add(targetJarFilePath);
				}
			}
		}
		catch (IOException e) {
			// on error - try to clean up the mess
			for (String targetJarFilePath : copiedJarFiles) {
				FileUtils.deleteQuietly(new File(targetJarFilePath));
			}
			throw e;
		}
	}

	private static Collection<File> getNewestVersions(Collection<File> xdsPluginJarFiles) {
		Map<String, File> pluginId2NewestPluginJarFile = new HashMap<String, File>();
		for (File xdsPluginJarFile : xdsPluginJarFiles) {
			String pluginId = getPluginId(xdsPluginJarFile);
			File newestPluginJarFile = pluginId2NewestPluginJarFile.get(pluginId);
			if (newestPluginJarFile == null) {
				pluginId2NewestPluginJarFile.put(pluginId, xdsPluginJarFile);
			}
			else{
				if (isNewerThan(xdsPluginJarFile, newestPluginJarFile)) {
					pluginId2NewestPluginJarFile.put(pluginId, xdsPluginJarFile);
				}
			}
		}
		return pluginId2NewestPluginJarFile.values();
	}
	
	private static boolean areSimilarInstalledPluginsOlder(File xdsPluginJarFile) {
		boolean result = false;
		Collection<File> pluginFiles = getSimilarInstalledPlugins(xdsPluginJarFile);
		if (org.apache.commons.collections.CollectionUtils.isEmpty(pluginFiles)) {
			result = true;
		}
		else {
			File newestInstalledPluginJarFile = getNewestPlugin(pluginFiles);
			if (isNewerThan(xdsPluginJarFile, newestInstalledPluginJarFile)) {
				result = true;
			}
		}
		
		return result;
	}
	
	private static File getNewestPlugin(Collection<File> pluginFiles) {
		return Collections.max(pluginFiles, IsNewerThanPluginJarComparator.INSTANCE);
	}
	
	private static Collection<File> getSimilarInstalledPlugins(
			File xdsPluginJarFile) {
		return FileUtils.listFiles(new File(getDropinsDirPath()), new SimilarPluginFileFilter(xdsPluginJarFile), TrueFileFilter.INSTANCE);
	}
	
	private static class IsNewerThanPluginJarComparator implements Comparator<File> {
		static IsNewerThanPluginJarComparator INSTANCE = new IsNewerThanPluginJarComparator();
		@Override
		public int compare(File firstPlugin, File secondPlugin) {
			return comparePluginVersions(firstPlugin, secondPlugin);
		}
	}
	
	public static boolean isNewerThan(File firstPlugin, File secondPlugin) {
		return comparePluginVersions(firstPlugin, secondPlugin) > 0;
	}
	
	private static int comparePluginVersions(File firstPlugin, File secondPlugin) {
		Version firstVersion = new Version(getVersion(firstPlugin));
		Version secondVersion = new Version(getVersion(secondPlugin));
		
		return firstVersion.compareTo(secondVersion);
	}
	
	private static String getVersion(File plugin) {
		return FilenameUtils.getName(plugin.getAbsolutePath()).replaceFirst("^.*_", "").replaceFirst("[.]jar$", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	private static String getPluginId(File plugin) {
		return FilenameUtils.getName(plugin.getAbsolutePath()).replaceFirst("_.*$", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private static Collection<File> getXdsPluginJars(UpdateDirDescriptor desc) {
		Collection<File> fileList = new ArrayList<File>();
		for (PluginUpdate pluginUpdate : desc.pluginUpdates) {
			File pluginFile = new File(FilenameUtils.concat(desc.updateDirPath, pluginUpdate.pluginLocation));
			fileList.add(pluginFile);
		}
		return fileList;
	}
	
	private static class SimilarPluginFileFilter implements IOFileFilter {

		private String fileNameExpr;
		
		public SimilarPluginFileFilter(File xdsPluginJarFile) {
			this(xdsPluginJarFile.getAbsolutePath());
		}
		
		public SimilarPluginFileFilter(String path) {
			String fileName = FilenameUtils.getName(path);
			// Convert "org.apache.ant.source_1.8.2.v20120109-1030" -> "org.apache.ant.source.*[.]jar"
			fileNameExpr = fileName.replaceFirst("_.*[.]jar$", "") + ".*[.]jar";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		@Override
		public boolean accept(File file) {
			String fileName = FilenameUtils.getName(file.getAbsolutePath());
			return fileName.matches(fileNameExpr);
		}

		@Override
		public boolean accept(File dir, String name) {
			return true;
		}
	}
	
	@SuppressWarnings("unused")
	private static class XdsPluginJarFileFilter implements IOFileFilter {
		public static XdsPluginJarFileFilter INSTANCE = new XdsPluginJarFileFilter();

		@Override
		public boolean accept(File file) {
			boolean isCorrectJar = FilenameUtils
					.getName(file.getAbsolutePath()).matches(
							"com[.]excelsior[.]xds.*[.]jar");//$NON-NLS-1$
			return isCorrectJar;
		}

		@Override
		public boolean accept(File dir, String name) {
			return true;
		}
	}
	
	private static String getDropinsDirPath() {
		String installPath = Platform.getInstallLocation().getURL().getPath();
		return FilenameUtils.concat(installPath, DROPINS_DIR_NAME);
	}

	public static enum UpdateDirectoryValidationResult {
		NO_ERROR, EMPTY, ALREADY_EXISTS
	}
}
