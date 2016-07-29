package com.excelsior.xds.ui.editor.commons.internal.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import com.excelsior.xds.core.help.IXdsHelpContextIds;
import com.excelsior.xds.ui.commons.utils.HelpUtils;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.editor.commons.ITokens;
import com.excelsior.xds.ui.editor.commons.PersistentTokenDescriptor;
import com.excelsior.xds.ui.editor.commons.internal.nls.Messages;
import com.excelsior.xds.ui.editor.commons.preferences.ISyntaxColoringPreferences;
import com.excelsior.xds.ui.editor.commons.preferences.ITokenModification;
import com.excelsior.xds.ui.editor.commons.preferences.SyntaxColoringPreferencesRegistry;

@SuppressWarnings("restriction")
public class SyntaxColoringPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    
    private TreeViewer fTreeViewer;
    private java.util.List<HighlightingColorTreeItem> fListAllItems;
    private Button fEnable;
    private ColorSelector fSyntaxForegroundColorEditor;
    private Button fBoldCheckBox;
    private Button fItalicCheckBox;
    private Button fStrikethroughCheckBox;
    private Button fUnderlineCheckBox;
    private SyntaxColorerPreviewer fPreviewer;
	private final List<ISyntaxColoringPreferences> syntaxColorings;
    
    public SyntaxColoringPreferencePage() {
    	this(""); //$NON-NLS-1$
    }
    
    public SyntaxColoringPreferencePage(String title) {
        super(title);
        setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
        syntaxColorings = SyntaxColoringPreferencesRegistry.get().contributions();
    }

    public SyntaxColoringPreferencePage(String title, ImageDescriptor image) {
        super(title, image);
        setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
        syntaxColorings = SyntaxColoringPreferencesRegistry.get().contributions();
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(final Composite parent) {
        HelpUtils.setHelp(parent, IXdsHelpContextIds.MODULA2_SYNTAX_COLORING_PREFERENCE_PAGE);

        // Create tree model:
    	ArrayList<RootTreeItem> treeModel = new ArrayList<RootTreeItem>();
    	fListAllItems = new ArrayList<HighlightingColorTreeItem>();
    	int maxTreeItemWidth = 0;
    	
    	for (ISyntaxColoringPreferences syntaxColoring : syntaxColorings) {
    		// Add root for M2 O2 files:
	    	RootTreeItem root = new RootTreeItem(syntaxColoring.getLanguageName(), syntaxColoring); 
	    	
	    	HashMap<String, RootTreeItem> catName2Root = new HashMap<String, SyntaxColoringPreferencePage.RootTreeItem>();
            for (ITokens modulaToken : syntaxColoring.getTokens()) {
                String cat = modulaToken.getCategoryName();
                HighlightingColorTreeItem it = new HighlightingColorTreeItem(syntaxColoring, modulaToken);
                if (cat == null) {
                    root.getItems().add(it);
                }
                else {
                	RootTreeItem catRoot = catName2Root.get(cat);
                    if (catRoot == null) {
                        catRoot = new RootTreeItem(cat, syntaxColoring);
                        catName2Root.put(cat,  catRoot);
                        root.getItems().add(catRoot);
                    }
                    catRoot.getItems().add(it);
                }
                fListAllItems.add(it);
                maxTreeItemWidth = Math.max(maxTreeItemWidth, convertWidthInCharsToPixels(it.toString().length() + 5));
            }
	    	treeModel.add(root);
		}
    	
        Composite colorComposite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        colorComposite.setLayout(layout);

        // Default colors and font can be configured on the....
        Link link= new Link(colorComposite, SWT.NONE);
        link.setText(Messages.SyntaxColoringPreferencePage_LinkHrefsDescription);
//        link.setText("Default colors and font can be configured on the " +
//                "<a href=\"org.eclipse.ui.preferencePages.GeneralTextEditor\">'Text Editors'</a> " +
//                "and on the <a href=\"org.eclipse.ui.preferencePages.ColorsAndFonts\">'Colors and Fonts'</a> " +
//                "preference page.");
        link.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                        if ("org.eclipse.ui.preferencePages.GeneralTextEditor".equals(e.text)) //$NON-NLS-1$
                                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
                        else if ("org.eclipse.ui.preferencePages.ColorsAndFonts".equals(e.text)) //$NON-NLS-1$
                                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, "selectFont:org.eclipse.jface.textfont"); //$NON-NLS-1$
                }
        });
        GridData gridData= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gridData.widthHint= 150; // only expand further if anyone else requires it
        gridData.horizontalSpan= 2;
        link.setLayoutData(gridData);

        //
        SWTFactory.createLabel(colorComposite,  "", 1); //$NON-NLS-1$

        // Element:
        SWTFactory.createLabel(colorComposite,  Messages.SyntaxColoringPreferencePage_Element+':', 1);

        // 2-columns composite
        Composite editorComposite= new Composite(colorComposite, SWT.NONE);
        layout= new GridLayout();
        layout.numColumns= 2;
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        editorComposite.setLayout(layout);
        GridData gd= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        editorComposite.setLayoutData(gd);

        // Tree
        fTreeViewer= new TreeViewer(editorComposite, SWT.SINGLE | SWT.BORDER);
        fTreeViewer.setLabelProvider(new ColorListLabelProvider());
    	fTreeViewer.setContentProvider(new ColorListContentProvider());
    	fTreeViewer.setInput(treeModel);
    	
        gd= new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true);
        gd.heightHint= convertHeightInCharsToPixels(9);
        ScrollBar vBar= ((Scrollable) fTreeViewer.getControl()).getVerticalBar();
        if (vBar != null)
        	maxTreeItemWidth += vBar.getSize().x * 5; // scrollbars and tree indentation guess
        gd.widthHint= maxTreeItemWidth;

        fTreeViewer.getControl().setLayoutData(gd);
        installDoubleClickListener(); // will be used in real tree, just it is a list

        // Styles...
        Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 10;
        layout.numColumns= 3;
        stylesComposite.setLayout(layout);
        stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        // [v] Enable
        fEnable = SWTFactory.createCheckbox(stylesComposite, Messages.SyntaxColoringPreferencePage_Enable, 3);

        //     Color: [   ]
        SWTFactory.createLabel(stylesComposite, "    ", 1); //$NON-NLS-1$
        SWTFactory.createLabel(stylesComposite, Messages.SyntaxColoringPreferencePage_Color+':', 1);

        fSyntaxForegroundColorEditor= new ColorSelector(stylesComposite); //TODO use SWTFactory.createColorSelector()
        Button foregroundColorButton= fSyntaxForegroundColorEditor.getButton();
        gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        foregroundColorButton.setLayoutData(gd);

        //     [v] Bold
        //     [v] Italic
        //     [v] Strikethrough
        //     [v] Underline
        SWTFactory.createLabel(stylesComposite, "    ", 1); //$NON-NLS-1$
        fBoldCheckBox = SWTFactory.createCheckbox(stylesComposite, Messages.SyntaxColoringPreferencePage_Bold, 2);
        SWTFactory.createLabel(stylesComposite, "    ", 1); //$NON-NLS-1$
        fItalicCheckBox = SWTFactory.createCheckbox(stylesComposite, Messages.SyntaxColoringPreferencePage_Italic, 2);
        SWTFactory.createLabel(stylesComposite, "    ", 1); //$NON-NLS-1$
        fStrikethroughCheckBox = SWTFactory.createCheckbox(stylesComposite, Messages.SyntaxColoringPreferencePage_Strikethrough, 2);
        SWTFactory.createLabel(stylesComposite, "    ", 1); //$NON-NLS-1$
        fUnderlineCheckBox = SWTFactory.createCheckbox(stylesComposite, Messages.SyntaxColoringPreferencePage_Underline, 2);

        // Editor preview...        
        Label label= new Label(colorComposite, SWT.LEFT);
        label.setText(Messages.SyntaxColoringPreferencePage_Preview+':');
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fPreviewer = new SyntaxColorerPreviewer(colorComposite, getPreferenceStore());
        gd= new GridData(GridData.FILL_BOTH);
        gd.widthHint= convertWidthInCharsToPixels(80);
        gd.heightHint= convertHeightInCharsToPixels(5);
        fPreviewer.setLayoutData(gd);

        fEnable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorTreeItem it = getHighlightingColorListItem();
                if (it != null) {
                    it.isDisabled = !fEnable.getSelection();
                    fPreviewer.updateColors();
                }
            }
        });

        foregroundColorButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                        HighlightingColorTreeItem it = getHighlightingColorListItem();
                        if (it != null) {
                            it.rgb = fSyntaxForegroundColorEditor.getColorValue();
                            fPreviewer.updateColors();
                        }
                }
        });

        fBoldCheckBox.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                        HighlightingColorTreeItem it = getHighlightingColorListItem();
                        if (it != null) {
                            if (fBoldCheckBox.getSelection()) {
                                it.style |= SWT.BOLD;
                            } else {
                                it.style &= ~SWT.BOLD;
                            }
                            fPreviewer.updateColors();
                        }
                }
        });

        fItalicCheckBox.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    HighlightingColorTreeItem it = getHighlightingColorListItem();
                    if (it != null) {
                        if (fItalicCheckBox.getSelection()) {
                            it.style |= SWT.ITALIC;
                        } else {
                            it.style &= ~SWT.ITALIC;
                        }
                        fPreviewer.updateColors();
                    }
                }
        });

        fUnderlineCheckBox.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    HighlightingColorTreeItem it = getHighlightingColorListItem();
                    if (it != null) {
                        if (fUnderlineCheckBox.getSelection()) {
                            it.style |= TextAttribute.UNDERLINE;
                        } else {
                            it.style &= ~TextAttribute.UNDERLINE;
                        }
                        fPreviewer.updateColors();
                    }
                }
        });
        
        fStrikethroughCheckBox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                HighlightingColorTreeItem it = getHighlightingColorListItem();
                if (it != null) {
                    if (fStrikethroughCheckBox.getSelection()) {
                        it.style |= TextAttribute.STRIKETHROUGH;
                    } else {
                        it.style &= ~TextAttribute.STRIKETHROUGH;
                    }
                    fPreviewer.updateColors();
                }
            }
        });
        
        fTreeViewer.getTree().addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
              handleTreeSelection();
            }        
        });

        TreeItem titems[] = fTreeViewer.getTree().getItems();
        if (titems.length > 0) {
        	fTreeViewer.getTree().select(titems[0]);
        }
        
        handleTreeSelection();
        colorComposite.layout(false);

        return colorComposite;
    }


    private void installDoubleClickListener() {
        fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection s = (IStructuredSelection) event.getSelection();
                Object element = s.getFirstElement();
                if (fTreeViewer.isExpandable(element)) {
                    fTreeViewer.setExpandedState(element,!fTreeViewer.getExpandedState(element));
                }
            }
        });
    }

    private HighlightingColorTreeItem getHighlightingColorListItem() {
    	TreeItem[] sel = fTreeViewer.getTree().getSelection();
    	if (sel.length > 0) {
    		Object ob = sel[0].getData();
    		if (ob instanceof HighlightingColorTreeItem) {
    			return (HighlightingColorTreeItem)ob;
    		}
    	}
    	return null;
    }

    private RootTreeItem getRootTreeItem() {
        TreeItem[] sel = fTreeViewer.getTree().getSelection();
        if (sel.length > 0) {
            Object ob = sel[0].getData();
            if (ob instanceof HighlightingColorTreeItem) {
                ob = ((ITreeContentProvider)fTreeViewer.getContentProvider()).getParent(ob);
            } else if (ob instanceof RootTreeItem) {
                return (RootTreeItem)ob;
            }
        }
        return null;
    }

    private void getHLItemsFromRoot(RootTreeItem root, List<HighlightingColorTreeItem> dest) {
        for (Object o : root.getItems()) {
            if (o instanceof HighlightingColorTreeItem) {
                dest.add((HighlightingColorTreeItem)o);
            } else if (o instanceof RootTreeItem) {
                getHLItemsFromRoot((RootTreeItem)o, dest);
            }
        }
    }
    
    private void handleTreeSelection() {
        RootTreeItem root = getRootTreeItem();
        if (root != null) {
            List<HighlightingColorTreeItem> lst = new ArrayList<HighlightingColorTreeItem>();
            getHLItemsFromRoot(root, lst);
        	fPreviewer.activateContent(root.syntaxColoring, lst);
            fPreviewer.updateColors();
        }
        
    	HighlightingColorTreeItem it = getHighlightingColorListItem();

        fEnable.setEnabled(it != null && it.mayBeDisabled);
    	fSyntaxForegroundColorEditor.setEnabled(it != null);
        fBoldCheckBox.setEnabled(it != null);
        fItalicCheckBox.setEnabled(it != null);
        fStrikethroughCheckBox.setEnabled(it != null);
        fUnderlineCheckBox.setEnabled(it != null);

        fEnable.setSelection(it == null || !it.isDisabled);
    	fSyntaxForegroundColorEditor.setColorValue(it == null ? new RGB(0x80, 0x80, 0x80) : it.rgb);
        fBoldCheckBox.setSelection(it == null ? false : (it.style & SWT.BOLD) != 0);
        fItalicCheckBox.setSelection(it == null ? false : (it.style & SWT.ITALIC) != 0);
        fStrikethroughCheckBox.setSelection(it == null ? false : (it.style & TextAttribute.STRIKETHROUGH) != 0);
        fUnderlineCheckBox.setSelection(it == null ? false : (it.style & TextAttribute.UNDERLINE) != 0);
    }
    
    @Override
    public boolean performOk() {
    	Map<ISyntaxColoringPreferences, List<HighlightingColorTreeItem>> coloring2Items = 
    	fListAllItems.stream().collect(Collectors.groupingBy(HighlightingColorTreeItem::getSyntaxColoring));
    	
		coloring2Items
				.entrySet()
				.stream()
				.forEach(
						e -> {
							ISyntaxColoringPreferences coloring = e.getKey();
							List<ITokenModification> modifications = e
									.getValue().stream()
									.map(i -> i.toTokenModification())
									.collect(Collectors.toList());
							coloring.save(modifications);
						});
    	return true;
    }
    
    @Override
    public void performDefaults() {
    	for(HighlightingColorTreeItem it : fListAllItems) {
    		it.setDefaultValues();
    	}
   	    handleTreeSelection();
        fPreviewer.updateColors();
    }

    
    //////////////////////////////////////////////////////////////////////////////////
    // Tree contents
    /////////////////////////////////////////////////////////////////////////////////
    
	private static class ColorListContentProvider implements ITreeContentProvider {

		private ArrayList<RootTreeItem> model;
		
		@Override
		public void dispose() {
		}

		@SuppressWarnings({ "unchecked" })
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.model = (ArrayList<RootTreeItem>) newInput;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return model.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof RootTreeItem) {
				return ((RootTreeItem)parentElement).getItems().toArray();
			}
			return null;
		}
		
		private Object getParent(Object element, RootTreeItem rti) {
            for (Object o : rti.getItems()) {
                if (o == element) {
                    return rti;
                }
                if (o instanceof RootTreeItem) {
                    Object x = getParent(element, (RootTreeItem)o);
                    if (x != null) {
                        return x;
                    }
                }
            }
            return null;
		}

		@Override
		public Object getParent(Object element) {
			for (RootTreeItem r : model) {
                Object x = getParent(element, r);
                if (x != null) {
                    return x;
                }
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return (element instanceof RootTreeItem);
		}
	}

    private static class ColorListLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            return String.valueOf(element);
        }
    }

    private static class RootTreeItem {
    	private final List<Object> items = new ArrayList<Object>();
		private String name;
		
		final ISyntaxColoringPreferences syntaxColoring;
    	
    	public RootTreeItem(String name, ISyntaxColoringPreferences syntaxColoring) {
    		this.syntaxColoring = syntaxColoring;
    		this.name = name;
    	}
    	
    	public List<Object> getItems (){
    		return items;
    	}    	
    	
       	@Override
    	public String toString() {
    		return name;
    	}
    }

    static class HighlightingColorTreeItem {
    	final PersistentTokenDescriptor token;
    	final ISyntaxColoringPreferences syntaxColoring;
    	ITokens iToken;

        int style;
        RGB rgb;
        boolean isDisabled;
        final boolean mayBeDisabled;
        
        HighlightingColorTreeItem(ISyntaxColoringPreferences syntaxColoring, ITokens iTokens) {
        	this.iToken = iTokens;
        	this.syntaxColoring = syntaxColoring;
        	token   = iTokens.getToken();
            mayBeDisabled = token.mayBeDisabled();
            isDisabled = token.isDisabled();
            style = token.getStyleWhenEnabled();
            rgb   = token.getRgbWhenEnabled();
        }
        
        ISyntaxColoringPreferences getSyntaxColoring() {
			return syntaxColoring;
		}

		void setDefaultValues() {
            style = token.getDefaultStyle();
            rgb   = token.getDefaultRgb();
        }
        
        ITokenModification toTokenModification(){
        	return new ITokenModification() {
				
				@Override
				public boolean isDisabled() {
					return isDisabled;
				}
				
				@Override
				public ITokens getToken() {
					return iToken;
				}
				
				@Override
				public int getStyle() {
					return style;
				}
				
				@Override
				public RGB getRgb() {
					return rgb;
				}
			};
        }
        
        PersistentTokenDescriptor getToken() {
            return token;
        }
        
        @Override
        public String toString() {
            return token.getName();
        }
    }
}
