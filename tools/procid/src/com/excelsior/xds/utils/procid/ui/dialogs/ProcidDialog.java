package com.excelsior.xds.utils.procid.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ProcidDialog extends Dialog {

	private final String processId;

	public ProcidDialog(Shell parentShell, String processId) {
		super(parentShell);
		this.processId = processId;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		Label procidLabel = new Label(container, SWT.NONE);
		procidLabel.setText("Process ID:");
		Text procidText = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		procidText.setText(processId);
		
		Button copyClipboardButton = new Button(container, SWT.PUSH);
		copyClipboardButton.setText("Copy Process ID to the clipboard");
		copyClipboardButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyProcidToClipboard();
			}
		});
		
		return container;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	protected void copyProcidToClipboard() {
		Display display = Display.getCurrent();
        Clipboard clipboard = new Clipboard(display);
        clipboard.setContents(new Object[] { processId },
                new Transfer[] { TextTransfer.getInstance() });
        clipboard.dispose();
	}
}
