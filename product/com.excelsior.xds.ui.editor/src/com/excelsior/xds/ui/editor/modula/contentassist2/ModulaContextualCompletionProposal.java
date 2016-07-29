package com.excelsior.xds.ui.editor.modula.contentassist2;

import java.util.Objects;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;


/**
 * The standard implementation of the <code>ICompletionProposal</code> interface.
 */
public class ModulaContextualCompletionProposal implements ICompletionProposal, ICompletionProposalExtension6 {

    /** The string to be displayed in the completion proposal popup. */
    private StyledString fDisplaySString;
    /** The replacement string. */
    private String fReplacementString;
    /** The replacement offset. */
    protected int fReplacementOffset;
    /** The replacement length. */
    protected int fReplacementLength;
    /** The cursor position after this proposal has been applied. */
    protected int fCursorPosition;
    /** The image to be displayed in the completion proposal popup. */
    private Image fImage;
    /** The context information of this proposal. */
    private IContextInformation fContextInformation;
    /** The additional info of this proposal. */
    private String fAdditionalProposalInfo;

    /**
     * Creates a new completion proposal based on the provided information. The replacement string is
     * considered being the display string too. All remaining fields are set to <code>null</code>.
     *
     * @param replacementString the actual string to be inserted into the document
     * @param replacementOffset the offset of the text to be replaced
     * @param replacementLength the length of the text to be replaced
     * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
     */
    public ModulaContextualCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition) {
        this(replacementString, replacementOffset, replacementLength, cursorPosition, null, null, null, null);
    }

    /**
     * Creates a new completion proposal. All fields are initialized based on the provided information.
     *
     * @param replacementString the actual string to be inserted into the document
     * @param replacementOffset the offset of the text to be replaced
     * @param replacementLength the length of the text to be replaced
     * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
     * @param image the image to display for this proposal
     * @param displayString the string to be displayed for the proposal
     * @param contextInformation the context information associated with this proposal
     * @param additionalProposalInfo the additional information associated with this proposal
     */
    public ModulaContextualCompletionProposal(String replacementString, int replacementOffset, int replacementLength, 
            int cursorPosition, Image image, StyledString displaySString, IContextInformation contextInformation, String additionalProposalInfo) {
        Assert.isNotNull(replacementString);
        Assert.isTrue(replacementOffset >= 0);
        Assert.isTrue(replacementLength >= 0);
        Assert.isTrue(cursorPosition >= 0);

        fReplacementString= replacementString;
        fReplacementOffset= replacementOffset;
        fReplacementLength= replacementLength;
        fCursorPosition= cursorPosition;
        fImage= image;
        fDisplaySString= displaySString;
        fContextInformation= contextInformation;
        fAdditionalProposalInfo= additionalProposalInfo;
    }

    /*
     * @see ICompletionProposal#apply(IDocument)
     */
    public void apply(IDocument document) {
        try {
        	String replaced = document.get(fReplacementOffset, fReplacementLength);
        	if (!Objects.equals(replaced, fReplacementString)) {
        		document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
        	}
        } catch (BadLocationException x) {
            // ignore
        }
    }
    
    public boolean isNoChangeProposal(IDocument document) {
		try {
			String replaced = document.get(fReplacementOffset, fReplacementLength);
			return Objects.equals(replaced, fReplacementString);
		} catch (BadLocationException e) {
			// ignore
		}
    	return false;
    }

    /*
     * @see ICompletionProposal#getSelection(IDocument)
     */
    public Point getSelection(IDocument document) {
        return new Point(fReplacementOffset + fCursorPosition, 0);
    }

    /*
     * @see ICompletionProposal#getContextInformation()
     */
    public IContextInformation getContextInformation() {
        return fContextInformation;
    }

    /*
     * @see ICompletionProposal#getImage()
     */
    public Image getImage() {
        return fImage;
    }

    /*
     * @see ICompletionProposal#getDisplayString()
     */
    public String getDisplayString() {
        if (fDisplaySString != null)
            return fDisplaySString.getString();
        return fReplacementString;
    }

    /*
     * @see ICompletionProposal#getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo() {
        return fAdditionalProposalInfo;
    }

    @Override
    public StyledString getStyledDisplayString() {
        if (fDisplaySString != null)
            return fDisplaySString;
        return new StyledString(fReplacementString);
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fImage == null) ? 0 : fImage.hashCode());
		result = prime
				* result
				+ ((fReplacementString == null) ? 0 : fReplacementString
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModulaContextualCompletionProposal other = (ModulaContextualCompletionProposal) obj;
		if (fImage == null) {
			if (other.fImage != null)
				return false;
		} else if (!fImage.equals(other.fImage))
			return false;
		if (fReplacementString == null) {
			if (other.fReplacementString != null)
				return false;
		} else if (!fReplacementString.equals(other.fReplacementString))
			return false;
		return true;
	}
}
