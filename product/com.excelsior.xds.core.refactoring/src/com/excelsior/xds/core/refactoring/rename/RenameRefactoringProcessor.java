package com.excelsior.xds.core.refactoring.rename;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.IConditionChecker;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceDescriptor;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.ide.symbol.SymbolModelManager;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.refactoring.CoreRefactoringPlugin;
import com.excelsior.xds.core.refactoring.rename.internal.nls.Messages;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.search.modula.ModulaSearchInput;
import com.excelsior.xds.core.search.modula.ModulaSearchOperation;
import com.excelsior.xds.core.search.modula.ModulaSearchOperation.ISearchResultCollector;
import com.excelsior.xds.core.search.modula.ModulaSymbolMatch;
import com.excelsior.xds.core.search.modula.utils.ModulaSearchUtils;
import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.utils.BuilderUtils;
import com.excelsior.xds.core.utils.JavaUtils;
import com.excelsior.xds.parser.commons.symbol.IBlockSymbolTextBinding;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;

public class RenameRefactoringProcessor extends RefactoringProcessor {
	private final RenameRefactoringInfo renameRefactoringInfo;
	private Map<IFile, Set<ModulaSymbolMatch>> ifile2SymbolMatches = new HashMap<IFile, Set<ModulaSymbolMatch>>();

	public RenameRefactoringProcessor(
			RenameRefactoringInfo renameRefactoringInfo) {
		this.renameRefactoringInfo = renameRefactoringInfo;
	}

	@Override
	public Object[] getElements() {
		return new Object[]{renameRefactoringInfo.getSymbolFromSelection().getName()};
	}

	@Override
	public String getIdentifier() {
		return getClass().getName();
	}

	@Override
	public String getProcessorName() {
		return Messages.RenameRefactoringProcessor_Name;
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return true;
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus result = new RefactoringStatus();
		IModulaSymbol symbol = renameRefactoringInfo.getSymbolFromSelection();
		if (symbol.isAttributeSet(SymbolAttribute.PERVASIVE) || symbol.isAttributeSet(SymbolAttribute.RECONSTRUCTED)) {
			result.addFatalError(Messages.RenameRefactoringProcessor_SymbolCannotBeRenamed);
		}
		return result;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor monitor,
			CheckConditionsContext context) throws CoreException,
			OperationCanceledException {
		RefactoringStatus refactoringStatus = findMatches(monitor, context);
		return refactoringStatus;
	}

	protected RefactoringStatus findMatches(IProgressMonitor monitor,
			CheckConditionsContext context) {
		ifile2SymbolMatches.clear();
		
		
		IModulaSymbol symbol = renameRefactoringInfo.getSymbolFromSelection();
		IFile activeIFile = renameRefactoringInfo.getActiveIFile();
		
		if (symbol.isAttributeSet(SymbolAttribute.ALREADY_DEFINED)) {
			if (symbol instanceof IBlockSymbolTextBinding) {
				IBlockSymbolTextBinding binding = (IBlockSymbolTextBinding) symbol;
				Collection<ITextRegion> nameTextRegions = binding.getNameTextRegions();
				for (ITextRegion textRegion : nameTextRegions) {
					putMatchIfAbsent(new ModulaSymbolMatch(activeIFile, symbol, new TextPosition(-1, -1, textRegion.getOffset())));
				}
			}
			else {
				putMatchIfAbsent(new ModulaSymbolMatch(activeIFile, symbol, symbol.getPosition()));
			}
		}
		else {
			SymbolModelManager.instance().waitUntilModelIsUpToDate();
			ModulaSearchInput m2SearchInput = new ModulaSearchInput(ResourceUtils.getProject(activeIFile));
			m2SearchInput.setLimitTo(ModulaSearchInput.LIMIT_TO_USAGES | ModulaSearchInput.LIMIT_TO_DECLARATIONS);
			m2SearchInput.setSearchFor(ModulaSearchInput.SEARCH_FOR_ANY_ELEMENT);
			m2SearchInput.setSearchInFlags(ModulaSearchInput.SEARCH_IN_ALL_SOURCES);
			m2SearchInput.setSearchModifiers(ModulaSearchInput.MODIFIER_ALL_NAME_OCCURENCES);
			IProject project = renameRefactoringInfo.getProject();
			m2SearchInput.setSearchScope(FileTextSearchScope.newSearchScope(new IResource[]{project}, new String[]{"*"}, false)); //$NON-NLS-1$
			m2SearchInput.setSearchFor(ModulaSearchUtils.getSearchForConstant(symbol));
			Set<String> symbolQualifiedNames = ModulaSearchUtils.getSymbolQualifiedNames(symbol);
			m2SearchInput.setSymbolQualifiedNames(symbolQualifiedNames);
			
			ISearchResultCollector searchResultCollector = new ISearchResultCollector() {
				@Override
				public void accept(Object objMatch) {
					if (objMatch instanceof ModulaSymbolMatch) {
						ModulaSymbolMatch match = (ModulaSymbolMatch)objMatch;
						putMatchIfAbsent(match);
					}
				}
			};
			ModulaSearchOperation op = new ModulaSearchOperation(m2SearchInput, searchResultCollector);
			MultiStatus status = new MultiStatus(CoreRefactoringPlugin.PLUGIN_ID, IStatus.OK, Messages.RenameRefactoringProcessor_ErrorWhileComputingLocationsToRename, null);
			op.execute(monitor, status);
			
			SymbolModelManager.instance().waitUntilModelIsUpToDate();
			
			if (ifile2SymbolMatches.keySet().size() > 0) {
				IConditionChecker checker = context.getChecker( ValidateEditChecker.class );
				ValidateEditChecker editChecker = ( ValidateEditChecker )checker;
				editChecker.addFiles( ifile2SymbolMatches.keySet().toArray(new IFile[0]) );
			}
			
			monitor.done();
		}
		
		return new RefactoringStatus();
	}
	
