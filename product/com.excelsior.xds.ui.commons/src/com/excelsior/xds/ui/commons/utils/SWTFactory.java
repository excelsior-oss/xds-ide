package com.excelsior.xds.ui.commons.utils;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.ui.commons.controls.Separator;

/**
 * Factory class to create some SWT resources. 
 */
public class SWTFactory {
	
	private static String lastBrowsePath = "C:/"; //$NON-NLS-1$


	// Browse for directory starting at the default location,
	// changes default location on Ok
	public static String browseDirectory(Shell shell, String text, String message) {
		return browseDirectory(shell, text, message, null);
	}

	// Browse for directory starting near the given location (or default if it is null or can't be found)
	// changes default location on Ok
	public static String browseDirectory(Shell shell, String text, String message, String preferredPath) {
		DirectoryDialog fd = new DirectoryDialog(shell);
		fd.setText(text);
        fd.setMessage(message);
        fd.setFilterPath(mkPrefPath(preferredPath));
        String res = fd.open();
		if (res != null)
			lastBrowsePath = res;
        return res; // null => cancel
	}


	// Browse for file starting at the default location,
	// changes default location on Ok
	public static String browseFile(Shell shell, boolean browseForSave, String text, String[] filterExt) {
		return browseFile(shell, browseForSave, text, filterExt, null);
	}
	
	// Browse for file starting near the given location (or default if it is null or can't be found)
	// changes default location on Ok
	public static String browseFile(Shell shell, boolean browseForSave, String text, String[] filterExt, String preferredPath) {
		FileDialog fd = new FileDialog(shell, browseForSave ? SWT.SAVE : SWT.OPEN);
		fd.setText(text);
        fd.setFilterPath(mkPrefPath(preferredPath));
        fd.setFilterExtensions(filterExt);
        String res = fd.open();
        lastBrowsePath = fd.getFilterPath();
        return res; // null => cancel
	}

	private static String mkPrefPath(String preferredPath) {		
		if (preferredPath == null) 
			return lastBrowsePath;
		while (true) {
			File f = new File(preferredPath);
			if (f.exists() && f.isDirectory())
				return preferredPath;
			preferredPath = f.getParent();
			if (preferredPath == null) {
				return lastBrowsePath;
			}
		}
	}



	public static void addCharsFilterValidator(Text t, final String charsToExclude) {
		t.addListener(SWT.Verify, new Listener() {
		   public void handleEvent(Event e) {
		      String s = e.text;
		      for (int i=0; i<charsToExclude.length(); ++i) {
		    	  if (s.indexOf(charsToExclude.charAt(i))>= 0) {
			            e.doit = false;
			            return;
		    	  }
		      }
		   }
		});
	}
	
	/**
	 * Play Yes/No message box in GUI thread
	 * @param shell - shell or null to use default
	 * @param caption - dialog title
	 * @param message - dialog text
	 * @return true when 'Yes'
	 */
	public static boolean YesNoQuestion(Shell shell, String caption, String message) {
		return ShowMessageBox(shell, caption, message, SWT.YES | SWT.NO ) == SWT.YES;
	}

	public static void OkMessageBox(Shell shell, String caption, String message) {
		ShowMessageBox(shell, caption, message, SWT.OK );
	}

	/**
	 * Play message box in GUI thread
	 * @param shell - shell or null to use default
	 * @param caption - dialog title
	 * @param message - dialog text
	 * @param style - SWT styles
	 * @return swt style
	 */
	public static int ShowMessageBox(final Shell shell, final String caption, final String message, final int style) {
		return new MessageBoxRunner().ShowMessageBox(shell, caption, message, style);
	}
	
