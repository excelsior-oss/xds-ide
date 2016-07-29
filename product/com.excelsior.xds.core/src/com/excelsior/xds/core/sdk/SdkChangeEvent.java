package com.excelsior.xds.core.sdk;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.excelsior.xds.core.sdk.Sdk.Property;
import com.excelsior.xds.core.utils.collections.Pair;

public final class SdkChangeEvent extends SdkEvent {
	private final Map<Property, Pair<String, String>> property2OldAndNewVal = new HashMap<Sdk.Property, Pair<String,String>>();
	
	SdkChangeEvent(Sdk source) {
		super(source);
	}
	
	boolean hasChanges() {
		return !property2OldAndNewVal.isEmpty();
	}

	void setPropertyChanged(Property p, String oldValue, String newValue) {
		Pair<String, String> oldNewVal = property2OldAndNewVal.get(p);
		if (oldNewVal == null){
			oldNewVal = new Pair<String, String>(oldValue, newValue);
			property2OldAndNewVal.put(p, oldNewVal);
		}
		if (Objects.equals(oldNewVal.getFirst(), oldNewVal.getSecond())) {
			property2OldAndNewVal.remove(p);
			return;
		}
		oldNewVal.setSecond(newValue);
	}
	
	public Collection<Property> getChangedProperties() {
		return property2OldAndNewVal.keySet();
	}
	
	public boolean isChanged(Property p) {
		return property2OldAndNewVal.keySet().contains(p);
	}
	
	public String getOldValue(Property p) {
		Pair<String, String> oldNewVal = property2OldAndNewVal.get(p);
		return oldNewVal != null? oldNewVal.getFirst() : null;
	}
	
	public String getNewValue(Property p) {
		Pair<String, String> oldNewVal = property2OldAndNewVal.get(p);
		return oldNewVal != null? oldNewVal.getSecond() : null;
	}
}
