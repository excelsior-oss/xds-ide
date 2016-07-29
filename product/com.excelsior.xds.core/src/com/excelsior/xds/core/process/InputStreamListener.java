package com.excelsior.xds.core.process;

public interface InputStreamListener {
	void onHasData(byte[] buffer, int length);
	void onEndOfStreamReached();
}
