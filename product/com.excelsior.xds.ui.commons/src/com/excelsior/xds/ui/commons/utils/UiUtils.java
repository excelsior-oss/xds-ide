package com.excelsior.xds.ui.commons.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.excelsior.xds.core.utils.IDisposable;

public class UiUtils {
  public static GridLayout removeMargins(GridLayout layout) {
    layout.marginBottom = 0;
    layout.marginHeight = 0;
    layout.marginLeft = 0;
    layout.marginRight = 0;
    layout.marginTop = 0;
    layout.marginWidth = 0;
    return layout;
  }

  public static RowLayout removeMargins(RowLayout layout) {
    layout.marginBottom = 0;
    layout.marginHeight = 0;
    layout.marginLeft = 0;
    layout.marginRight = 0;
    layout.marginTop = 0;
    layout.marginWidth = 0;
    return layout;
  }

  public static Font modifyFont(Control c, int flags) {
    Font f = c.getFont();
    return modifyFont(c.getDisplay(), f, flags);
  }
  
  public static Font modifyFont(Device d, Font f, int flags) {
	  FontData fd = modifyFont(f.getFontData()[0], flags);
	  return new Font(d, fd);
  }
  
  public static FontData modifyFont(FontData fd, int flags) {
	  FontData newFd = new FontData(fd.toString());
	  newFd.setStyle(newFd.getStyle() | flags);
	  return newFd;
  }

  public static Sash createSplitter(Composite parent, FormAttachment left) {
    final Sash sash = new Sash(parent, SWT.VERTICAL);
    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0); // Attach to top
    formData.bottom = new FormAttachment(100, 0); // Attach to bottom
    formData.left = left;
    sash.setLayoutData(formData);
    sash.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        // We reattach to the left edge, and we use the x value of the event to
        // determine the offset from the left
        ((FormData) sash.getLayoutData()).left = new FormAttachment(0, event.x);

        // Until the parent window does a layout, the sash will not be redrawn
        // in
        // its new location.
        sash.getParent().layout();
      }
    });
    return sash;
  }
  
  public static int getClickedItemColumnIndex(Table table, Point point){
    return getClickedItemColumnIndex(table, table.getItem(point), point);
  }
  
  public static int getClickedItemColumnIndex(Table table, TableItem item, 
      Point point){
    int column = -1;
    if (item != null) {
      // Determine which column was selected
      for (int i = 0, n = table.getColumnCount(); i < n; i++) {
        Rectangle rect = item.getBounds(i);
        if (rect.contains(point)) {
          // This is the selected column
          column = i;
          break;
        }
      }
    }
    return column;
  }

  public static void dispose(Resource r) {
    if (null != r && !r.isDisposed()) {
      r.dispose();
    }
  }
  
  public static void dispose(Control c) {
    if (null != c && !c.isDisposed()) {
      c.dispose();
    }
  }
  
  public static void dispose(IDisposable disposable) {
	  if (disposable != null) {
		  disposable.dispose();
	  }
  }
  
  public static org.eclipse.swt.graphics.Color createSwtColorFromAwt(Display display,
      java.awt.Color awtColor) {
    return new org.eclipse.swt.graphics.Color(display, awtColor.getRed(), awtColor.getGreen(),
        awtColor.getBlue());
  }

  public static java.awt.Color createAwtFromSwtColor(org.eclipse.swt.graphics.Color swtColor) {
    return new java.awt.Color(swtColor.getRed(), swtColor.getGreen(), swtColor.getBlue());
  }
  
  public static void setEnabled(Table table, boolean isEnabled){
    for (int i = 0; i < table.getItemCount(); i++) {
      table.getItem(i).setGrayed(!isEnabled);
    }
  }
  
  public static void resizeColumnsByCaption(Table table){
    GC gc = new GC(table);
    try {
    	for (int i = 0; i < table.getColumnCount(); i++) {
    		int maxTextExtent = getColumnTextWidth(table, gc, i);
    		table.getColumn(i).setWidth(maxTextExtent);
    	}
	} finally {
		gc.dispose();
	}
  }
  
  public static void resizeColumsByContent(Table table){
    for (int i = 0; i < table.getColumnCount(); i++) {
      table.getColumn(i).pack();
    }
  }

  public static int getColumnTextWidth(Table table, GC gc, int i) {
    TableColumn tableColumn = table.getColumn(i);
    int maxTextExtent = 20 + gc.textExtent(tableColumn.getText()).x;
    return maxTextExtent;
  }
  
  public static void resizeColumnByCaption(Table table, int columnIdx){
    GC gc = new GC(table);
    int maxTextExtent;
    try {
    	maxTextExtent = getColumnTextWidth(table, gc, columnIdx);
	} finally {
		gc.dispose();
	}
    table.getColumn(columnIdx).setWidth(maxTextExtent);
  }
  
  public static void setDataColumnBackground(Table table, int columnIdx, Color color){
    for (int i = 0; i < table.getItemCount(); i++) {
      table.getItem(i).setBackground(columnIdx, color);
    }
  }
  
  public static Boolean setEnabled(Control control, boolean isEnabled){
    if (control.isDisposed()) return null;
    
    boolean isEnabledBefore = control.getEnabled();
    if (isEnabledBefore != isEnabled){
      control.setEnabled(isEnabled);
    }
    return isEnabledBefore;
  }
  
  public static int getMaximumLengthOfText(int maxSize, GC gc){
    String text = "w";
    int size = 0;
    while(gc.textExtent(text).x < maxSize ){
      ++size;
      text += "w";
    }
    return size;
  }
  
  public static Image getPlatformImage(String imageId){
    return PlatformUI.getWorkbench().getSharedImages().getImage(
        imageId);
  }
  
  public static void addFocusTransfering(Control control, final Control gainFocusControl){
    control.addFocusListener(new FocusListener() {
      
      @Override
      public void focusLost(FocusEvent e) {
        gainFocusControl.forceFocus();
      }
      
      @Override
      public void focusGained(FocusEvent e) {
      }
    });
  }
  
  public static void disableTabTraversal(Control c){
    c.addTraverseListener(new TraverseListener () {
      public void keyTraversed(TraverseEvent e) {
        switch (e.detail) {
          case SWT.TRAVERSE_TAB_NEXT:
          case SWT.TRAVERSE_TAB_PREVIOUS: {
            e.doit = false;
          }
        }
      }
    });
  }
}
