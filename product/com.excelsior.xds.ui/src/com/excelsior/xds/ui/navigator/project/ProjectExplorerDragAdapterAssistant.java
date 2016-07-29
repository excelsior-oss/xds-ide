package com.excelsior.xds.ui.navigator.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;

import com.excelsior.xds.core.model.IXdsResource;
import com.excelsior.xds.core.resource.ResourceUtils;

/**
 * 
 * Customizes the drag part of the drag-n-drop operation.<br><br><br>
 * 
 * For some unknown reason, only {@link #dragStart(DragSourceEvent, IStructuredSelection)} methods is called.<br>
 * <br>
 * <br>
 * @author lsa80
 */
public class ProjectExplorerDragAdapterAssistant extends CommonDragAdapterAssistant {
	private static final Transfer[] TRANSFERS = new Transfer[] {
		LocalSelectionTransfer.getTransfer(),
		FileTransfer.getInstance()
	};

	public ProjectExplorerDragAdapterAssistant() {
	}
	
	@Override
	public void dragStart(DragSourceEvent anEvent,
			IStructuredSelection aSelection) {
		anEvent.doit = true;
	}
	
	/*
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#getSupportedTransferTypes()
	 */
	@Override
	public Transfer[] getSupportedTransferTypes() {
		return TRANSFERS;
	}

	@Override
	public boolean setDragData(DragSourceEvent event,
			IStructuredSelection selection) {
		if (selection != null) {
			if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
				boolean applicable= false;
				for (Iterator<?> iter= (selection).iterator(); iter.hasNext();) {
					Object element= iter.next();
					if (element instanceof IXdsResource) {
						applicable= true;
					}
				}
				if (applicable) {
					event.data = selection;
					return true;
				}
			} else if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
				List<String> files= new ArrayList<String>();
				for (Iterator<?> iter= (selection).iterator(); iter.hasNext();) {
					Object element= iter.next();
					if (element instanceof IXdsResource) {
						IXdsResource xr= (IXdsResource) element;
						IResource resource = xr.getResource();
						if (resource != null) {
							files.add(ResourceUtils.getAbsolutePath(resource));
						}
					}
				}
				if (!files.isEmpty()) {
					event.data = files.toArray(new String[files.size()]);
					return true;
				}
			}
		}
		return false;
	}
}
