package com.photon.phresco.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.plist.XMLPropertyListConfiguration;
import org.apache.commons.lang.StringUtils;

import com.photon.phresco.api.ConfigManager;
import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.configuration.Configuration;
import com.photon.phresco.configuration.Environment;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration.Parameters.Parameter;
import com.photon.phresco.plugins.util.MojoProcessor;
import com.photon.phresco.util.Constants;
import com.photon.phresco.util.ProjectUtils;
import com.photon.phresco.util.Utility;
import com.phresco.pom.exception.PhrescoPomException;
import com.phresco.pom.util.PomProcessor;

public class IPhonePhrescoApplicationProcessor extends AbstractApplicationProcessor{
	private static final String PLIST = "feature-manifest.plist";
	private static final String INFO_PLIST = "info.plist";
	private static final String FEATURES = "features";
	private static final String NAME = "name";
	private static final String ENVIRONMENT_PLIST = "environment.plist";
	private static final String RESOURCE = "Resource";

	@Override
	public void postUpdate(ApplicationInfo appInfo, List<ArtifactGroup> artifactGroup, List<ArtifactGroup> deletedFeatures) throws PhrescoException {
		File pomFile = new File(Utility.getProjectHome() + appInfo.getAppDirName() + File.separator + Constants.POM_NAME);
		ProjectUtils projectUtils = new ProjectUtils();
		projectUtils.deletePluginExecutionFromPom(pomFile);
		if(!artifactGroup.isEmpty()) { 
			projectUtils.updatePOMWithPluginArtifact(pomFile, artifactGroup);
		}
		BufferedReader breader = projectUtils.ExtractFeature(appInfo);
		try {
			String line = "";
			while ((line = breader.readLine()) != null) {
				if (line.startsWith("[ERROR]")) {
					System.err.println(line);
				}
			}
		} catch (IOException e) {
			throw new PhrescoException(e);
		}
	}

