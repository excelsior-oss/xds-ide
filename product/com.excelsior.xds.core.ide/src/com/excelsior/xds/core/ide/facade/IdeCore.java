package com.excelsior.xds.core.ide.facade;

import static com.excelsior.xds.core.utils.Lambdas.nonnull;
import static com.excelsior.xds.core.utils.collections.CollectionsUtils.array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.core.ide.symbol.SymbolModelManager;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.marker.MarkerUtils;
import com.excelsior.xds.core.project.IXdsProjectSettingsListener;
import com.excelsior.xds.core.project.XdsProjectSettings;
import com.excelsior.xds.core.project.XdsProjectSettingsManager;
import com.excelsior.xds.core.sdk.ISdkListener;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.Sdk.Property;
import com.excelsior.xds.core.sdk.SdkChangeEvent;
import com.excelsior.xds.core.sdk.SdkEvent;
import com.excelsior.xds.core.sdk.SdkManager;
import com.excelsior.xds.core.sdk.SdkRemovedEvent;
import com.excelsior.xds.core.sdk.SdkUtils;
import com.excelsior.xds.core.utils.BuilderUtils;
import com.excelsior.xds.core.utils.BuilderUtils.BuildAction;
import com.excelsior.xds.core.utils.Lambdas;
import com.excelsior.xds.core.utils.collections.XMapUtils;

/**
 * <br>
 * Facade for operations requiring interactions between components - like changing SDK of the project. <br>
 * <br>
 * It requires actions like:  <br>
 *  1) Invalidating of the BuildSettingsCache<br>
 *  2) Invalidating of the ModulaSymbolCache<br>
 *  3) Rebuilding of the CompilationSet and refreshing of the UI<br>
 *  <br>
 * @author lsa80
 * <br>
 */
public final class IdeCore {
	private IdeCore(){
		SdkManager.getInstance().addListener(new SdkListener());
		XdsProjectSettingsManager.addListener(new XdsProjectSettingsListener());
	}
	
	private final class XdsProjectSettingsListener implements
			IXdsProjectSettingsListener {
		@Override
		public void projectSdkChanged(IProject project, Sdk oldSdk, Sdk currentSdk) {
			try {
				MarkerUtils.scheduleDeleteMarkers(project);
				BuilderUtils.invokeBuilder(project, EnumSet.of(
						BuildAction.REBUILD_LIBRARY_FILESET,
						BuildAction.GET_COMPILATION_SET), new NullProgressMonitor());
			} catch (CoreException e) {
				LogHelper.logError(e);
			}
		}
	}

	private static class SdkListener implements ISdkListener{
		private static final Map<Property, BuildAction> mapper = XMapUtils.newHashMap(
				array(Property.XDS_COMPILER, Property.XDS_LIB_DEFS_PATH, Property.XDS_PRIM_EXTENSIONS), 
				array(BuildAction.REBUILD_PROJECT, BuildAction.REBUILD_LIBRARY_FILESET, BuildAction.REFRESH_PROJECT));
		
		@Override
		public void sdkChanged(SdkChangeEvent e) {
			EnumSet<BuildAction> buildActions = EnumSet.noneOf(BuildAction.class);
			Consumer<IProject> sdkRenamedAction = createRenameAction(e, buildActions);
			if (sdkRenamedAction != null) {
				SdkUtils.getProjectsWithGivenSdkName(getSdkName(e)).forEach(sdkRenamedAction);
			}
			List<Consumer<IProject>> projectActions = new ArrayList<>();
			addProjectBuilderAction(e, buildActions, projectActions);
			
			executeProjectActions(e, projectActions);
		}

		/**
		 * @param e
		 * @return Name of the SDK if it is not default or null otherwise
		 */
		private static String getSdkName(SdkChangeEvent e) {
			return e.getSource().isDefault()? null : e.getOldValue(Property.XDS_NAME);
		}

		@Override
		public void sdkRemoved(SdkRemovedEvent e) {
			Consumer<IProject> action = p -> {
				XdsProjectSettings xdsProjectSettings = XdsProjectSettingsManager.getXdsProjectSettings(p);
				xdsProjectSettings.setProjectSdk(null); // use default SDK
				xdsProjectSettings.flush();
				createProjectBuilderAction(EnumSet.of(BuildAction.REBUILD_PROJECT)).accept(p);
			};
			executeProjectActions(e, Arrays.asList(action));
		}
		
		private void executeProjectActions(SdkEvent e,
				List<Consumer<IProject>> projectActions) {
			if (!projectActions.isEmpty()) {
				List<IProject> projectsWithGivenSdk = SdkUtils.getProjectsWithGivenSdk(e.getSource());
				projectActions.stream().forEachOrdered(a -> {
					projectsWithGivenSdk.forEach(a);
				});
			}
		}
		
		private Consumer<IProject> createRenameAction(SdkChangeEvent e, EnumSet<BuildAction> buildActions) {
			Sdk sdk = e.getSource();
			Consumer<IProject> sdkRenamedAction = null;
			if (e.isChanged(Property.XDS_NAME)) {
				sdkRenamedAction = new Consumer<IProject>() {
					@Override
					public void accept(IProject p) {
						XdsProjectSettings settings = XdsProjectSettingsManager.getXdsProjectSettings(p);
						if (Objects.equals(settings.getProjectSpecificSdkName(), getSdkName(e))) {
							settings.setProjectSdk(sdk);
							settings.flush();
							buildActions.add(BuildAction.GET_COMPILATION_SET);
							buildActions.add(BuildAction.REBUILD_LIBRARY_FILESET);
						}
					}
				};
			}
			return sdkRenamedAction;
		}
		
		private void addProjectBuilderAction(SdkChangeEvent e, EnumSet<BuildAction> buildActions, List<Consumer<IProject>> projectActions) {
			e.getChangedProperties().stream().map(Lambdas.func(mapper)).filter(nonnull()).forEach(buildActions::add);
			if (!buildActions.isEmpty()) {
				projectActions.add(createProjectBuilderAction(buildActions));
			}
		}

		private Consumer<IProject> createProjectBuilderAction(
				EnumSet<BuildAction> buildActions) {
			return p -> {
				try {
					if (buildActions.contains(BuildAction.REBUILD_PROJECT)) {
						BuildSettingsCache.invalidateBuildSettingsCache(p);
						SymbolModelManager.instance().scheduleRemove(p);
						MarkerUtils.scheduleDeleteMarkers(p);
					}
					BuilderUtils.invokeBuilder(p, buildActions, new NullProgressMonitor());
				} catch (CoreException e1) {
					LogHelper.logError(e1);
				}
			};
		}
	}
	
	public static IdeCore instance() {
		return IdeCoreHolder.INSTANCE;
	}
	
	private static final class IdeCoreHolder{
		static IdeCore INSTANCE = new IdeCore();
	}
}
