package com.excelsior.xds.ui.editor.modula;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;

import com.excelsior.xds.core.ide.symbol.utils.EntityUtils;
import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.preferences.PreferenceKeys;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.utils.JavaUtils;
import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureDeclarationSymbol;
import com.excelsior.xds.parser.modula.utils.AstUtils;
import com.excelsior.xds.parser.modula.utils.ModulaSymbolUtils;
import com.excelsior.xds.ui.editor.commons.SourceCodeTextEditor;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.modula.utils.ModulaAstUtils;
import com.excelsior.xds.ui.editor.modula.utils.ModulaEditorSymbolUtils;

public class ModulaOccurrencesMarker {
    
    public static final String OCCURENCE_ANNOTATION_ID = "com.excelsior.xds.ui.editor.occurrenceMarkerAnnotation";  //$NON-NLS-1$
    public static final String WRITE_ANNOTATION_ID     = "com.excelsior.xds.ui.editor.writeOccurrenceMarkerAnnotation";  //$NON-NLS-1$

    public static final String CONSTRUCTION_ANNOTATION_ID = "com.excelsior.xds.ui.editor.constructionMarkerAnnotation";  //$NON-NLS-1$
    
    private final static HashSet<String> hsOccurrencesTypes; 
    private final static HashSet<String> hsConstructionTypes; 
    private final static HashSet<String> hsAllTypes; 
    static {
        hsOccurrencesTypes = new HashSet<String>(); 
        hsOccurrencesTypes.add(OCCURENCE_ANNOTATION_ID);
        hsOccurrencesTypes.add(WRITE_ANNOTATION_ID);
        
        hsConstructionTypes = new HashSet<String>(); 
        hsConstructionTypes.add(CONSTRUCTION_ANNOTATION_ID);
        
        hsAllTypes = new HashSet<String>(); 
        hsAllTypes.add(OCCURENCE_ANNOTATION_ID);
        hsAllTypes.add(WRITE_ANNOTATION_ID);
        hsAllTypes.add(CONSTRUCTION_ANNOTATION_ID);
    }


    final private MarkOccurencesJob markingJob;
    private boolean isOccurrenceMarkerOn = true;
    private boolean isConstructionMarkerOn = true;
    private ISourceViewer viewer;
    private SourceCodeTextEditor sourceEditor;
    private volatile int caretOffset = -1;

    
    private static final int DELAY_BEFORE_PROCESS_ACTIVITY = 200; // wait before process new cursor pos etc.
    
