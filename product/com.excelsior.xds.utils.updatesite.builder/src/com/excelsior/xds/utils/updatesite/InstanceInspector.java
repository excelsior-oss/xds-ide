package com.excelsior.xds.utils.updatesite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InstanceInspector {
	public Map<String, BundleDescriptor> getBundleDescriptors(String instancePath) throws IOException {
		if (instancePath == null) return null;
		Map<String, BundleDescriptor>  bundleDescriptors = new HashMap<String, BundleDescriptor>() ;
		try(BufferedReader reader = new BufferedReader(new FileReader(instancePath + "/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info"))){
			String line = null;
			while( (line = reader.readLine()) != null ) {
				String[] parts = line.split("[,]");
				String id = parts[0];
				BundleDescriptor desc  = new BundleDescriptor(id);
				bundleDescriptors.put(id, desc);
			}
			return bundleDescriptors;
		}
	}
}
