/**
 * see org.eclipse.cdt.internal.ui.text.c.hover.AbstractAnnotationHover
 */
package com.excelsior.xds.ui.editor.modula.spellcheck.internal;

import java.text.MessageFormat;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;

import com.excelsior.xds.ui.editor.commons.HoverInformationControl;
import com.excelsior.xds.ui.editor.commons.IHoverInfo;

public class HoverInfoWithSpellingAnnotation implements IHoverInfo{
    //---------- Hover data:

    private final SpellingAnnotation fSpellingAnnotation;
    private final ITextViewer fViewer;
    private final int fOffset;

    public HoverInfoWithSpellingAnnotation(SpellingAnnotation spellingAnnotation, ITextViewer viewer, int offset) {
        this.fSpellingAnnotation = spellingAnnotation;
        this.fViewer = viewer;
        this.fOffset = offset;
    }
    
    
    //---------- Hover painting and functionality:
    
    private Composite fParent;
    private HoverInformationControl fMIControl;
    private DefaultMarkerAnnotationAccess fMarkerAnnotationAccess= new DefaultMarkerAnnotationAccess();
    
    
    @Override
    public void deferredCreateContent(Composite parent, HoverInformationControl miControl) 
    {
        this.fParent = parent;
        this.fMIControl = miControl;
        
        GridLayout layout= new GridLayout(1, false);
        layout.verticalSpacing= 0;
        layout.marginWidth= 0;
        layout.marginHeight= 0;
        fParent.setLayout(layout);

        // fillToolbar();
        
        createAnnotationInformation(fParent, fSpellingAnnotation);
        setColorAndFont(fParent, fParent.getForeground(), fParent.getBackground(), JFaceResources.getDialogFont());

        ICompletionProposal[] proposals= fSpellingAnnotation.getSpellingProblem().getProposals();
        if (proposals.length > 0)
            createCompletionProposalsControl(fParent, proposals);

        fParent.layout(true);
    }
    
