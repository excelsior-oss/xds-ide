package com.excelsior.xds.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import com.excelsior.xds.utils.updatesite.BundleDescriptor;
import com.excelsior.xds.utils.updatesite.Feature;
import com.excelsior.xds.utils.updatesite.FeatureEntry;
import com.excelsior.xds.utils.updatesite.FeatureParser;
import com.excelsior.xds.utils.updatesite.InstanceInspector;

/**
 * This class controls all aspects of the application's execution
 */
public class Main  {

	private static final String SITE_OPTION = "site";
	private static final String REPO_OPTION = "repo";
	private static final String INSTANCE_OPTION = "instance";
	private static final String LOCALIZATION_FRAGMENT_POSTFIX = "l10n_postfix";
	
	/*
	 * Sample usage : 
	 * --instance d:\tmp\xds-ide  
	 * --repo d:\lsa80\MyProjects\Modulipse_Excelsior\src\xdsclipse\src\localization\ru 
	 * --l10n_postfix nl_ru
	 * --site D:\lsa80\MyProjects\Modulipse_Excelsior\src\xdsclipse\src\product\com.excelsior.xds.utils\site
	 * */
	public static void main(String[] args) throws Exception {
		Options options = new Options();

		options.addOption(INSTANCE_OPTION, true, "Eclipse Instance path");
		options.addOption(REPO_OPTION, true, "Plugins and Features repo path");
		options.addOption(SITE_OPTION, true, "Output site path");
		options.addOption(LOCALIZATION_FRAGMENT_POSTFIX, true,
				"Localization fragment postfix");

		CommandLineParser parser = new PosixParser();
		CommandLine cmdLine = parser.parse(options, args);
		String repo = getAndValidateDirectoryOption(cmdLine, REPO_OPTION);
		String site = getAndValidateDirectoryOption(cmdLine, SITE_OPTION, true);
		String instance = getAndValidateDirectoryOption(cmdLine, INSTANCE_OPTION);
		if (repo == null || site == null || instance == null) {
			return;
		}
		
		String postfix = cmdLine.getOptionValue(LOCALIZATION_FRAGMENT_POSTFIX);
		
		InstanceInspector instanceInspector = new InstanceInspector();
		Map<String, BundleDescriptor> id2BundleDescriptor = instanceInspector
				.getBundleDescriptors(instance);
		
		Template velocityTemplate = initializeVelocityTemplate();

		File[] folders = new File(repo + "/features")
				.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
		for (File folder : folders) {
			System.out.println("Processing feature " + folder.getName() + " ...");
			processFeatureFolder(velocityTemplate, postfix, id2BundleDescriptor, folder, repo,
					site);
		}
		
		System.out.println("Site created");
	}
	
	private static String getAndValidateDirectoryOption(CommandLine cmdLine, String optionName) {
		return getAndValidateDirectoryOption(cmdLine, optionName, false);
	}
	
	private static String getAndValidateDirectoryOption(CommandLine cmdLine, String optionName, boolean isCreateIfNotExist) {
		String val = cmdLine.getOptionValue(optionName);
		if (val == null) {
			System.out.println(String.format("Option %s not specified", optionName));
			return null;
		}
		if (!new File(val).exists()) {
			if (isCreateIfNotExist) {
				new File(val).mkdirs();
			} else {
				System.out.println(String.format("Option %s specifies path: %s \n\t which doesnot exists", optionName, val));
				return null;
			}
		}
		
		return val;
	}
	
	private static void processFeatureFolder(Template velocityTemplate, String postfix,
			Map<String, BundleDescriptor> id2BundleDescriptor,
			File featureFolder, String repo, String site) throws Exception {

		String featureXml = FilenameUtils.concat(
				featureFolder.getAbsolutePath(), "feature.xml");
		Feature feature = FeatureParser.parseFeature(featureXml);
		FeatureEntry[] entries = feature.getEntries();

		String repoPluginFolderPath = repo + "/plugins/";
		
		String sitePluginFolderPath = site + "/plugins/";
		new File(sitePluginFolderPath).mkdirs();

		List<FeatureEntry> filteredEntries = new ArrayList<FeatureEntry>();
		
		for (FeatureEntry featureEntry : entries) {
			String id = featureEntry.getId();
			if (postfix != null) {
				if (!id.contains(postfix)) {
					continue;
				}
				id = id.replace("." + postfix, "");
			}
			if ((id2BundleDescriptor != null && id2BundleDescriptor.containsKey(id)) || 
					id2BundleDescriptor == null) {
				filteredEntries.add(featureEntry);
				String fromPath = FilenameUtils.concat(repoPluginFolderPath, featureEntry.getId() + "_" + featureEntry.getVersion() + ".jar");
				String toPath = FilenameUtils.concat(sitePluginFolderPath, featureEntry.getId() + "_" + featureEntry.getVersion() + ".jar");
				IOUtils.copy(new FileInputStream(fromPath),
						new FileOutputStream(toPath));
			}
		}
		
		if (!filteredEntries.isEmpty()) {
			String resultFeatureXmlPath = site + "/features/" + featureFolder.getName() + "/feature.xml";

			File resultFeatureXmlFile = new File(resultFeatureXmlPath);
			resultFeatureXmlFile.getParentFile().mkdirs();

			FileWriter feautureInfoWriter = new FileWriter(resultFeatureXmlFile);

			VelocityContext context = new VelocityContext();
			context.put("feature", feature);
			context.put("featureEntries", filteredEntries);
			velocityTemplate.merge(context, feautureInfoWriter);

			feautureInfoWriter.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
	}
	
	private static Template initializeVelocityTemplate() throws IOException {
		VelocityEngine velocityEngine = new VelocityEngine();
		Properties p = new Properties();
		p.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
		p.setProperty("file.resource.loader.path", "./data/");
		velocityEngine.init(p);
		Template template = velocityEngine.getTemplate("feature_template.xml");
		return template;
	}
}
