package com.excelsior.xds.core.utils;

import java.util.ArrayList;
import java.util.List;

public class CompositedDisposable implements IDisposable{
	private List<IDisposable> disposables = new ArrayList<IDisposable>();
	
	public CompositedDisposable(IDisposable... disposables) {
		for (IDisposable d : disposables) {
			this.disposables.add(d);
		}
	}

	@Override
	public void dispose() {
		disposables.stream().forEach(IDisposable::dispose);
	}
}