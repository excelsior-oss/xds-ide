package com.excelsior.xds.ui.commons.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Dialog page separator.
 */
public class Separator {

    private Label fSeparator;
    private int fStyle;

    public Separator() {
        this(SWT.NONE);
    }

    /**
     * @param style of the separator. See <code>Label</code> for possible
     * styles.
     */
    public Separator(int style) {
        fStyle= style;
    }

    // ------- layout helpers

    /**
     * Creates the separator and fills it in a MGridLayout.
     * @param height The height of the separator
     */
    public Control[] doFillIntoGrid(Composite parent, int nColumns, int height) {
        Control separator= getSeparator(parent);
        separator.setLayoutData(gridDataForSeperator(nColumns, height));

        return new Control[] { separator };
    }

    public Control[] doFillIntoGrid(Composite parent, int nColumns) {
        return doFillIntoGrid(parent, nColumns, 4);
    }

    protected static GridData gridDataForSeperator(int span, int height) {
        GridData gd= new GridData();
        gd.horizontalAlignment= GridData.FILL;
        gd.verticalAlignment= GridData.BEGINNING;
        gd.heightHint= height;
        gd.horizontalSpan= span;
        return gd;
    }
    
    // ------- ui creation

    /**
     * Creates or returns the created separator.
     * @param parent The parent composite or <code>null</code> if the widget has
     * already been created.
     */
    public Control getSeparator(Composite parent) {
        if (fSeparator == null) {
            fSeparator= new Label(parent, fStyle);
        }
        return fSeparator;
    }
    
}