    public ModulaOccurrencesMarker() {
        markingJob = new MarkOccurencesJob();
        
        PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.addChangeListener(new IPreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent event) {
                if (PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.getKey().equals(event.getKey())) {
                    reReadStore();
                }
            }
        });
        
        PreferenceKeys.PKEY_HIGHLIGHT_CONSTRUCTIONS.addChangeListener(new IPreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent event) {
                if (PreferenceKeys.PKEY_HIGHLIGHT_CONSTRUCTIONS.getKey().equals(event.getKey())) {
                    reReadStore();
                }
            }
        });
        reReadStore();
    }

    public void setViewer(ISourceViewer viewer, SourceCodeTextEditor sourceEditor) {
        if (this.viewer != null) {
            LogHelper.logError("ModulaOccurrencesMarker.setViewer() is called more than one time!"); // $//$NON-NLS-1$
        } else {
            this.viewer = viewer;
            this.sourceEditor = sourceEditor;
            viewer.getTextWidget().addCaretListener(new CaretListener() {
                @Override
                public void caretMoved(CaretEvent event) {
                    caretOffset = event.caretOffset;
                    reMark(false);
                }
            });
        }
    }

    private void reReadStore() {
        boolean bOcc = PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.getStoredBoolean();
        boolean bCon = PreferenceKeys.PKEY_HIGHLIGHT_CONSTRUCTIONS.getStoredBoolean();
        if (isOccurrenceMarkerOn != bOcc || isConstructionMarkerOn != bCon) {
            isOccurrenceMarkerOn = bOcc;
            isConstructionMarkerOn = bCon;
            reMark(true);
        }
    }

    public void reconciled() {
        reMark(false);
    }


    private void reMark(boolean force) {
        if (sourceEditor != null && sourceEditor.getEditorInput() != null) {
            if (isOccurrenceMarkerOn || isConstructionMarkerOn || force) {
                markingJob.reMarkRequest(); 
            }
        }
    }

    /////------- JOB
    
    private class MarkOccurencesJob extends Job {
        private IModulaSymbol prevMarkedSymbol;
        
		public MarkOccurencesJob() {
            super(Messages.OccurrencesMarker_MarkingOccurrences);
        }

        public void reMarkRequest() {
            this.schedule(DELAY_BEFORE_PROCESS_ACTIVITY);
        }
        
        public IStatus run(IProgressMonitor monitor) {
            try {
            	if (sourceEditor.isDisposed()) {
            		return Status.OK_STATUS;
            	}
                if (!isOccurrenceMarkerOn) {
                    removeAnnotations(hsOccurrencesTypes);
                    prevMarkedSymbol = null;
                }
                if (!isConstructionMarkerOn) {
                    removeAnnotations(hsConstructionTypes);
                }
                
                if (!(isOccurrenceMarkerOn || isConstructionMarkerOn) || (caretOffset < 0)) {
                	return Status.OK_STATUS;
                }
                
                ModulaAst ast = ModulaAstUtils.getAstIfUpToDate(sourceEditor);
                if (ast == null) {
                    // remove marks and cross fingers for reconcile
                    removeAnnotations(hsAllTypes);
                    return Status.OK_STATUS;
                }

                IProject iProject = CoreEditorUtils.getIProjectFrom(sourceEditor.getEditorInput());
                
                IModulaSymbol symToMark = null;
                ArrayList<Region> constructionRegions = new ArrayList<Region>();
                String frameName = "";

                if (isOccurrenceMarkerOn) {
                    /// Try to find symbol to mark its occurrences:
                    try {
                        PstNode identNode = ModulaEditorSymbolUtils.getIdentifierPstLeafNode(ast, caretOffset);
                        if (identNode != null && identNode.getElementType() == ModulaTokenTypes.IDENTIFIER) {
                            String idTxt = sourceEditor.getDocumentProvider().getDocument(sourceEditor.getEditorInput()).get(identNode.getOffset(), identNode.getLength());
                            IModulaSymbol sym = ModulaSymbolUtils.getSymbol(identNode);
                            if (sym != null && sym.getName().equals(idTxt)) { // tree may be bad
                                symToMark = sym;
                            }
                        }
                    } catch (Exception e) {
                        // hz what may crash on bad tree
                        symToMark = null;
                    }
                    if (symToMark != null && symToMark == prevMarkedSymbol) {
                        return Status.OK_STATUS; // Old markers are valid. Do nothing.
                    }
                    prevMarkedSymbol = null;
                }

                if (isConstructionMarkerOn) {
                    // No symbol => try to find construction here:
                    if (symToMark == null) {
                        IAstFrameNode fr = ModulaAstUtils.findConstructionNear(ast, caretOffset, null);
                        if (fr != null) {
                            for (PstNode n : fr.getFrameNodes()) {
                                constructionRegions.add(new Region(n.getOffset(), n.getLength()));
                            }
                            frameName = fr.getFrameName();
                        }
                    }
                }
                
                Map<Annotation, Position> newAnnotations = null;
                if (symToMark != null) {
                	IModuleSymbol curModule = ast.getModuleSymbol();
                    Collection<TextPosition> symPositions = getRelatedUsages(iProject, ast, curModule, symToMark);
                    newAnnotations = createOccurrencesAnnotations(symToMark, symPositions);
                    prevMarkedSymbol = symToMark;
                } else if (!constructionRegions.isEmpty()){
                    String annotationTooltip = String.format(Messages.ModulaOccurrencesMarker_StructureOf, frameName);
                    newAnnotations = createConstructionAnnotations(constructionRegions, annotationTooltip);
                }
                replaceAnnotations(hsAllTypes, newAnnotations);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return Status.OK_STATUS;
        }
        
        private void removeAnnotations(Set<String> types) {
            replaceAnnotations(types, null);
        }
        
        private void replaceAnnotations(Set<String> types, Map<Annotation, Position> newAnnotations) {
            final Annotation[] remove = findExistentAnnotations(types);
            final IAnnotationModel model = viewer.getAnnotationModel();
            if (model instanceof IAnnotationModelExtension) {
                final IAnnotationModelExtension eModel = (IAnnotationModelExtension) model;
                eModel.replaceAnnotations(remove, newAnnotations);
            } else if (model != null) {
                for (final Annotation annotation : remove) {
                    model.removeAnnotation(annotation);
                }
                for (final Annotation annotation : newAnnotations.keySet()) {
                    model.addAnnotation(annotation, newAnnotations.get(annotation));
                }
            }
        }

        
        private Annotation[] findExistentAnnotations(Set<String> types) {
            final IAnnotationModel model = viewer.getAnnotationModel();
            final List<Annotation> annotations = new ArrayList<Annotation>();
            if (model != null) {
                final Iterator<?> it = model.getAnnotationIterator();
                while (it.hasNext()) {
                    final Annotation annotation = (Annotation) it.next();
                    String type = annotation.getType();
                    if (types.contains(type)) {
                        annotations.add(annotation);
                    }
                }
            }
            return annotations.toArray(new Annotation[annotations.size()]);
        }


        private Map<Annotation, Position> createOccurrencesAnnotations(IModulaSymbol symToMark, Collection<TextPosition> symPositions) {
            final Map<Annotation, Position> map = new HashMap<Annotation, Position>();
            if (symPositions != null && symToMark != null) {
                for (TextPosition pos : symPositions) {
                    String msg = String.format(Messages.OccurrencesMarker_OccurrenceOf, symToMark.getName());
                    Annotation annotation = new Annotation(OCCURENCE_ANNOTATION_ID, false, msg);
                    //TODO: write occurrences when need:
                    //String.format(Messages.OccurrencesMarker_WriteOccurrencesMarker, symToMark.getName());
                    //Annotation annotation = new Annotation(WRITE_ANNOTATION_ID, false, msg);
                    Position position = new Position(pos.getOffset(), symToMark.getName().length());
                    map.put(annotation, position);
                }
            }
            return map;
        }
        
        private Map<Annotation, Position> createConstructionAnnotations(ArrayList<Region> regions, String msg) {
            final Map<Annotation, Position> map = new HashMap<Annotation, Position>();
            for (Region reg : regions) {
                Annotation annotation = new Annotation(CONSTRUCTION_ANNOTATION_ID, false, msg);
                Position position = new Position(reg.getOffset(), reg.getLength());
                map.put(annotation, position);
            }
            return map;
        }
        
    } // class MarkingJob
    
    
    ////////////////////////////////////////////////////////////////
    //
    // Finding of occurrences of symbol:
    //
    ////////////////////////////////////////////////////////////////
    
    /**
	 * Returns usages (as positions) of given {@code symbol} in the given {@code module} plus some related usage :
	 * <ol>
	 * <li> For the symbol itself - position of its declaration, if also in the current module</li>
	 * <li> For the {@code IProcedureDefinitionSymbol} - also usages of the related  {@code IProcedureDeclarationSymbol}</li>
	 * <li> For the {@code IProcedureDeclarationSymbol}- also usages of forward declaration to this given {@code IProcedureDeclarationSymbol}</li>
	 * </ol>
     * @param ast 
	 * 
	 * @param module
	 * @param symbol
	 * @return
	 */
	private static Collection<TextPosition> getRelatedUsages(IProject project, ModulaAst ast, final IModuleSymbol module, IModulaSymbol startingSymbol) {
		Collection<TextPosition> usages = new HashSet<TextPosition>();
		
		if (module != null && startingSymbol != null) {
			Collection<IModulaSymbol> symbols = EntityUtils.syncGetRelatedSymbols(project, false, startingSymbol);
			for (IModulaSymbol symbol : symbols) {
				if (symbol == null) {
					continue;
				}
				if (JavaUtils.isOneOf(symbol, IProcedureDeclarationSymbol.class, IModuleSymbol.class)) {
					if (symbol.getPosition() != null && ModulaSymbolUtils.isSymbolFromModule(module, symbol)) {
						PstLeafNode identifierNode = ModulaEditorSymbolUtils.getIdentifierPstLeafNode(ast, symbol.getPosition().getOffset());
						List<PstNode> identifierNodes = AstUtils.getIdentifierNodesOfParent(identifierNode);
						if (identifierNodes.size() == 2) {
							PstNode identifier = identifierNodes.get(identifierNodes.size() - 1);
							usages.add(new TextPosition(0, 0, identifier.getOffset()));
						}
					}
				}
				
				addSymbolUsages(module, symbol, usages);
				addSymbolDefinitionPosition(module, symbol, usages);
			}
		}
		
        return usages;
	}
	
	private static void addSymbolDefinitionPosition(IModuleSymbol module, IModulaSymbol symbol, Collection<TextPosition> usages) {
		if (symbol.getPosition() != null && ModulaSymbolUtils.isSymbolFromModule(module, symbol)) {
			usages.add(symbol.getPosition());
		}
	}
	
	private static void addSymbolUsages(IModuleSymbol module, IModulaSymbol symbol, Collection<TextPosition> usages) {
		if (symbol != null) {
			Collection<TextPosition> symbolUsages = module.getSymbolUsages(symbol);
			if (symbolUsages != null) {
				usages.addAll(symbolUsages);
			}
		}
	}
    
}
