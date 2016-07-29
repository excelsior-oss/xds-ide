package com.excelsior.xds.core.ide.symbol;


public interface IModificationRequest {
	/**
	 * @return collection of affected source files
	 */
	Iterable<ModificationStatus> apply();
	void completed();
}