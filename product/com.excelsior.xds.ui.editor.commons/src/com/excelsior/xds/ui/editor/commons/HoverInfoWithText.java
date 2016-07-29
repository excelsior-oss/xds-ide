package com.excelsior.xds.ui.editor.commons;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.excelsior.xds.ui.commons.utils.XStyledString;

public class HoverInfoWithText implements IHoverInfo {

    // ---------- Hover data:

    private final XStyledString xsString;
    private boolean monospace;
    private int itemOffset; // -1 or position of the hovered item in the hover text. Used
                            // to scroll hover content to make it visible

    public HoverInfoWithText(XStyledString xsString, boolean monospace) {
        this(xsString, monospace, -1); 
    }

    public HoverInfoWithText(XStyledString xsString, boolean monospace, int itemOffset) {
        this.xsString = xsString;
        this.monospace = monospace;
        this.itemOffset = itemOffset;
    }

    // ---------- Hover painting and functionality:

    private StyledText fText;
    private Composite fParent;
    private HoverInformationControl fMIControl;

    @Override
    public void deferredCreateContent(Composite parent,
            HoverInformationControl miControl) {
        this.fParent = parent;
        this.fMIControl = miControl;

        fText = new StyledText(parent, SWT.MULTI | SWT.READ_ONLY
                | fMIControl.getAdditionalTextStyles());
        fText.setForeground(parent.getForeground());
        fText.setBackground(parent.getBackground());
        fText.setFont(JFaceResources.getDialogFont());
        fText.addPaintObjectListener(new PaintObjectListener() {
            public void paintObject(PaintObjectEvent event) {
                StyleRange style = event.style;
                Image image = (Image) style.data;
                if (image != null && !image.isDisposed()) {
                    int x = event.x + 2;
                    int y = event.y + event.ascent / 2 - style.metrics.ascent
                            / 2 + 2;
                    event.gc.drawImage(image, x, y);
                }
            }
        });
        FillLayout layout = new FillLayout();
        layout.marginHeight = 2;
        layout.marginWidth = 2;
        parent.setLayout(layout);

        if (monospace) {
            fText.setFont(JFaceResources.getTextFont());
        } else {
            fText.setFont(JFaceResources.getDialogFont());
        }

        fText.setText(xsString.getText());
        fText.setStyleRanges(xsString.getStyleRanges().toArray(new StyleRange[0]));

        parent.layout(true);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            if (fText != null) {
                if( fText.getWordWrap()) {
                    Shell shell = fParent.getShell();
                    Point currentSize = shell.getSize();
                    shell.pack(true);
                    Point newSize = shell.getSize();
                    if (newSize.x > currentSize.x || newSize.y > currentSize.y) {
                        shell.setSize(currentSize.x, currentSize.y); // restore
                                                                     // previous
                                                                     // size
                    }
                }
                // Scroll fText to make line with hovered object visible
                if (itemOffset >= 0) {
                    int topIdx = fText.getTopIndex();
                    int btmIdx = topIdx + fText.getSize().y / fText.getLineHeight();
                    btmIdx = Math.max(btmIdx-1, 0);

                    int line = fText.getLineAtOffset(itemOffset);
                    if (line > btmIdx) {
                        fText.setTopIndex(line - btmIdx);
                    }
                }
            }
        }
    }

}
