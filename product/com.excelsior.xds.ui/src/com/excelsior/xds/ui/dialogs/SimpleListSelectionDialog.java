package com.excelsior.xds.ui.dialogs;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.excelsior.xds.ui.commons.utils.SwtUtils;

public class SimpleListSelectionDialog {

	public interface ITextProvider {
		public String getText(Object o);
	}

	/**
	 * 
	 * @param shell - shell or null to get default
	 * @param title
	 * @param message
	 * @param image - image 16x16 to show in items
	 * @param iTextProvider - items textualizator
	 * @param items - items array
	 * @param initialSelection - item to select or null
	 * @return index of (1st) selected item or -1
	 */
	public static int Selection(
			Shell shell, 
			String title, 
			String message, 
			Image image,
			ITextProvider iTextProvider,
			Object[] items, 
			Object initialSelection) 
	{
		SimpleListSelectionDialog lsd = new SimpleListSelectionDialog(shell, title, message, image, iTextProvider, items, initialSelection);
		int res = -1;
		if (lsd.dialog.open() == Window.OK) {			
			Object sel = lsd.dialog.getFirstResult();
			for (int i=0; i<items.length; ++i) {
				if (items[i] == sel) {
					res = i;
					break;
				}
			}
		}		
		return res;
	}
	
	private ElementListSelectionDialog dialog;
	private Image image;
	private ITextProvider iTextProvider;

	private SimpleListSelectionDialog(
			Shell shell, 
			String title, 
			String message, 
			Image image,
			ITextProvider iTextProvider,
			Object[] items, 
			Object initialSelection) 
	{
		this.image = image;
		this.iTextProvider = iTextProvider;
		if (shell == null) {
			shell = SwtUtils.getDefaultShell();
		}
		dialog= new ElementListSelectionDialog(shell, new SimpleLabelProvider());
		dialog.setTitle(title); 
		dialog.setMessage(message);
		dialog.setElements(items);
		if (initialSelection != null) {
			dialog.setInitialSelections(new Object[] { initialSelection });
		}
	}
	
	
	private class SimpleLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			return image;
		}
		
		public String getText(Object element) {
			return iTextProvider.getText(element);
		}
	}

}
