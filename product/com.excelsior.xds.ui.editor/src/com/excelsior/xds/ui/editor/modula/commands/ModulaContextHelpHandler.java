package com.excelsior.xds.ui.editor.modula.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.PlatformUI;

import com.excelsior.xds.ui.commons.utils.SelectionUtils;
import com.excelsior.xds.ui.commons.utils.WordAndRegion;
import com.excelsior.xds.ui.editor.modula.IModulaKeywords;

/**
 * A command handler to display help for Modula-2 keywords and pervasive identifier.
 */
public class ModulaContextHelpHandler extends AbstractHandler implements IHandler 
{
    public static final String M2_HELP_PLUGIN = "com.excelsior.xds.help.modula2"; //$NON-NLS-1$

    /**
     * Calls the help support system to display the context help of given 
     * Modual-2 pervasive identifier.
     * <p>
     * May only be called from a UI thread.
     * <p>
     * 
     * @param identifier
     *            the Modula-2 pervasive identifier to display
     */
    private static void displayModulaKeywordHelp(String word) {
        PlatformUI.getWorkbench().getHelpSystem().displayHelp(
                M2_HELP_PLUGIN + ".keyword_" + word    //$NON-NLS-1$   
        );
    }
    
    /**
     * Calls the help support system to display the context help of given 
     * Modual-2 pervasive identifier.
     * <p>
     * May only be called from a UI thread.
     * <p>
     * 
     * @param identifier
     *            the Modula-2 pervasive identifier to display
     */
    private static void displayModulaPervasiveIdHelp(String identifier) {
        PlatformUI.getWorkbench().getHelpSystem().displayHelp(
                M2_HELP_PLUGIN + ".pervasive_identifier_" + identifier    //$NON-NLS-1$
        );
    }

    
    /*
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		WordAndRegion wordUnderCursor = SelectionUtils.getWordUnderCursor(false);
		if (wordUnderCursor != null) {
			if (IModulaKeywords.KEYWORDS.contains(wordUnderCursor.word)) {
				displayModulaKeywordHelp(wordUnderCursor.word);
			}
			else if (IModulaKeywords.PERVASIVE_IDENTIFIERS.contains(wordUnderCursor.word)) {
				displayModulaPervasiveIdHelp(wordUnderCursor.word);
			}
			else {
				PlatformUI.getWorkbench().getHelpSystem().search(wordUnderCursor.word);
			}
		}
		return null;
	}
	
}