	private static class MessageBoxRunner {
		private volatile int result = 0;
		public int ShowMessageBox(final Shell shell, final String caption, final String message, final int style) 
		{
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageBox mb = new MessageBox(shell == null ? SwtUtils.getDefaultShell() : shell, style);
					mb.setText(caption);
					mb.setMessage(message);
					result = mb.open();
				}
			});
			return result;
		}
	}
	
	
	// --------------------------------------------------
	// mix from org.eclipse.debug.internal.ui.SWTFactory:
	// --------------------------------------------------
	
	
	/**
	 * Creates a new label widget
	 * @param parent the parent composite to add this label widget to
	 * @param text the text for the label
	 * @param hspan the horizontal span to take up in the parent composite
	 * @return the new label
	 */
	public static Label createLabel(Composite parent, String text, int hspan) {
	    return createLabel(parent, text, hspan, SWT.NONE, GridData.FILL_HORIZONTAL);
	}
	
    /**
     * Creates a new label widget
     * @param parent the parent composite to add this label widget to
     * @param text the text for the label
     * @param hspan the horizontal span to take up in the parent composite
     * @param fontstyle - SWT style
     * @return the new label
     */
    public static Label createLabel(Composite parent, String text, int hspan, int fontstyle) {
        return createLabel(parent, text, hspan, fontstyle, GridData.FILL_HORIZONTAL);
    }
    
    /**
     * Creates a new label widget
     * @param parent the parent composite to add this label widget to
     * @param text the text for the label
     * @param hspan the horizontal span to take up in the parent composite
     * @param fontstyle - SWT style
     * @param gdStyle - GridData style
     * @return the new label
     */
    public static Label createLabel(Composite parent, String text, int hspan, int fontstyle, int gdStyle) {
        Label l = new Label(parent, SWT.NONE);
        if (fontstyle != SWT.NONE){
            FontData fd = parent.getFont().getFontData()[0];
            l.setFont(new Font(Display.getDefault(), fd.getName(), fd.getHeight(), fontstyle));
        } else {
            l.setFont(parent.getFont());
        }
        l.setText(text);
        GridData gd = new GridData(gdStyle);
        gd.horizontalSpan = hspan;
        gd.grabExcessHorizontalSpace = false;
        l.setLayoutData(gd);
        return l;
    }
    

	/**
	 * Creates a wrapping label
	 * @param parent the parent composite to add this label to
	 * @param text the text to be displayed in the label
	 * @param hspan the horizontal span that label should take up in the parent composite
	 * @param wrapwidth the width hint that the label should wrap at
	 * @return a new label that wraps at a specified width
	 */
	public static Label createWrapLabel(Composite parent, String text, int hspan, int wrapwidth) {
		Label l = new Label(parent, SWT.NONE | SWT.WRAP);
		l.setFont(parent.getFont());
		l.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		gd.widthHint = wrapwidth;
		l.setLayoutData(gd);
		return l;
	}

	
    /**
     * Creates a new link widget
     * @param parent the parent composite to add this link widget to
     * @param text the text for the link
     * @param hspan the horizontal span to take up in the parent composite
     * @return the new link
     */
    public static Link createLink(Composite parent, String text, int hspan, SelectionAdapter listener) {
        Link l = new Link(parent, SWT.NONE);
        l.setFont(parent.getFont());
        l.setText(text);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = hspan;
        gd.grabExcessHorizontalSpace = false;
        l.setLayoutData(gd);
        l.addSelectionListener(listener);
        return l;
    }
	
	/**
	 * Creates a vertical spacer for separating components. If applied to a 
	 * <code>GridLayout</code>, this method will automatically span all of the columns of the parent
	 * to make vertical space
	 * 
	 * @param parent the parent composite to add this spacer to
	 * @param xLines - space height (in standard font height units)
	 */
	public static void createVerticalSpacer(Composite parent, double xLines) {
		Label lbl = new Label(parent, SWT.NONE);
		
	    GC gc = new GC(parent);
	    int cyLine;
	    try {
	    	cyLine = gc.textExtent("Wq").y; //$NON-NLS-1$
		} finally {
			gc.dispose();
		}
	    int cy = (int)((double)cyLine * xLines);
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		Layout layout = parent.getLayout();
		if(layout instanceof GridLayout) {
			gd.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
		}
		gd.heightHint = cy;
		lbl.setLayoutData(gd);
	}


	public static Text createSingleText(Composite parent, int hspan, int style) {
    	Text t = new Text(parent, style);
    	t.setFont(parent.getFont());
    	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    	gd.horizontalSpan = hspan;
    	t.setLayoutData(gd);
    	return t;
    }
	
	public static Text createSingleText(Composite parent, int hspan) {
    	return createSingleText(parent, hspan, SWT.SINGLE | SWT.BORDER);
    }
	
	public static List createList(Composite parent, int hspan, int style, int nLines) {
		List l = new List(parent, style);
		l.setFont(parent.getFont());
	    GC gc = new GC(l);
	    int cyLine;
	    try{
	    	cyLine = gc.textExtent("Wq").y; //$NON-NLS-1$
	    }
	    finally{
	    	gc.dispose();
	    }
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		gd.heightHint = cyLine * nLines + 5;
		l.setLayoutData(gd);
		return l;
	}


	public static Button createPushButton(Composite parent, String label, Image image) {
		return createPushButton(parent, label, 1, image);
	}	
	
	public static Button createPushButton(Composite parent, String label, int hspan, Image image) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		if (image != null) {
			button.setImage(image);
		}
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		gd.horizontalSpan = hspan;
		button.setLayoutData(gd);	
		setButtonDimensionHint(button);
		return button;	
	}	
	