	private void putMatchIfAbsent(ModulaSymbolMatch match) {
		IFile matchIFile = match.getFile();
		try {
			if (!ResourceUtils.getFileSystem(matchIFile.getLocationURI()).canWrite()) {
				return;
			}
		} catch (CoreException e) {
			LogHelper.logError(e);
			return;
		}
		Set<ModulaSymbolMatch> matches = ifile2SymbolMatches.get(matchIFile);
		if (matches == null) {
			matches = new TreeSet<ModulaSymbolMatch>(new ModulaSymbolMatchPositionComparator());  
			ifile2SymbolMatches.put(matchIFile, matches);
		}
		matches.add(match);
	}

	@Override
	public Change createChange(IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		Change resultChange = null;
		try
		{
			if (!ifile2SymbolMatches.isEmpty()) {
				monitor.beginTask(Messages.RenameRefactoringProcessor_Creatingchanges, countMatches());
				
				IModuleSymbol definingModule = ModulaSymbolUtils.getHostModule(renameRefactoringInfo.getSymbolFromSelection());
				resultChange = new RenameCompositeChange( getProcessorName(), definingModule ); 
				
				RefactoringChangeContext refactoringChangeContext = new RefactoringChangeContext();
				for (Entry<IFile, Set<ModulaSymbolMatch>> entry : ifile2SymbolMatches.entrySet()) {
					IFile ifileWithOccurrence = entry.getKey();
					processIFile((RenameCompositeChange)resultChange, refactoringChangeContext, ifileWithOccurrence, entry.getValue(), monitor);
				}
			}
		}
		finally{
			monitor.done();
		}
		
		if (resultChange == null) {
			resultChange = new NullChange();
		}
		
		return resultChange;
	}

	/**
	 * Compares {@link ModulaSymbolMatch} only by file and position information, disregarding symbol, context line etc
	 * 
	 * @author lsa80
	 */
	private final class ModulaSymbolMatchPositionComparator implements
			Comparator<ModulaSymbolMatch> {
		@Override
		public int compare(ModulaSymbolMatch o1, ModulaSymbolMatch o2) {
			String absolutePath1 = ResourceUtils.getAbsolutePath(o1.getFile());
			String absolutePath2 = ResourceUtils.getAbsolutePath(o2.getFile());
			int result = JavaUtils.compare(absolutePath1, absolutePath2);
			if (result == 0) {
				result = JavaUtils.compare(o1.getOffset(), o2.getOffset());
				if (result == 0) {
					result = JavaUtils.compare(o1.getLength(), o2.getLength());
				}
			}
			return result;
		}
	}

