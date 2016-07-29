package com.excelsior.xds.ui.refactoring.rename;

import java.io.IOException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.refactoring.rename.RenameRefactoringInfo;
import com.excelsior.xds.parser.commons.ast.TokenType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.scanner.jflex._XdsFlexScanner;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.SymbolAttribute;
import com.excelsior.xds.ui.internal.nls.Messages;

public class RenameRefactoringPage extends UserInputWizardPage {

	private Text txtNewName;
	private RenameRefactoringInfo refactoringInfo;

	public RenameRefactoringPage(RenameRefactoringInfo refactoringInfo) {
		super(RenameRefactoringPage.class.getName());
		this.refactoringInfo = refactoringInfo;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = createRootComposite( parent );
	    setControl( composite );

	    createLblNewName( composite );
	    createTxtNewName( composite );
	    validate();
	}
	
	// UI creation methods
	// ////////////////////

	private Composite createRootComposite(final Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		result.setLayout(gridLayout);
		initializeDialogUnits(result);
		Dialog.applyDialogFont(result);
		return result;
	}

	private void createLblNewName(final Composite composite) {
		Label lblNewName = new Label(composite, SWT.NONE);
		lblNewName.setText(Messages.RenameRefactoringPage_NewName);
	}

	private void createTxtNewName(Composite composite) {
		txtNewName = new Text(composite, SWT.BORDER);
		txtNewName.setText(refactoringInfo.getSelectedIdentifier());
		txtNewName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtNewName.selectAll();
		txtNewName.addKeyListener( new KeyAdapter() {
			public void keyReleased( final KeyEvent e ) {
				refactoringInfo.setNewName( txtNewName.getText() );
				validate();
			}
		} );
	}
	
	private void validate() {
	    String newName = txtNewName.getText();
	    
	    Boolean isPageComplete = null;
	    String errorMessage = null;
	    String warningMessage = getWarning();
	    if (!newName.isEmpty() && !newName.equals( refactoringInfo.getSelectedIdentifier() )) {
	    	 _XdsFlexScanner lexer = new _XdsFlexScanner(); 
	         lexer.reset(newName);
	         TokenType token;
	         try {
				while ((token = lexer.nextToken()) != ModulaTokenTypes.EOF) {
					if (isPageComplete != null) {
						isPageComplete = false; // input has several separate tokens, this is not correct
						errorMessage = Messages.RenameRefactoringPage_ErrorNameHasSeveralParts;
						break;
					}
					if (token != ModulaTokenTypes.IDENTIFIER) {
						isPageComplete = false;
						errorMessage = Messages.RenameRefactoringPage_ErrorNameIsNotCorrectModulaName;
						break;
					}
					isPageComplete = token == ModulaTokenTypes.IDENTIFIER;
				}
			} catch (IOException e) {
				LogHelper.logError(e);
			}
	         
	         IModulaSymbol symbol = refactoringInfo.getSymbolFromSelection();
	         ISymbolWithScope parentScope = symbol.getParentScope();
	         if (parentScope != null) {
	        	 IModulaSymbol symbolWithTheSameName = parentScope.resolveName(newName);
	        	 if (symbolWithTheSameName != null) {
	        		 warningMessage = Messages.RenameRefactoringPage_WarningNameIsAlreadyInScope;
	        	 }
	         }
	         
	    }
	    if (isPageComplete == null) {
	    	isPageComplete = false;
	    }
	    setPageComplete( isPageComplete );
	    setErrorMessage(errorMessage);
	    if (errorMessage == null) {
	    	setMessage(warningMessage, WARNING);
	    }
	  }

	private String getWarning() {
		String message = null;
		IModulaSymbol symbol = refactoringInfo.getSymbolFromSelection();
		if (symbol != null && symbol.isAttributeSet(SymbolAttribute.ALREADY_DEFINED)) {
			message = Messages.RenameRefactoringPage_WarningRenamingDuplicatedSymbol;
		}
		return message;
	}
}
