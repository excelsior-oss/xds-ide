package com.excelsior.xds.ui.search.modula;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.search.modula.ModulaSymbolMatch;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.ui.commons.swt.resources.ResourceRegistry;
import com.excelsior.xds.ui.commons.utils.XStyledString;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.internal.services.ServiceHolder;
import com.excelsior.xds.ui.search.ResultModel.ModelItem;
import com.excelsior.xds.ui.tools.colorers.IModulaSyntaxColorer;
import com.excelsior.xds.ui.viewers.ModulaSymbolImages;
import com.excelsior.xds.ui.viewers.XdsElementImages;

@SuppressWarnings("restriction")
public class ModulaSearchLabelProvider extends    DecoratingStyledCellLabelProvider 
                                       implements IPropertyChangeListener
                                                , ILabelProvider 
{
	
    public ModulaSearchLabelProvider(ModulaSearchResultPage resultPage, ResourceRegistry resourceRegistry) {
        super(new M2LabelProvider(resultPage, resourceRegistry), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(), null);
    }

    public void initialize(ColumnViewer viewer, ViewerColumn column) {
        PlatformUI.getPreferenceStore().addPropertyChangeListener(this);
        EditorsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this); 
        JFaceResources.getColorRegistry().addListener(this);

        setOwnerDrawEnabled(showColoredLabels());

        super.initialize(viewer, column);
    }

    public void dispose() {
        super.dispose();
        PlatformUI.getPreferenceStore().removePropertyChangeListener(this);
        EditorsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
        JFaceResources.getColorRegistry().removeListener(this);
    }

    private void refresh() {
        ColumnViewer viewer= getViewer();

        if (viewer == null) {
            return;
        }
        M2LabelProvider.clrMatch = null; // to re-read
        boolean showColoredLabels= showColoredLabels();
        if (showColoredLabels != isOwnerDrawEnabled()) {
            setOwnerDrawEnabled(showColoredLabels);
            viewer.refresh();
        } else if (showColoredLabels) {
            viewer.refresh();
        }
    }

    protected StyleRange prepareStyleRange(StyleRange styleRange, boolean applyColors) {
        if (!applyColors && styleRange.background != null) {
            styleRange= super.prepareStyleRange(styleRange, applyColors);
            styleRange.borderStyle= SWT.BORDER_DOT;
            return styleRange;
        }
        return super.prepareStyleRange(styleRange, applyColors);
    }

    public static boolean showColoredLabels() {
        return PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS);
    }

    public void propertyChange(PropertyChangeEvent event) {
        String property= event.getProperty();
        if (property.equals(JFacePreferences.QUALIFIER_COLOR) || property.equals(JFacePreferences.COUNTER_COLOR) || property.equals(JFacePreferences.DECORATIONS_COLOR)
                || property.equals(IWorkbenchPreferenceConstants.USE_COLORED_LABELS) || property.equals(M2LabelProvider.EDITOR_SEARCH_RESULT_INDICATION_COLOR)) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    refresh();
                }
            });
        }
    }

    public String getText(Object element) {
        return getStyledText(element).getString();
    }
    
    public Image getImage(Object element) {
        if (element instanceof ModelItem) {
            IFile iFile = ((ModelItem)element).getIFile();
            if (iFile != null) {
                element = iFile; // it will turn on eclipse decorators for iFiles inside project trees
            }
        }
        return super.getImage(element);
    }
    