	@Override
	public void postConfiguration(ApplicationInfo appInfo, List<Configuration> configs) throws PhrescoException {
		File ConfigFilePath = new File(Utility.getProjectHome() + appInfo.getAppDirName() + File.separator + Constants.DOT_PHRESCO_FOLDER + File.separator + Constants.CONFIGURATION_INFO_FILE);
		try {
			ConfigManager configManager = new ConfigManagerImpl(ConfigFilePath);
			List<Environment> environments = configManager.getEnvironments();
			for (Environment environment : environments) {
				String environmenName = "";
				Map<String, List<Properties>> values = new HashMap<String, List<Properties>>();
				String environmentName = environment.getName();
				File file = new File(Utility.getProjectHome() + appInfo.getAppDirName() + getThirdPartyFolder(appInfo) + File.separator + environmentName);
				if(!file.exists()) {
					file.mkdir();
				}
				List<Configuration> configurations = environment.getConfigurations();
				String plistFile = file.getPath() + File.separator + ENVIRONMENT_PLIST;
				if (CollectionUtils.isNotEmpty(configurations)) {
					storeConfigObjAsPlist(configurations, plistFile);
				}
			}
		}catch (Exception e ) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Configuration> preFeatureConfiguration(ApplicationInfo appInfo, String featureName) throws PhrescoException {
		File plistFile = new File(Utility.getProjectHome() + appInfo.getAppDirName() + getThirdPartyFolder(appInfo) + File.separator + featureName + File.separator + featureName+RESOURCE +File.separator + PLIST);
		try {
			//		    if (!plistFile.exists()) {
			//		        throw new PhrescoException("feature-manifest.plist file does not exist");
			//		    }
			return getConfigFromPlist(plistFile.getPath());
		} catch (Exception e) {
			throw new PhrescoException(e);
		}
	}

	@Override
	public void postFeatureConfiguration(ApplicationInfo appInfo, List<Configuration> configs, String featureName)
	throws PhrescoException {
		try {
			String plistPath = Utility.getProjectHome() + appInfo.getAppDirName() + getThirdPartyFolder(appInfo) + File.separator + featureName + File.separator + featureName+RESOURCE +File.separator + INFO_PLIST;
			storeConfigObjAsPlist(configs, plistPath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new PhrescoException(e);
		}

	}

	private void storeConfigObjAsPlist(List<Configuration> configuration, String destPlistFile) throws PhrescoException {
		//FIXME: this logic needs to be revisited and should be fixed with XMLPropertyListConfiguration classes.
		try {
			XMLPropertyListConfiguration plist = new XMLPropertyListConfiguration();
			for(Configuration config : configuration) {
				if(FEATURES.equalsIgnoreCase(config.getType())) {
					String rootNode = "";
					HashMap<String, Object> valueMap = new HashMap();

					Properties properties = config.getProperties();
					Enumeration em = properties.keys();
					while (em.hasMoreElements()) {
						HashMap<String, Object> tempMap = new HashMap();
						String key = (String) em.nextElement();
						String value = properties.getProperty(key);
						String[] keySplited = key.split("\\.");
						rootNode = keySplited[0];
						int length = keySplited.length;
						if (value != null && value instanceof String && value.toString().startsWith("[") && value.toString().endsWith("]")) {

							String arrayListvalue = value.toString().replace("[", "").replace("]", "");
							String[] split = arrayListvalue.toString().split(",");
							Map<String, Object> keyValuePair = new HashMap<String, Object>();
							List<String> asList = new ArrayList<String>();
							for (String string : split) {
								asList.add(string.trim());
							}
							String rootKey = "";
							String arrayListKey = "";
							if(length == 3 ) {
								rootKey = keySplited[1];
								arrayListKey = keySplited[2];
								keyValuePair.put(arrayListKey, asList);
								if(!valueMap.isEmpty()) {
									if(valueMap.containsKey(keySplited[1])) {
										HashMap<String, Object> object = (HashMap) valueMap.get(keySplited[1]);
										tempMap.put(arrayListKey, asList);

										for (Map.Entry<String, Object> entry : object.entrySet()) {
											tempMap.put(entry.getKey(), entry.getValue());
										}

										valueMap.put(rootKey, tempMap);
									} else {
										valueMap.put(rootKey, keyValuePair);
									}
								} else {
									valueMap.put(rootKey, keyValuePair);
								}
							} else if( length == 2){
								valueMap.put(keySplited[1], asList);
							}
						} else { 
							String arrayListKey = "";
							if(length == 2) {
								arrayListKey = keySplited[1];
								valueMap.put(arrayListKey, value);
							} else if (length == 3){
								HashMap localMap = new HashMap();
								localMap.put(keySplited[2], value);
								if(!valueMap.isEmpty()) {
									if(valueMap.containsKey(keySplited[1])) {
										HashMap<String, Object> object = (HashMap) valueMap.get(keySplited[1]);
										tempMap.put(keySplited[2], value);

										for (Map.Entry<String, Object> entry : object.entrySet()) {
											tempMap.put(entry.getKey(), entry.getValue());
										}

										valueMap.put(keySplited[1], tempMap);
									} else {
										valueMap.put(keySplited[1], localMap);
									}
								} else {
									valueMap.put(keySplited[1], localMap);
								}
							}
						}
					}
					plist.addProperty(rootNode, valueMap);
				} 
			}
			plist.save(destPlistFile);
		} catch (ConfigurationException e) {
			throw new PhrescoException(e);
		}
	}

	private String getThirdPartyFolder(ApplicationInfo appInfo) throws PhrescoException { 
		File pomPath = new File(Utility.getProjectHome() + appInfo.getAppDirName() + File.separator + Constants.POM_NAME);
		try {
			PomProcessor processor = new PomProcessor(pomPath);
			String property = processor.getProperty(Constants.POM_PROP_KEY_MODULE_SOURCE_DIR);
			if(StringUtils.isNotEmpty(property)) {
				return property;
			}
		} catch (PhrescoPomException e) {
			throw new PhrescoException(e);
		}
		return "";
	}

	private List<Configuration> getConfigFromPlist(String plistPath) throws PhrescoException {
		List<Configuration> configs = new ArrayList<Configuration>();
		try {
			Configuration config = null;
			File plistFile = new File(plistPath);
			if (plistFile.isFile()) {
				config = new Configuration(plistFile.getName(), FEATURES);
			} else {
				return Collections.EMPTY_LIST;
			}

			// get all the key and value pairs
			Properties properties = new Properties();
			XMLPropertyListConfiguration conf = new XMLPropertyListConfiguration(plistPath);
			Iterator it = conf.getKeys();
			while (it.hasNext()) {
				String key = (String) it.next();
				Object property = conf.getProperty(key);
				String value = property.toString();
				properties.setProperty(key, value);
			}
			config.setProperties(properties);
			configs.add(config);
		} catch (ConfigurationException e) {
			throw new PhrescoException(e);
		}
		return configs;
	}

	@Override
	public void preBuild(ApplicationInfo appInfo) throws PhrescoException {
		Configuration configuration = new Configuration();
		Properties properties = new Properties();
		String baseDir = Utility.getProjectHome() + appInfo.getAppDirName();	
		File pluginInfoFile = new File(baseDir + File.separator + Constants.PACKAGE_INFO_FILE);
		MojoProcessor mojoProcessor = new MojoProcessor(pluginInfoFile);
		Parameter environmentParameter = mojoProcessor.getParameter(Constants.MVN_GOAL_PACKAGE, Constants.MOJO_KEY_ENVIRONMENT_NAME);
		String environmentValue = environmentParameter.getValue();
		Parameter themeParameter = mojoProcessor.getParameter(Constants.MVN_GOAL_PACKAGE, Constants.MOJO_KEY_THEME);
		if (themeParameter != null) {
			String themeValue = themeParameter.getValue();
			properties.put(Constants.MOJO_KEY_THEME, themeValue);
		}
		properties.put(Constants.MOJO_KEY_ENVIRONMENT_NAME, environmentValue);
		configuration.setProperties(properties);
		String plistFile = baseDir + File.separator + "source/info.plist";
		List<Configuration> asList = Arrays.asList(configuration);
		try {
			storeConfigObjAsPlist(asList, plistFile);
		} catch (Exception e) {
			throw new PhrescoException(e);
		}
	}

	@Override
	public List<Configuration> preConfiguration(ApplicationInfo appInfo, String featureName, String envName) throws PhrescoException {
		File plistFile = new File(Utility.getProjectHome() + appInfo.getAppDirName() + getThirdPartyFolder(appInfo) + File.separator + featureName + File.separator + featureName+RESOURCE +File.separator + PLIST);
		try {
			return getConfigFromPlist(plistFile.getPath());
		} catch (Exception e) {
			throw new PhrescoException(e);
		}
	}
}
