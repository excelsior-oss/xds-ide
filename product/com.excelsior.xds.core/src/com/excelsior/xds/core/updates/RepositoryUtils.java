package com.excelsior.xds.core.updates;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.framework.ServiceReference;

import com.excelsior.xds.core.XdsCorePlugin;
import com.excelsior.xds.core.log.LogHelper;

public final class RepositoryUtils {
	private RepositoryUtils() {
		super();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addUpdateSite(String url) {
		try {
			ServiceReference serviceReference = XdsCorePlugin.getContext().getServiceReference(IProvisioningAgentProvider.SERVICE_NAME);
			if (serviceReference == null)
				return;
			IProvisioningAgentProvider agentProvider = (IProvisioningAgentProvider) XdsCorePlugin
					.getContext().getService(serviceReference);
			if (agentProvider == null) {
				LogHelper.logError("Cannot get provisioning service"); //$NON-NLS-1$
				return;
			}
			// if null provided, the provisioning agent for the currently
			// running
			// system is returned, if available
			IProvisioningAgent provisioningAgent = agentProvider
					.createAgent(null);
			if (provisioningAgent == null) {
				LogHelper.logError("Cannot create provisioning agent"); //$NON-NLS-1$
				return;
			}

			// Load repository manager
			IMetadataRepositoryManager metaManager = (IMetadataRepositoryManager) provisioningAgent
					.getService(IMetadataRepositoryManager.SERVICE_NAME);
			if (metaManager == null) {
				LogHelper.logError("Cannot get service : " + IMetadataRepositoryManager.SERVICE_NAME); //$NON-NLS-1$
				return;
			}

			// Load artifact manager
			IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) provisioningAgent
					.getService(IArtifactRepositoryManager.SERVICE_NAME);
			if (artifactManager == null) {
				LogHelper.logError("Cannot get service : " + IArtifactRepositoryManager.SERVICE_NAME); //$NON-NLS-1$
				return;
			}

			// create a new repository
			URI repoUri = new URI(url);

			if (!metaManager.contains(repoUri)) {
				IMetadataRepository metaRepo = null;
				boolean loaded = false;
				try {
					metaRepo = metaManager.loadRepository(repoUri, IRepositoryManager.REPOSITORY_HINT_MODIFIABLE , null);
					loaded = true;
				} catch (ProvisionException e) {
					//expected - fall through and create the new repository
				}
				if (!loaded) {
					metaRepo = metaManager.createRepository(
							repoUri, "Metadata desc here", //$NON-NLS-1$
							IMetadataRepositoryManager.TYPE_COMPOSITE_REPOSITORY,
							null);
					metaManager.addRepository(metaRepo.getLocation());
				}
			}

			if (!artifactManager.contains(repoUri)) {
				IArtifactRepository aftifacts = null;
				boolean loaded = false;
				try {
					aftifacts = artifactManager.loadRepository(repoUri, IRepositoryManager.REPOSITORY_HINT_MODIFIABLE , null);
					loaded = true;
				} catch (ProvisionException e) {
					//expected - fall through and create the new repository
				}
				if (!loaded) {
					aftifacts = artifactManager
							.createRepository(
									repoUri,
									"Artifact desc here", //$NON-NLS-1$
									IArtifactRepositoryManager.TYPE_COMPOSITE_REPOSITORY,
									null);
					artifactManager.addRepository(aftifacts.getLocation());
				}
			}
		} catch (OperationCanceledException e) {
			LogHelper.logError(e);
		} catch (ProvisionException e) {
			LogHelper.logError(e);
		} catch (URISyntaxException e) {
			LogHelper.logError(e);
		}
	}
}