//    // Fast getText():
//    public String getTextToCompare(Object element) {
//        StringBuilder sb = new StringBuilder();
//        if (element instanceof IResource) {
//            sb.append(((IResource)element).getName());
//        }
//        else if (element instanceof ModulaSymbolMatch) {
//            ModulaSymbolMatch match = ((ModulaSymbolMatch) element);
//            sb.append(match.getLine()).append(':').append(match.getColumn());
//        }
//        return sb.toString();
//    }

    private static class M2LabelProvider extends LabelProvider implements IStyledLabelProvider {
    	private ResourceRegistry resourceRegistry;
    	
        public final static String EDITOR_SEARCH_RESULT_INDICATION_COLOR= "searchResultIndicationColor"; //$NON-NLS-1$
        private final WorkbenchLabelProvider fLabelProvider;
        private ModulaSearchResultPage resultPage;
        private Font dlgFont;
        private Styler positionStyler; // it is StyledString.QUALIFIER_STYLER + Italic
        private Font italicFont;
        
        private static Color clrMatch;
        
        private Styler matchStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                if (clrMatch == null &&
                    EditorsPlugin.getDefault().getPreferenceStore().contains(EDITOR_SEARCH_RESULT_INDICATION_COLOR)) 
                {
                    RGB rgb = PreferenceConverter.getColor(EditorsPlugin.getDefault().getPreferenceStore(), EDITOR_SEARCH_RESULT_INDICATION_COLOR);
                    clrMatch = new Color(Display.getDefault(), rgb);
                } 
                textStyle.background = clrMatch;
            }
        };
  

        public M2LabelProvider(ModulaSearchResultPage resultPage, ResourceRegistry resourceRegistry) {
            this.resultPage = resultPage;
            this.resourceRegistry = resourceRegistry;
            this.dlgFont = resultPage.getControl().getFont();
            fLabelProvider= new WorkbenchLabelProvider();
            positionStyler = new Styler() {
                @Override
                public void applyStyles(TextStyle textStyle) {
                    textStyle.foreground = JFaceResources.getColorRegistry().get(JFacePreferences.QUALIFIER_COLOR);
                    textStyle.font= getItalicFont(dlgFont);
                }
            };
        }

        private Font getItalicFont(Font dlgFont) {
            if (italicFont == null) {
                FontData[] data= dlgFont.getFontData();
                for (int i= 0; i < data.length; i++) {
                    data[i].setStyle(SWT.ITALIC);
                }
                italicFont= new Font(dlgFont.getDevice(), data);
            }
            return italicFont;
        }

        @Override
        public String getText(Object object) {
            return getStyledText(object).getString();
        }

        
        @Override
        public StyledString getStyledText(Object element) {
            StyledString sstring = new StyledString();
            
            IResource iRes = null;

            if (element instanceof IResource) {
                iRes = (IResource)element;
            } else if (element instanceof ModelItem) {
                iRes = ((ModelItem)element).getIFile();
                if (iRes == null) {
                    IXdsElement xdsEl = ((ModelItem)element).getIXdsElement();
                    if (xdsEl != null) {
                        sstring.append(xdsEl.getElementName());
                    }
                }
            }

            if (iRes != null) {
                sstring.append(iRes.getName());
                AbstractTextSearchResult sr = resultPage.getInput();
                Match[] matches = sr.getMatches(iRes);
                if (matches.length > 1) {
                    sstring.append(' ');
                    sstring.append(String.format(Messages.ModulaSearchLabelProvider_cnt_matches, matches.length), StyledString.COUNTER_STYLER);
                }
            }
            else if (element instanceof ModulaSymbolMatch) {
                ModulaSymbolMatch match = ((ModulaSymbolMatch) element);
                
                String s = Messages.format( "({0}: {1})", new Object[]{ match.getLine(), match.getColumn() }); //$NON-NLS-1$
                sstring.append(s, positionStyler);
                sstring.append("  "); //$NON-NLS-1$

                String ctxLine = match.getContextLine();
                if (ctxLine != null) {

                    int trimL = 0;
                    for (; trimL < ctxLine.length(); ++trimL) {
                        char ch = ctxLine.charAt(trimL);
                        if (ch != ' ' && ch != '\t') {
                            break;
                        }
                    }
                    ctxLine = ctxLine.substring(trimL);

                    XStyledString xss;
                    try {
                    	IModulaSyntaxColorer modulaColorer = ServiceHolder.getInstance().getModulaColorer();
                        xss = modulaColorer.color(ctxLine);
                    } catch (Exception e) {
                        xss = new XStyledString(resourceRegistry);
                        xss.append(ctxLine);
                    }

                    // Appent it to sstring and mark matched symbol with border:
                    int matchPos = sstring.length() + match.getColumn() - 1 - trimL;
                    sstring.append(xss.convertToStyledString(dlgFont));
                    sstring.setStyle(matchPos, match.getSymbol().getName().length(), matchStyler);
                } else {
                    IModulaSymbol symbol = match.getSymbol();
                    sstring.append(symbol.getName(), matchStyler);
                }
            } 
            return sstring;
        }

        @Override
        public Image getImage(Object element) {
            Image image = null;
            
            IResource iRes = null;

            if (element instanceof IResource) {
                iRes = (IResource)element;
            } else if (element instanceof ModelItem) {
                IXdsElement xdsEl = ((ModelItem)element).getIXdsElement();
                if (xdsEl != null) {
                    if (xdsEl instanceof IXdsProject) {
                        image = ImageUtils.getImage(ImageUtils.M2_PRJ_FOLDER_TRANSP);
                    } else { 
                        image = XdsElementImages.getProjectElementImage(xdsEl);
                    }
                } else {
                    iRes = ((ModelItem)element).getIFile();
                }
            }

            if (iRes != null) {
                IXdsElement xdsElement = XdsModelManager.getModel().getXdsElement((IResource)element);
                if (xdsElement != null) {
                    image = XdsElementImages.getProjectElementImage(xdsElement);
                } else {
                    // hz. now there is no way to have file not from xds here. set no image 
                }
            }
            else if (element instanceof ModulaSymbolMatch) {
                ModulaSymbolMatch match = ((ModulaSymbolMatch)element);
                IModulaSymbol symbol = match.getSymbol();
                image = ModulaSymbolImages.getImage(symbol);
            } else if (element instanceof String) {
                image = ImageUtils.getImage(ImageUtils.PACKAGE_FRAGMENT_IMAGE_NAME);
            }
            return image;
        }

        @Override
        public void dispose() {
            super.dispose();
            fLabelProvider.dispose();
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return fLabelProvider.isLabelProperty(element, property);
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
            super.removeListener(listener);
            fLabelProvider.removeListener(listener);
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
            super.addListener(listener);
            fLabelProvider.addListener(listener);
        }

    }

}