    private void createAnnotationInformation(Composite parent, final Annotation annotation) {
        Composite composite= new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        GridLayout layout= new GridLayout(2, false);
        layout.marginHeight= 2;
        layout.marginWidth= 2;
        layout.horizontalSpacing= 0;
        composite.setLayout(layout);

        final Canvas canvas= new Canvas(composite, SWT.NO_FOCUS);
        GridData gridData= new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
        gridData.widthHint= 17;
        gridData.heightHint= 16;
        canvas.setLayoutData(gridData);
        canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                e.gc.setFont(null);
                fMarkerAnnotationAccess.paint(annotation, e.gc, canvas, new Rectangle(0, 0, 16, 16));
            }
        });

        StyledText text= new StyledText(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        GridData data= new GridData(SWT.FILL, SWT.FILL, true, true);
        text.setLayoutData(data);
        text.setText(annotation.getText());
    }
    
    private void setColorAndFont(Control control, Color foreground, Color background, Font font) {
        control.setForeground(foreground);
        control.setBackground(background);
        control.setFont(font);
        
        if (control instanceof Composite) {
            Control[] children= ((Composite) control).getChildren();
            for (Control element : children) {
                setColorAndFont(element, foreground, background, font);
            }
        }
    }

    private void createCompletionProposalsControl(Composite parent, ICompletionProposal[] proposals) {
        Composite composite= new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout2= new GridLayout(1, false);
        layout2.marginHeight= 0;
        layout2.marginWidth= 0;
        layout2.verticalSpacing= 2;
        composite.setLayout(layout2);

        Label separator= new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gridData= new GridData(SWT.FILL, SWT.CENTER, true, false);
        separator.setLayoutData(gridData);

        Label quickFixLabel= new Label(composite, SWT.NONE);
        GridData layoutData= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        layoutData.horizontalIndent= 4;
        quickFixLabel.setLayoutData(layoutData);
        String text;
        if (proposals.length == 1) {
            text= "1 quick fix available:";
        } else {
            text= MessageFormat.format("{0} quick fixes available:", new Object[] {String.valueOf(proposals.length)});
        }
        quickFixLabel.setText(text);

        setColorAndFont(composite, parent.getForeground(), parent.getBackground(), JFaceResources.getDialogFont());
        createCompletionProposalsList(composite, proposals);
    }

    private void createCompletionProposalsList(Composite parent, ICompletionProposal[] proposals) {
        final ScrolledComposite scrolledComposite= new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        GridData gridData= new GridData(SWT.FILL, SWT.FILL, true, true);
        scrolledComposite.setLayoutData(gridData);
        scrolledComposite.setExpandVertical(false);
        scrolledComposite.setExpandHorizontal(false);

        Composite composite= new Composite(scrolledComposite, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout= new GridLayout(3, false);
        layout.verticalSpacing= 2;
        composite.setLayout(layout);

        final Link[] links= new Link[proposals.length];
        for (int i= 0; i < proposals.length; i++) {
            Label indent= new Label(composite, SWT.NONE);
            GridData gridData1= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
            gridData1.widthHint= 0;
            indent.setLayoutData(gridData1);

            links[i] = createCompletionProposalLink(composite, proposals[i]); 
        }

        scrolledComposite.setContent(composite);
        setColorAndFont(scrolledComposite, parent.getForeground(), parent.getBackground(), JFaceResources.getDialogFont());
        
        Point contentSize= composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        composite.setSize(contentSize);
        
        Point constraints= fMIControl.getInfoSizeConstraints();
        if (constraints != null && contentSize.x < constraints.x) {
            ScrollBar horizontalBar= scrolledComposite.getHorizontalBar();
            
            int scrollBarHeight;
            if (horizontalBar == null) {
                Point scrollSize= scrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                scrollBarHeight= scrollSize.y - contentSize.y;
            } else {
                scrollBarHeight= horizontalBar.getSize().y;
            }
            gridData.heightHint= contentSize.y - scrollBarHeight;
        }

        for (int i= 0; i < links.length; i++) {
            final int index= i;
            final Link link= links[index];
            link.addKeyListener(new KeyListener() {
                public void keyPressed(KeyEvent e) {
                    switch (e.keyCode) {
                        case SWT.ARROW_DOWN:
                            if (index + 1 < links.length) {
                                links[index + 1].setFocus();
                            }
                            break;
                        case SWT.ARROW_UP:
                            if (index > 0) {
                                links[index - 1].setFocus();
                            }
                            break;
                        default:
                            break;
                    }
                }

                public void keyReleased(KeyEvent e) {
                }
            });

            link.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    int currentPosition= scrolledComposite.getOrigin().y;
                    int hight= scrolledComposite.getSize().y;
                    int linkPosition= link.getLocation().y;

                    if (linkPosition < currentPosition) {
                        if (linkPosition < 10)
                            linkPosition= 0;

                        scrolledComposite.setOrigin(0, linkPosition);
                    } else if (linkPosition + 20 > currentPosition + hight) {
                        scrolledComposite.setOrigin(0, linkPosition - hight + link.getSize().y);
                    }
                }

                public void focusLost(FocusEvent e) {
                }
            });
        }
    }
    
    private Link createCompletionProposalLink(Composite parent, final ICompletionProposal proposal) {
        Label proposalImage= new Label(parent, SWT.NONE);
        proposalImage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        Image image= proposal.getImage();
        if (image != null) {
            proposalImage.setImage(image);

            proposalImage.addMouseListener(new MouseListener() {

                public void mouseDoubleClick(MouseEvent e) {
                }

                public void mouseDown(MouseEvent e) {
                }

                public void mouseUp(MouseEvent e) {
                    if (e.button == 1) {
                        apply(proposal);
                    }
                }

            });
        }

        Link proposalLink= new Link(parent, SWT.WRAP);
        proposalLink.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        proposalLink.setText("<a>" + proposal.getDisplayString() + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
        proposalLink.addSelectionListener(new SelectionAdapter() {
            /*
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                apply(proposal);
            }
        });

        return proposalLink;
    }

    private void apply(ICompletionProposal p) {
        //Focus needs to be in the text viewer, otherwise linked mode does not work
        fMIControl.dispose();

        IRewriteTarget target= null;
        try {
            IDocument document= fViewer.getDocument();

            if (fViewer instanceof ITextViewerExtension) {
                ITextViewerExtension extension= (ITextViewerExtension) fViewer;
                target= extension.getRewriteTarget();
            }

            if (target != null)
                target.beginCompoundChange();

            if (p instanceof ICompletionProposalExtension2) {
                ICompletionProposalExtension2 e= (ICompletionProposalExtension2) p;
                e.apply(fViewer, (char) 0, SWT.NONE, fOffset);
            } else if (p instanceof ICompletionProposalExtension) {
                ICompletionProposalExtension e= (ICompletionProposalExtension) p;
                e.apply(document, (char) 0, fOffset);
            } else {
                p.apply(document);
            }

            Point selection= p.getSelection(document);
            if (selection != null) {
                fViewer.setSelectedRange(selection.x, selection.y);
                fViewer.revealRange(selection.x, selection.y);
            }
        } finally {
            if (target != null)
                target.endCompoundChange();
        }
    }
    
    @Override
    public void setVisible(boolean visible) {
    }

}
