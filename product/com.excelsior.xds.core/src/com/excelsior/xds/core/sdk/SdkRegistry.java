package com.excelsior.xds.core.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The container of all registered XDS development systems.
 */
public class SdkRegistry {
	
	private String defaultSdkName;
	private List<Sdk> registeredSDKs = new ArrayList<Sdk>();

	public SdkRegistry(String activeSdkName) {
		this.defaultSdkName = activeSdkName;
	}
	
	public String getDefaultSdkName() {
		return defaultSdkName;
	}

	public Sdk getDefaultSdk() {
		for (Sdk sdk : registeredSDKs) {
			if (sdk.getName().equals(defaultSdkName)) {
				return sdk;
			}
		}
		if (registeredSDKs.size() > 0) {
		    // if there is no active sdk now: mark 1st sdk (if any) as active and return it
		    Sdk sdk = registeredSDKs.get(0);
            defaultSdkName = sdk.getName();
            return sdk;
		}
		return null;
	}
	
	public Sdk findSdk(String name) {
		for (Sdk sdk : registeredSDKs) {
			if (sdk.getName().equals(name)) {
				return sdk;
			}
		}
		return null;
	}
	
	public void addSdk(Sdk sdk) {
		registeredSDKs.add(sdk);
		if (registeredSDKs.size() == 1) {
			defaultSdkName = sdk.getName();
		}
	}
	
	public void removeSdk(Sdk sdk) {
		registeredSDKs.remove(sdk);
		if (sdk.getName().equals(defaultSdkName) && !registeredSDKs.isEmpty()) { // removed is active sdk
			defaultSdkName = registeredSDKs.iterator().next().getName();
		}
	}
	
	public void editSdk(Sdk oldSdk, Sdk editedSdk) {
		if (oldSdk.getName().equals(defaultSdkName)) {
			defaultSdkName = editedSdk.getName();
		}

		// the same Sdk object must be preserved,
		// it may be cached anywhere so copyFrom it:
		
		oldSdk.copyFrom(editedSdk);
	}
	
	public void setDefaultSdk(String defaultSdkName) {
		this.defaultSdkName = defaultSdkName;
	}

	public List<Sdk> getRegisteredSDKs() {
		return Collections.unmodifiableList(registeredSDKs);
	}

	public void setRegisteredSDKs(List<Sdk> registeredSDKs) {
		this.registeredSDKs = registeredSDKs;
	}

	/**
	 * TODO : remove the fuck out
	 * @param aSdk
	 */
	public String makeSdkNameUnique(String name) {
		if (findSdk(name) == null) {
			return name;
		}
		for (int i = 1; true; ++i) {
			String nn = name + "(" + i +")"; //$NON-NLS-1$ //$NON-NLS-2$
			if (findSdk(nn) == null) {
				return nn;
			}
		}
	}
	
	/**
	 * TODO : remove the fuck out
	 * @param aSdk
	 */
	public void makeSdkNameUnique(Sdk[] aSdk) {
	    if (aSdk != null) {
	        for (Sdk sdk : aSdk) {
	        	// TODO : remove ASAP
	        	boolean beingEdited = sdk.isBeingEdited();
				if (!beingEdited){
					sdk.beginEdit();
	        	}
	            sdk.setName(makeSdkNameUnique(sdk.getName()));
	            // TODO : remove ASAP
	            if (!beingEdited){
					sdk.endEdit();
	        	}
	        }
	    }
	}
	
}