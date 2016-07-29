package com.excelsior.xds.core.ide.symbol;

import java.util.ArrayList;

/**
 * Special request to stop processing
 * 
 * @author lsa80
 */
public final class PoisonRequest implements IModificationRequest {
	
	public final static PoisonRequest INSTANCE = new PoisonRequest();

	@Override
	public Iterable<ModificationStatus> apply() {
		return new ArrayList<ModificationStatus>();
	}

	@Override
	public void completed() {
	}
}
