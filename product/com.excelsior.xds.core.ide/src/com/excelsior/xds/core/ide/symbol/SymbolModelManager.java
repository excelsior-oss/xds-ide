package com.excelsior.xds.core.ide.symbol;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.ide.utils.ParsedModuleKeyUtils;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.project.ProjectUtils;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.resource.XdsResourceChangeListener;
import com.excelsior.xds.core.resource.XdsSourceIFilePredicate;
import com.excelsior.xds.core.utils.Lambdas;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;
import com.excelsior.xds.core.utils.collections.Pair;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.XdsParserManager;
import com.excelsior.xds.parser.modula.XdsStandardNames;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.binding.ModulaSymbolCache;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public final class SymbolModelManager extends XdsResourceChangeListener{
	public static final boolean IS_DEBUG_MODEL_MODIFICATIONS = false;
	/**
	 * Main queue through which modification requests are dispatched. 
	 */
	private final LinkedBlockingQueue<IModificationRequest> modelModificationQueue = new LinkedBlockingQueue<IModificationRequest>();
	
	private final Map<ParsedModuleKey, Set<ISymbolModelListener>> file2symbolModelListeners = new ConcurrentHashMap<ParsedModuleKey, Set<ISymbolModelListener>>();
	private final Set<ISymbolModelListener> modelUpToDateListeners = CollectionsUtils.newConcurentHashSet();
	
	private volatile boolean stop = false;
	
	private final ReentrantLock notificationLock = new ReentrantLock();
	
	private final Notifier notifier = new Notifier();
	
	private SymbolModelManager(){
	}
	
	/**
	 * Only takes first symbol produced by the {@link #syncParseSymbols(IParseTask, boolean)}. Useful when parseTask contains only one file to parse.
	 */
	public IModuleSymbol syncParseFirstSymbol(IParseTask parseTask) {
		Collection<Pair<IModuleSymbol, ModulaAst>> result = syncParse(parseTask);
		if (result == null) {
			return null;
		}
		
		Collection<IModuleSymbol> symbols = CollectionsUtils.bindFirst(result);
		if (symbols != null && !symbols.isEmpty()) {
			return symbols.iterator().next();
		}
		return null;
	}
	
	/**
	 * Only takes first symbol produced by the {@link #syncParseSymbols(IParseTask, boolean)}. Useful when parseTask contains only one file to parse.
	 */
	public ModulaAst syncParseFirstAst(IParseTask parseTask) {
		Collection<Pair<IModuleSymbol, ModulaAst>> result = syncParse(parseTask);
		if (result == null) {
			return null;
		}
		
		Collection<ModulaAst> symbols = CollectionsUtils.bindSecond(result);
		if (symbols != null && !symbols.isEmpty()) {
			return symbols.iterator().next();
		}
		return null;
	}
	
	/**
	 * Blocks calling thread until results are available.
	 * 
	 * Warning! Deadlock is possible, be really sure that you need this method. Try use async scheduling instead.
	 * 
	 */
	public Collection<IModuleSymbol> syncParseSymbols(IParseTask parseTask) {
		Collection<Pair<IModuleSymbol, ModulaAst>> result = syncParse(parseTask);
		if (result == null) {
			return null;
		}
		
		return CollectionsUtils.bindFirst(result);
	}
	
	/**
	 * Blocks calling thread until results are available.
	 * 
	 * Warning! Deadlock is possible, be really sure that you need this method. Try use async scheduling instead.
	 * 
	 * @return
	 */
	public Collection<Pair<IModuleSymbol, ModulaAst>> syncParse(IParseTask parseTask) {
		final boolean[] isReady = {false};
		
		Iterable<ParsedModuleKey> moduleKeys = ParsedModuleKeyUtils.transform(parseTask.buildSettings(), parseTask.files());
		
		class SymbolModelListener extends SymbolModelListenerAdapter{
			private Set<ParsedModuleKey> modules2Parse = new HashSet<ParsedModuleKey>();
			final Collection<Pair<IModuleSymbol, ModulaAst>> parseResults = new ArrayList<Pair<IModuleSymbol,ModulaAst>>();
			
			public SymbolModelListener(Iterable<ParsedModuleKey> targetModuleKeys) {
				super(targetModuleKeys);
				Iterables.addAll(modules2Parse, targetModuleKeys);
			}
			
			@Override
			public void parsed(ParsedModuleKey key, IModuleSymbol moduleSymbol, ModulaAst ast) {
				synchronized(this){
					boolean contained = modules2Parse.remove(key);
					if (contained) {
						parseResults.add(Pair.create(moduleSymbol, ast));
					}
					doNotify();
				}
			}
			
			@Override
			public void removed(ParsedModuleKey key) {
				synchronized(this){
					modules2Parse.remove(key);
					doNotify();
				}
			}

			@Override
			public void error(Throwable e) {
				LogHelper.logError(e);
				synchronized(this){
					modules2Parse.clear();
					doNotify();
				}
			}

			void doNotify() {
				if (modules2Parse.isEmpty()) {
					isReady[0] = true;
					this.notify();
				}
			}
		}
		SymbolModelListener listener = new SymbolModelListener(moduleKeys);
		scheduleParse(parseTask, listener);
		try {
			synchronized(listener) {
				while (!isReady[0]) {
					listener.wait();
				}
			}
		} catch (InterruptedException e) {
			LogHelper.logError(e);
		}
		finally{
			removeListener(listener);
		}
		return listener.parseResults;
	}
	
	/**
	 * Blocks calling thread until the moment {@link SymbolModelManager#modelModificationQueue} is empty. If it is already empty - returns immediately. 
	 */
	public void waitUntilModelIsUpToDate() {
		class SymbolModelListener extends SymbolModelListenerAdapter{
			volatile boolean isUpToDate = false;
			public SymbolModelListener() {
				super((Iterable<ParsedModuleKey>)null);
			}
			
			@Override
			public void modelUpToDate() {
				synchronized (this) {
					isUpToDate = true;
					notify();
				}
			}

			@Override
			public boolean isInterestedInModelUpToDateEvent() {
				return true;
			}
		};
		SymbolModelListener listener = new SymbolModelListener();
		addListener(listener, false, false);
		try{
			synchronized (listener) {
				try {
					while (!listener.isUpToDate) {
						listener.wait();
					}
				} catch (InterruptedException e) {
				}
			}
		}
		finally{
			removeListener(listener);
		}
	}
	
	/**
	 * Schedules parse job and registers an optional listener, which will be notified on parse completion. <br>
	 * 
	 * @param parseTask - description of parse job to perform.
	 * @param listener - if not null, already existing parsed modules of interest will be reported
	 */
	public void scheduleParse(IParseTask parseTask, ISymbolModelListener listener) {
		addListener(listener, listener != null, parseTask.isNeedModulaAst());
		
		scheduleModification(new ParseRequest(parseTask, notifier));
	}

	/**
	 * Removes parsed modules of the {@link IFile}`s from the {@link ModulaSymbolCache}<br>
	 * Assumes that all {@link IFile}`s are from the same {@link IProject}.
	 * @param ifiles
	 */
	private void scheduleRemove(Collection<IFile> ifiles) {
		if (ifiles.isEmpty()) {
			return;
		}
		
		IFile firstIFile = ifiles.iterator().next();
		IProject project = firstIFile.getProject();
		
		Collection<IFileStore> files = ResourceUtils.convertIFilesToFileStores(ifiles);
		scheduleModification(new RemoveRequest(project, files, notifier));
	}
	
	/**
	 * Removes all parsed module of the project, if any
	 */
	public void scheduleRemove(IProject p) {
		if (!ProjectUtils.isXdsProject(p)) {
			return;
		}
		List<IFile> projectIFiles = ResourceUtils.getProjectResources(p, new XdsSourceIFilePredicate());
		scheduleRemove(projectIFiles);
	}
	
	/**
	 * Removes symbols and re-parses file to create the new symbols
	 */
	public void scheduleRemoveReparse(Collection<IFile> ifiles) {
		if (ifiles.isEmpty()) {
			return;
		}
		
		synchronized (modelModificationQueue) { // to introduce tasks in atomic manner
			final Map<IProject, List<IFile>> project2IFiles = groupByProject(ifiles);
			scheduleRemove(project2IFiles);
			scheduleParse(project2IFiles);
		}
	}
	
	/**
	 * @param listener
	 * @param isReportOnExistingModule - event will be raised if module of interest is already compiled
	 */
	public void addListener(ISymbolModelListener listener, boolean isReportOnExistingModule, boolean isNeedModulaAst) {
		if (listener == null) {
			return;
		}
		
		if (listener.isInterestedInModelUpToDateEvent()) {
			modelUpToDateListeners.add(listener);
			notifyModelUpToDate(listener);
		}
		
		if (!Iterables.isEmpty(listener.getModulesOfInterest())) {
			Iterable<ParsedModuleKey> keys = listener.getModulesOfInterest();
			for (ParsedModuleKey key : keys) {
				Set<ISymbolModelListener> listeners = file2symbolModelListeners.get(key);
				if (listeners == null) {
					listeners = CollectionsUtils.newConcurentHashSet();
					file2symbolModelListeners.put(key, listeners);
				}
				listeners.add(listener);
			}
			
			if (isReportOnExistingModule) {
				for (ParsedModuleKey key : listener.getModulesOfInterest()) {
					notifyParsed(listener, key, isNeedModulaAst);
				}
			}
		}
	}
	
	public void removeListener(ISymbolModelListener listener) {
		if (listener == null) {
			return;
		}
		Iterable<ParsedModuleKey> keys = listener.getModulesOfInterest();
		for (ParsedModuleKey key : keys) {
			Set<ISymbolModelListener> listeners = file2symbolModelListeners.get(key);
			listeners.remove(listener);
		}
		
		modelUpToDateListeners.remove(listener);
	}
	
	public void startup(){
		 new Thread(new ModelUpdater(), "Symbol Model Manager").start(); //$NON-NLS-1$
	}
	
	public void shutdown(){
		stop = true;
		modelModificationQueue.add(PoisonRequest.INSTANCE); // poison
	}
	
	public static SymbolModelManager instance() {
		return SymbolModelManagerHolder.INSTANCE;
	}
	
	protected void notifyParsed(ISymbolModelListener listener,
			ParsedModuleKey key, boolean isNeedModulaAst) {
		IModuleSymbol moduleSymbol = ModulaSymbolCache.instance().getModuleSymbol(key);
		ModulaAst ast = null;
		if (isNeedModulaAst) {
			ast = XdsParserManager.getModulaAst(key);
		}

		if (moduleSymbol != null || ast != null) {
			try{
				notificationLock.lock();
				try {
					listener.parsed(key, moduleSymbol, ast);
				}
				catch(RuntimeException e) {
					LogHelper.logError(e);
				}
			}
			finally{
				notificationLock.unlock();
			}
		}
	}
	
	protected void notifyRemoved(ISymbolModelListener listener, ParsedModuleKey parsedModuleKey) {
		try{
			notificationLock.lock();
			try {
				listener.removed(parsedModuleKey);
			}
			catch(RuntimeException e) {
				LogHelper.logError(e);
			}
		}
		finally{
			notificationLock.unlock();
		}
	}
	
	protected void notifyParsed(ParsedModuleKey key, boolean isNeedModulaAst) {
		Set<ISymbolModelListener> listeners = getSymbolModelListeners(key);
		for (ISymbolModelListener l : listeners) {
			notifyParsed(l, key, isNeedModulaAst);
			
		}
	}
	
	protected void notifyRemoved(ParsedModuleKey key) {
		Set<ISymbolModelListener> listeners = getSymbolModelListeners(key);
		for (ISymbolModelListener l : listeners) {
			notifyRemoved(l, key);
			
		}
	}
	
	protected void notifyModelUpToDate(ISymbolModelListener listener) {
		synchronized (modelModificationQueue) { // use synchronized here because condition isEmpty() is checked here 
			if (modelModificationQueue.isEmpty()) {
				if (listener != null) {
					try {
						listener.modelUpToDate();
					}
					catch(RuntimeException e) {
						listener.error(e);
						throw e;
					}
				}
				else {
					for (ISymbolModelListener l : modelUpToDateListeners) {
						l.modelUpToDate();
					}
				}
			}
		}
	}
	
	protected Set<ISymbolModelListener> getSymbolModelListeners(ParsedModuleKey key) {
		Set<ISymbolModelListener> listeners = file2symbolModelListeners.get(key);
		if (listeners == null) {
			listeners = Collections.emptySet();
		}
		return listeners;
	}
	
	private static class SymbolModelManagerHolder{
		public static SymbolModelManager INSTANCE = new SymbolModelManager();
	}
	
	private final class ModelUpdater implements Runnable {
		@Override
		public void run() {
			while(!stop) {
				try {
					notifyModelUpToDate(null);
					IModificationRequest req = modelModificationQueue.take();
					if (req == PoisonRequest.INSTANCE) { // poison - stop processing
						return;
					}
					Iterable<ModificationStatus> statuses = req.apply();
					for (ModificationStatus status : statuses) {
						req.completed();
						
						if (status.getModificationType() == ModificationType.PARSED) {
							ParseRequest parseReq = (ParseRequest) req;
							if (parseReq.getParseTask().isParseDualModule()) {
								scheduleParseDualModule(parseReq, status);
							}
							if (parseReq.getParseTask().isParseImportSection()) {
								scheduleResolveImportSection(parseReq, status);
							}
						}
						
					}
				}
				catch(InterruptedException e) {
				}
				catch (Throwable e) { // brutally catch all errors, since nothing should stop SymbolModelManager from processing, especially bugs in listeners.
					LogHelper.logError(e);
				}
			}
		}
		
		private void scheduleParseDualModule(ParseRequest parseReq, ModificationStatus status) {
			if (!parseReq.getParseTask().isParseDualModule()) {
				return;
			}
			ParsedModuleKey key = status.getKey();
			IFileStore sourceFile = key.moduleFile;
			if (XdsFileUtils.isDefinitionModuleFile(sourceFile.fetchInfo().getName())) {
				ParseTask task = new ParseTask(parseReq.getParseTask());
				
				task.setNeedModulaAst(false);
				task.setParseDualModule(false);
				task.setForce(true);
				task.setParseImportSection(false);
				
				String moduleName = FilenameUtils.getBaseName(sourceFile.fetchInfo().getName());
		        if (XdsStandardNames.STANDART_MODULE_SET.contains(moduleName)) {
		            return;
		        }
		        
		        IFileStore moduleFile     = null;
		        String moduleFileName = XdsFileUtils.getProgramModuleFileName(moduleName);
		        if (parseReq.getParseTask().buildSettings() != null) {
		            moduleFile = ResourceUtils.toFileStore(parseReq.getParseTask().buildSettings().lookup(moduleFileName));
		        }
		        else {
		            IFileStore parent = sourceFile.getParent();
					moduleFile = parent != null? parent.getChild(moduleFileName) : null;
		        }
		        
		        if (moduleFile != null && moduleFile.fetchInfo().exists()) {
		        	task.setFiles(Arrays.asList(moduleFile));
		        	
		        	ParseRequest parseDualReq = new ParseRequest(task, notifier);
		        	scheduleModification(parseDualReq);
		        }
	        }
		}
		
		private void scheduleResolveImportSection(ParseRequest parseReq, ModificationStatus status) {
			ParsedModuleKey key = status.getKey();
			final BuildSettings buildSettings = parseReq.getParseTask().buildSettings();
			
			IModuleSymbol hostModuleSymbol = ModulaSymbolCache.instance().getModuleSymbol(key);
	        if (hostModuleSymbol == null)
	            return;
	        
	        Set<IFileStore> importModuleFiles = new HashSet<IFileStore>();
	        
	        for (IModulaSymbol modulaSymbol : hostModuleSymbol.getImports()) {
	            IModuleSymbol importedModuleSymbol = null;
	            if (modulaSymbol instanceof IModuleSymbol) {
	                importedModuleSymbol = (IModuleSymbol)modulaSymbol;
	            }
	            else if (modulaSymbol instanceof IModuleAliasSymbol) {
	                importedModuleSymbol = ((IModuleAliasSymbol)modulaSymbol).getReference();
	            }
	            else {
	                importedModuleSymbol = ModulaSymbolUtils.getParentModule(modulaSymbol);
	            }
	            
	            if (importedModuleSymbol != null) {
	                String moduleFileName = importedModuleSymbol.getName() + "." 
	                                      + XdsFileUtils.MODULA_PROGRAM_MODULE_FILE_EXTENSION;
	                File moduleFile = buildSettings.lookup(moduleFileName);  
	                if (moduleFile != null && moduleFile.exists()) {
	                	moduleFile = ResourceUtils.getAbsolutePathAsInFS(moduleFile);
	                	importModuleFiles.add(ResourceUtils.toFileStore(moduleFile));
	                }
	            }
	            
	        }
	        
	        ParseTask task = new ParseTask(parseReq.getParseTask());
			
			task.setParseDualModule(false);
			task.setForce(false);
			task.setParseImportSection(false);
			task.setFiles(importModuleFiles);
			task.setNeedModulaAst(false);
			
			ParseRequest parseImportReq = new ParseRequest(task, notifier);
			scheduleModification(parseImportReq);
		}
	}
	
	@Override
	protected void handleProjectRemoved(IResourceDelta delta,
			IProject affectedProject, boolean isPreDeleteEvent) {
		try {
			affectedProject.accept(new IResourceVisitor() {
				@Override
				public boolean visit(IResource r) throws CoreException {
					if (isCompilationUnitFile(r)) {
						SymbolModelManager.instance().scheduleRemove(Arrays.asList((IFile)r));
					}
					return true;
				}
			});
		} catch (CoreException e) {
			LogHelper.logError(e);
		}
		super.handleProjectRemoved(delta, affectedProject, isPreDeleteEvent);
	}
	
	@Override
	protected VisitResult doVisit(IResourceChangeEvent event) {
		VisitResult result = super.doVisit(event);
		
		List<IFile> removedCompUnits = getCompilationUnits(result.removedResourceDeltas, null);
		scheduleRemove(groupByProject(removedCompUnits));
		
		List<IFile> addedCompUnits = getCompilationUnits(result.addedResourceDeltas, null);
		scheduleParse(groupByProject(addedCompUnits));
		
		List<IResourceDelta> changedResourceDeltas = result.changedResourceDeltas;
		List<IFile> ifiles = getCompilationUnits(changedResourceDeltas, rd -> isContentChanged(rd));
		scheduleRemoveReparse(ifiles); // bulk-update the model to overcome the dependency issues between the modules.
		return result;
	}
	
	private void scheduleParse(Map<IProject, List<IFile>> project2IFiles) {
		List<ParseTask> tasks = createParseTasks(project2IFiles);
		tasks.stream().filter(Lambdas.nonnull())
				.forEach(t -> scheduleParse(t, null));
	}
	
	private void scheduleRemove(Map<IProject, List<IFile>> project2IFiles) {
		project2IFiles.entrySet().stream().forEach(e -> scheduleRemove(e.getValue()));
	}
	
	private static List<ParseTask> createParseTasks(Map<IProject, List<IFile>> project2IFiles) {
		// for each IFile`s list (grouped by IProject), convert it to the ParseTask
		return project2IFiles.entrySet().stream()
				.map(e -> ParseTaskFactory.createWorkspaceTask(e.getValue()))
				.collect(Collectors.toList());
	}

	/**
	 * groups {@link IFile} by their {@link IProject}.
	 * @param compUnits
	 * @return
	 */
	private static Map<IProject, List<IFile>> groupByProject(
			Collection<IFile> compUnits) {
		Map<IProject, List<IFile>> project2IFiles = compUnits.stream()
				.collect(
						Collectors.groupingBy(f -> f.getProject(),
								Collectors.toList()));
		return project2IFiles;
	}

	private List<IFile> getCompilationUnits(List<IResourceDelta> resourceDeltas, Predicate<IResourceDelta> pred) {
		List<IFile> compUnits = new ArrayList<IFile>();
		for (IResourceDelta rd : resourceDeltas) {
			if (rd == null) continue;
			IResource r = rd.getResource();
			if (isCompilationUnitFile(r)) {
				IFile f = (IFile)r;
				if (pred != null && !pred.apply(rd)) {
					continue;
				}
				compUnits.add(f);
			}
		}
		return compUnits;
	}
	
	private void scheduleModification(IModificationRequest modification) {
		synchronized (modelModificationQueue) { // use synchronized here because condition isEmpty() can be checked in other methods (also from other methods)
			modelModificationQueue.add(modification);
		}
	}

	private class Notifier implements INotifier {
		/* (non-Javadoc)
		 * @see com.excelsior.xds.builder.symbol.INotifier#notifyParsed(com.excelsior.xds.builder.symbol.ISymbolModelListener, com.excelsior.xds.parser.commons.symbol.ParsedModuleKey, boolean)
		 */
		@Override
		public void notifyParsed(ParsedModuleKey key, boolean isNeedModulaAst) {
			SymbolModelManager.this.notifyParsed(key, isNeedModulaAst);
		}
		
		/* (non-Javadoc)
		 * @see com.excelsior.xds.builder.symbol.INotifier#notifyRemoved(com.excelsior.xds.builder.symbol.ISymbolModelListener, com.excelsior.xds.parser.commons.symbol.ParsedModuleKey)
		 */
		@Override
		public void notifyRemoved(ParsedModuleKey parsedModuleKey) {
			SymbolModelManager.this.notifyRemoved(parsedModuleKey);
		}
	}
	
	interface INotifier {
		void notifyParsed(ParsedModuleKey key, boolean isNeedModulaAst);
		void notifyRemoved(ParsedModuleKey parsedModuleKey);
	}
}
