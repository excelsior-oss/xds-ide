package com.excelsior.xds.core.ide.symbol;

import com.excelsior.xds.core.ide.symbol.SymbolModelManager.INotifier;

abstract class AbstractRequest implements IModificationRequest{
	protected final INotifier notifier;

	AbstractRequest(INotifier notifier) {
		this.notifier = notifier;
	}
}