	/**
	 * Internal class for storing information about refactorings to be performed.
	 * 
	 * @author lsa80
	 */
	private static class RefactoringChangeContext {
		private Map<Change, CompositeChange> change2Parent = new HashMap<Change, CompositeChange>();
		private Map<File, Change> file2RenameResourceChange = new HashMap<File, Change>();
		private Map<File, TextFileChange> file2TextFileChange = new HashMap<File, TextFileChange>();
		
		private Set<File> filesForDelete = new HashSet<File>();
		
		public boolean isRenameFileChangeRegistered(File file) {
			return file2RenameResourceChange.containsKey(file);
		}
		
		public void registerRenameFileChange(File file, CompositeChange parentChange, Change renameChange) {
			file2RenameResourceChange.put(file, renameChange);
			registerParent(parentChange, renameChange);
		}

		/**
		 * @return true if was successfully registered, false if it is overridden by the delete change
		 */
		public boolean registerTextFileChange(File file, CompositeChange parentChange, TextFileChange textFileChange) {
			if (!isFileWillBeDeleted(file)) {
				file2TextFileChange.put(file, textFileChange);
				registerParent(parentChange, textFileChange);
				return true;
			}
			
			return false;
		}
		
		public void registerFileDelete(File file) {
			filesForDelete.add(file);
			TextFileChange textFileChange = file2TextFileChange.remove(file);
			unregisterChangeAtParent(textFileChange);
		}
		
		private boolean isFileWillBeDeleted(File file) {
			return filesForDelete.contains(file);
		}
		
		private void unregisterChangeAtParent(Change change) {
			if (change != null) {
				CompositeChange parent = change2Parent.remove(change);
				parent.remove(change);
			}
		}
		
		private void registerParent(CompositeChange parentChange, Change change) {
			change2Parent.put(change, parentChange);
		}
	}
	
	private void processIFile(RenameCompositeChange resultChange,
			RefactoringChangeContext refactoringChangeContext,
			IFile ifileWithOccurrence,
			Set<ModulaSymbolMatch> fileMatches,
			IProgressMonitor monitor) {
		File fileWithOccurrence = ResourceUtils.getAbsoluteFile(ifileWithOccurrence);
		Change renameFileChange = null;
		
		resultChange.moduleFileDamaged(ifileWithOccurrence);
		resultChange.setProject(ifileWithOccurrence.getProject());
		
		boolean isRefactoringEnabled = isRefactoringEnabledByDefaultOn(ifileWithOccurrence);
		TextFileChange textFileChange = new TextFileChange( ifileWithOccurrence.getName(), ifileWithOccurrence );
		// a file change contains a tree of edits, first add the root of them
		MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
		textFileChange.setEdit( fileChangeRootEdit );
		String ext = FilenameUtils.getExtension(ifileWithOccurrence.getName());
		textFileChange.setTextType(ext);
		
		for (ModulaSymbolMatch symbolMatch : fileMatches) {
			IModulaSymbol symbol = symbolMatch.getSymbol();
			boolean isTopLevelModuleSymbol = symbol == ModulaSymbolUtils.getHostModule(symbol);
			// this automatically implies that TRUE(symbol instanceof IModuleSymbol) 
			if (isTopLevelModuleSymbol) {
				File sourceFile = ModulaSymbolUtils.getSourceFile(symbol);
				if (sourceFile != null && !refactoringChangeContext.isRenameFileChangeRegistered(sourceFile)) {
					boolean areFileSame = ResourceUtils.equalsPathesAsInFS(sourceFile, fileWithOccurrence);
					if (areFileSame) {
						String newModuleName = createNewModuleName(ifileWithOccurrence, renameRefactoringInfo.getNewName());
						renameFileChange = createRenameModuleFileChange(ifileWithOccurrence, newModuleName, refactoringChangeContext);
						if (renameFileChange != null) {
							renameFileChange.setEnabled(isRefactoringEnabled);
							refactoringChangeContext.registerRenameFileChange(fileWithOccurrence, resultChange, renameFileChange);
						}
					}
				}
			}
			ReplaceEdit edit = new ReplaceEdit( symbolMatch.getOffset(), 
					symbolMatch.getLength(), 
		            renameRefactoringInfo.getNewName() );
			fileChangeRootEdit.addChild(edit);
			
			monitor.worked(1);
		}
		
		if (refactoringChangeContext.registerTextFileChange(fileWithOccurrence, resultChange, textFileChange)) {
			textFileChange.setEnabled(isRefactoringEnabled);
			resultChange.add(textFileChange);
		}
		
		if (renameFileChange != null) {
			resultChange.add(renameFileChange);
		}
	}
	