public static int getButtonWidthHint(Button button) {
		PixelConverter converter= new PixelConverter(button);
		int widthHint= converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	public static void setButtonDimensionHint(Button button) {
		Assert.isNotNull(button);
		Object gd= button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData)gd).widthHint= getButtonWidthHint(button);	
			((GridData)gd).horizontalAlignment = GridData.FILL;	 
		}
	}		

	public static Combo createCombo(Composite parent, int hspan, int style) {
	    return createCombo(parent, hspan, style, GridData.FILL_HORIZONTAL);
	}

    public static Combo createCombo(Composite parent, int hspan, int style, int gdStyle) {
        Combo c = new Combo(parent, style);
        c.setFont(parent.getFont());
        GridData gd = new GridData(gdStyle);
        gd.horizontalSpan = hspan;
        c.setLayoutData(gd);
        return c;
    }

	public static Button createCheckbox(Composite parent, String label, int hspan) {
		Button cb = new Button(parent, SWT.CHECK);
		cb.setFont(parent.getFont());
		if (label != null) {
			cb.setText(label);
		}
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = false;
    	gd.horizontalSpan = hspan;
    	cb.setLayoutData(gd);
		return cb;	
	}
	
	public static Button createRadiobutton(Composite parent, String label, int hspan) {
		Button cb = new Button(parent, SWT.RADIO);
		cb.setFont(parent.getFont());
		if (label != null) {
			cb.setText(label);
		}
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = false;
    	gd.horizontalSpan = hspan;
    	cb.setLayoutData(gd);
		return cb;	
	}

	/**
	 * Creates a Composite widget
	 * @param parent the parent composite to add this composite to
	 * @param font the font to set on the control
	 * @param columns the number of columns within the composite
	 * @param hspan the horizontal span the composite should take up on the parent
	 * @param fill the style for how this composite should fill into its parent
	 * @return the new group
	 */
	public static Composite createComposite(Composite parent, Font font, int columns, int hspan, int fill) {
    	Composite g = new Composite(parent, SWT.NONE);
    	g.setLayout(new GridLayout(columns, false));
    	g.setFont(font);
    	GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
    	g.setLayoutData(gd);
    	return g;
    }
	
    /**
     * Creates a Group widget
     * @param parent the parent composite to add this group to
     * @param text the text for the heading of the group
     * @param columns the number of columns within the group
     * @param hspan the horizontal span the group should take up on the parent
     * @param fill the style for how this composite should fill into its parent
     * @return the new group
     */
	public static Group createGroup(Composite parent, String text, int columns, int hspan, int fill) {
    	Group g = new Group(parent, SWT.NONE);
    	g.setLayout(new GridLayout(columns, false));
    	g.setText(text);
    	g.setFont(parent.getFont());
    	GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
    	g.setLayoutData(gd);
    	return g;
    }

    public static Composite createComposite(Composite parent, Font font, int columns, int hspan, int fill, int marginwidth, int marginheight) {
        Composite g = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(columns, false);
        layout.marginWidth = marginwidth;
        layout.marginHeight = marginheight;
        g.setLayout(layout);
        if (font != null) {
            g.setFont(font);
        }
        GridData gd = new GridData(fill);
        gd.horizontalSpan = hspan;
        g.setLayoutData(gd);
        return g;
    }

    /**
     * Create empty cell for layout
     * 
     * @param cx
     *            , cy - sizes
     */
	public static void createFreeSpace(Composite parent, int cx, int cy, int hspan) {
		Label l = new Label(parent, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = hspan;
		gd.grabExcessHorizontalSpace = false;
		gd.widthHint = cx;
		gd.heightHint = cy;
		l.setLayoutData(gd);
	}
	
    /**
     * Creates a separator line. Expects a <code>GridLayout</code> with at least 1 column.
     *
     * @param composite the parent composite
     * @param nColumns number of columns to span
     * @param height The height of the separator
     */
    public static Control createSeparator(Composite composite, int nColumns, int height) {
        Separator sep = new Separator(SWT.SEPARATOR | SWT.HORIZONTAL);
        sep.doFillIntoGrid(composite, nColumns, height);
        return sep.getSeparator(composite);
    }
	
    
    public static ColorSelector createColorSelector(Composite composite, int hspan) {
    	ColorSelector cs = new ColorSelector(composite);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = hspan;
		cs.getButton().setLayoutData(gd);
		return cs;
    }
    
    
    public static int getCharHeight(Control ctr) {
        GC gc = new GC(ctr);
        FontMetrics fm;
        try {
        	gc.setFont(JFaceResources.getDialogFont());
        	fm = gc.getFontMetrics();
		} finally {
			gc.dispose();
		}
        return fm.getHeight();
    }


}
