package com.excelsior.xds.core.marker;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;

/**
 * Marker descriptor - contains all information necessary to create the marker.
 * @author lsa80
 */
public class MarkerInfo {
	private Map<String, Object> attributes = new HashMap<String, Object>();
	
	private final String type;

	public MarkerInfo(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}
	
	/**
	 * @return
	 * @see {@link IMarker#CHAR_START}
	 */
	public Integer getCharStart() {
		return (Integer)attributes.get(IMarker.CHAR_START);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MarkerInfo other = (MarkerInfo) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