	/**
	 * Whether refactoring should suggest modify resources only in compilation set or, otherwise,
	 * only not in compilation set 
	 * 
	 * @param ifileWithOccurrence - file on which refactoring will possibly operate 
	 * @return
	 */
	private boolean isRefactoringEnabledByDefaultOn(IFile ifileWithOccurrence) {
		IFile activeIFile = renameRefactoringInfo.getActiveIFile();
		boolean isActiveInCompilationSet = CompilationSetManager.getInstance().isInCompilationSet(activeIFile);
		boolean isFileWithOccurrenceInCompilationSet = CompilationSetManager.getInstance().isInCompilationSet(ifileWithOccurrence);
		return isActiveInCompilationSet == isFileWithOccurrenceInCompilationSet;
	}
	
	private static class RenameCompositeChange extends CompositeChange{
		private Set<IFile> damagedModuleFiles = new HashSet<IFile>();
		private IProject project;
		
		public RenameCompositeChange(String name, IModuleSymbol definingModule) {
			super(name);
		}

		public void setProject(IProject project) {
			this.project = project;
		}

		public void moduleFileDamaged(IFile moduleFile) {
			damagedModuleFiles.add(moduleFile);
		}
		
		public Change perform(IProgressMonitor pm) throws CoreException {
			super.perform(pm);
			SymbolModelManager.instance().scheduleRemoveReparse(damagedModuleFiles);

			BuilderUtils.invokeGetCompilationSet(project, pm);
			return null;
		}
	}
	
	private String createNewModuleName(IFile moduleIFile, String newName) {
		String modulePath = ResourceUtils.getAbsolutePath(moduleIFile);
		String moduleFileExt = FilenameUtils.getExtension(modulePath);
		StringBuilder sb = new StringBuilder(newName);
		return sb.append('.').append(moduleFileExt).toString();
	}
	
	private Change createRenameModuleFileChange(IFile ifile, String newName, RefactoringChangeContext refactoringChangeContext) {
		IFile renamedIFile = getRenamedAbsoluteFile(ifile, newName);
		if (ResourceUtils.equals(renamedIFile, ifile)) {
			return null;
		}
		
		final RenameResourceDescriptor descriptor= (RenameResourceDescriptor) RefactoringCore.getRefactoringContribution(RenameResourceDescriptor.ID).createDescriptor();
		descriptor.setProject(ifile.getProject().getName());
		descriptor.setDescription(Messages.RenameRefactoringProcessor_RenameModule);
		descriptor.setComment(""); //$NON-NLS-1$
		descriptor.setFlags(RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);
		descriptor.setResourcePath(ifile.getFullPath());
		descriptor.setNewName(newName);
		descriptor.setUpdateReferences(true);
		
		IPath conflictingResourcePath = null;
		if (renamedIFile.exists()) {
			conflictingResourcePath = renamedIFile.getFullPath();
			refactoringChangeContext.registerFileDelete(ResourceUtils.getAbsoluteFile(renamedIFile));
		}
		XdsRenameResourceChange resourceChange = new XdsRenameResourceChange(ifile.getFullPath(), conflictingResourcePath, newName);
		resourceChange.setDescriptor(new RefactoringChangeDescriptor(descriptor));
		
		return resourceChange;
	}
	
	private IFile getRenamedAbsoluteFile(IFile iFile, String newName) {
		IContainer container = iFile.getParent();
		IFile renamedIFile = container.getFile(new Path(newName));
		return renamedIFile;
	}
	
	private int countMatches() {
		int count = 0;
		
		for (Entry<IFile, Set<ModulaSymbolMatch>> entry : ifile2SymbolMatches.entrySet()) {
			count += entry.getValue().size();
		}
		
		return count;
	}
	
	@Override
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status,
			SharableParticipants sharedParticipants) throws CoreException {
		return new RefactoringParticipant[0];
	}
}
