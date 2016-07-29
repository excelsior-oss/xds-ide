package com.excelsior.xds.core.utils.time;

import java.util.concurrent.atomic.AtomicLong;

public class ModificationStamp 
{
	/**
	 * Time stamp value guaranteed to be oldest
	 */
	public final static ModificationStamp OLDEST = new ModificationStamp(true); 
	
	private final static AtomicLong globalModificationCount = new AtomicLong(0L);
	
	private final long modificationStampValue;

	public ModificationStamp() {
		this(false);
	}
	
	private ModificationStamp(boolean isOldest) {
		if (isOldest) {
			modificationStampValue = 0L;
		}
		else{
			modificationStampValue = globalModificationCount.incrementAndGet();
		}
	}
		
	public long getModificationStampValue() {
		return modificationStampValue;
	}

	public boolean isGreaterThan(ModificationStamp ms) {
        return this.modificationStampValue - ms.modificationStampValue > 0;
    }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ (int) (modificationStampValue ^ (modificationStampValue >>> 32));
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
		ModificationStamp other = (ModificationStamp) obj;
		if (modificationStampValue != other.modificationStampValue)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ""+modificationStampValue;
	}
	
	
}
