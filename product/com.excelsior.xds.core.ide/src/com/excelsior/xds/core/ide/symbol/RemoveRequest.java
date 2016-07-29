package com.excelsior.xds.core.ide.symbol;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;

import com.excelsior.xds.core.ide.symbol.SymbolModelManager.INotifier;
import com.excelsior.xds.core.ide.utils.ParsedModuleKeyUtils;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.symbol.binding.ModulaSymbolCache;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

class RemoveRequest extends AbstractRequest {
	private final IProject project;
	private final Collection<IFileStore> filesToRemove;
	
	public RemoveRequest(IProject project, Collection<IFileStore> filesToRemove, INotifier notifier) {
		super(notifier);
		this.project = project;
		this.filesToRemove = filesToRemove;
	}

	@Override
	public Iterable<ModificationStatus> apply() {
		if (project != null && !project.isOpen()) {
			return Arrays.asList();
		}
		return Iterables.transform(filesToRemove, new Function<IFileStore, ModificationStatus>() {
			@Override
			public ModificationStatus apply(IFileStore sourceFile) {
				ParsedModuleKey moduleKey = ParsedModuleKeyUtils.create(project, sourceFile);
				if (ModulaSymbolCache.instance().removeModule(moduleKey)) {
					notifier.notifyRemoved(moduleKey);
				}
				return new ModificationStatus(ModificationType.REMOVED, project, moduleKey);
			}
		});
	}

	@Override
	public void completed() {
	}
}
