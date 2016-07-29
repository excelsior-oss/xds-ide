package com.excelsior.xds.xbookmarks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class XBookmarksDialog extends PopupDialog {
    
    public static class Model {
        public Model(int columns) {
            this.columns = columns;
            rows = new ArrayList<Row>();
        }
        
        public Row addRow(Row r) {
            rows.add(r);
            return r;
        }
        
        public ArrayList<Row> getRows() {
            return rows;
        }
        
        public void SortRows(Comparator<XBookmarksDialog.Model.Row> comparator) {
            Collections.sort(rows, comparator);
        }
        
        private int columns;
        private ArrayList<Row> rows; 
        
        public static class Row {
            private String[] fields;
            private boolean enabled;
            private int data;
            
            public Row(String[] fields) {
                this.fields = fields;
                enabled = true;
                data = -1;
            }
            
            public String getField(int idx) {
                return fields.length > idx ? fields[idx] : ""; //$NON-NLS-1$
            }
            
            public void setField(int idx, String s) {
                if (fields.length > idx) 
                    fields[idx] = s;
            }

            public Row setEnabled(boolean b) {
                enabled = b;
                return this;
            }
            
            public boolean isEnabled() {
                return enabled;
            }

            public Row setData(int data) {
                this.data = data;
                return this;
            }
            
            public int getData() {
                return data;
            }
        }
       
    }
    
    //--------------------------------------------
    
    private TableViewer tableViewer;
    private Table table;
    private Model model;
    private Listener selectionListener;

    public XBookmarksDialog(Model model, 
                      Listener selectionListener,
                      Shell parent, 
                      boolean persistSize, 
                      boolean persistLocation,
                      boolean showDialogMenu, 
                      boolean showPersistActions,
                      String titleText, 
                      String infoText) 
    {
        super(parent, INFOPOPUPRESIZE_SHELLSTYLE, true, persistSize, persistLocation,
                showDialogMenu, showPersistActions, titleText, infoText);
        this.model = model;
        this.selectionListener = selectionListener;
    }
    
    /* use it only after open() */
    public void addListener (int type, Listener listener) {
        table.addListener (type, listener);
    }
    
    public int getSelectionIndex() {
        return table.getSelectionIndex();
    }
    
    public Model.Row getSelection() {
        int idx = getSelectionIndex();
        if (idx >= 0 && idx < model.getRows().size()) {
            return model.getRows().get(idx);
        }
        return null;
    }
    
    /* redraws table: new content in model will be shown without size adjustments */
    public void update() {
        tableViewer.refresh();
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().extendedMargins(Util.isWindows() ? 0 : 3, 3, 2, 2).applyTo(composite);

        Composite tableComposite = new Composite(composite, SWT.NONE);
        tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableComposite.setLayout(new GridLayout(1, false));
        
        tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        
        table = tableViewer.getTable();
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableViewer.setContentProvider(new MenuTableContentProvider());
        tableViewer.setLabelProvider(new MenuTableLabelProvider());

        { // Columns:
            GC gc= new GC(table);
            try {
            	int maxW = gc.stringExtent("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW").x; //$NON-NLS-1$
            	int gap  = gc.stringExtent("WW").x; //$NON-NLS-1$
            	int widths[] = new int[model.columns];
            	for (int i=0; i<model.columns; ++i) {
            		widths[i] = 0;
            	}
            	for (Model.Row r : model.getRows()) {
            		for (int i=0; i<model.columns; ++i) {
            			if (!r.getField(i).isEmpty()) {
            				int w     = Math.min(gc.stringExtent(r.getField(i)).x + gap, maxW);
            				widths[i] = Math.max(widths[i], w);
            			}
            		}
            	}
            	
            	for (int i=0; i<model.columns; ++i) {
            		TableColumn tc = new TableColumn(table, SWT.LEFT);
            		tc.setWidth(widths[i]);
            	}
            	table.setHeaderVisible(false);
            	table.setLinesVisible(false);
			} finally {
				gc.dispose();
			}
        }
        
        Listener eventListener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type == SWT.MouseDoubleClick || 
                   (event.type == SWT.KeyDown && event.character == SWT.CR)) 
                {
                    doSelect();
                }
            }
            
        };
        
        addListener (SWT.KeyDown, eventListener);
        addListener (SWT.MouseDoubleClick, eventListener);
            
        tableViewer.setInput(model);
        table.select(0);

        return composite;
    }
    
    @Override
    protected Control getFocusControl() {
        return table;
    }
    
    private void doSelect() {
        int idx = table.getSelectionIndex();
        if (idx >= 0) {
            Model.Row r = model.getRows().get(idx); 
            Event event = new Event();
            event.data = r;
            selectionListener.handleEvent(event);
            close();
        }
    }

    
    private class MenuTableContentProvider implements IStructuredContentProvider {
        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return ((Model)inputElement).getRows().toArray();
        }
    }

    public class MenuTableLabelProvider implements ITableLabelProvider, ITableColorProvider {

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            Model.Row row = (Model.Row)element;
            return row.getField(columnIndex);
        }
        
        @Override
         public Color getBackground(Object element, int columnIndex) {
               return null;
         }

         @Override
         public Color getForeground(Object element, int columnIndex) {
             Model.Row row = (Model.Row)element;
             if (!row.isEnabled())
                 return new Color(Display.getDefault(), new RGB(0x80,0x80,0x80));
             return null; 
         }
    }


}
