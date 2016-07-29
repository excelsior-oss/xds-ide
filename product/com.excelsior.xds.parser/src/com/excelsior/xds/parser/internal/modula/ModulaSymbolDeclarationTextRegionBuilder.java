package com.excelsior.xds.parser.internal.modula;

import com.excelsior.xds.core.text.TextRegion;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.internal.modula.symbol.ModulaSymbol;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.constants.AstConstantDeclaration;
import com.excelsior.xds.parser.modula.ast.imports.AstModuleAlias;
import com.excelsior.xds.parser.modula.ast.imports.AstSimpleImportStatement;
import com.excelsior.xds.parser.modula.ast.modules.AstFinallyBody;
import com.excelsior.xds.parser.modula.ast.modules.AstLocalModule;
import com.excelsior.xds.parser.modula.ast.modules.AstModule;
import com.excelsior.xds.parser.modula.ast.modules.AstModuleBody;
import com.excelsior.xds.parser.modula.ast.procedures.AstFormalParameter;
import com.excelsior.xds.parser.modula.ast.procedures.AstOberonMethodDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstOberonMethodForwardDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstOberonMethodReceiver;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedure;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureBody;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureDefinition;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureExternalSpecification;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureForwardDeclaration;
import com.excelsior.xds.parser.modula.ast.types.AstEnumElement;
import com.excelsior.xds.parser.modula.ast.types.AstRecordField;
import com.excelsior.xds.parser.modula.ast.types.AstRecordSimpleFieldBlock;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantFieldBlock;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantSelector;
import com.excelsior.xds.parser.modula.ast.types.AstTypeDeclaration;
import com.excelsior.xds.parser.modula.ast.variables.AstVariable;
import com.excelsior.xds.parser.modula.ast.variables.AstVariableDeclaration;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;


public class ModulaSymbolDeclarationTextRegionBuilder extends ModulaAstVisitor
{
    private TextRegion textRegion;
    
    @Override
    public boolean visit(AstModule node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return true;
    }
    
    @Override
    public boolean visit(AstLocalModule node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return true;
    }

    
    @Override
    public boolean visit(AstModuleBody node) {
        PstNode parent = node.getParent();
        if (parent instanceof AstModule) {
            textRegion = new TextRegion(parent.getOffset(), parent.getLength());
            setDeclarationTextRegion(node.getSymbol(), textRegion);
        }
        return false;
    }

    @Override
    public boolean visit(AstFinallyBody node) {
        PstNode parent = node.getParent();
        if (parent instanceof AstModule) {
            textRegion = new TextRegion(parent.getOffset(), parent.getLength());
            setDeclarationTextRegion(node.getSymbol(), textRegion);
        }
        return false;
    }


    @Override
    public boolean visit(AstSimpleImportStatement node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        return true;
    }
    
    @Override
    public boolean visit(AstModuleAlias node) {
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return false;
    }
    

    
    @Override
    public boolean visit(AstProcedureDefinition node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return true;
    }

    @Override
    public boolean visit(AstProcedureDeclaration node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return true;
    }

    @Override
    public boolean visit(AstProcedureForwardDeclaration node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return true;
    }
    
    @Override
    public boolean visit(AstProcedureExternalSpecification node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return false;
    }
    

    @Override
    public boolean visit(AstFormalParameter node) {
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return false;
    }
    
    @Override
    public boolean visit(AstProcedureBody node) {
        PstNode parent = node.getParent();
        if (parent instanceof AstProcedure) {
            textRegion = new TextRegion(parent.getOffset(), parent.getLength());
            setDeclarationTextRegion(node.getSymbol(), textRegion);
        }
        return false;
    }
    

    @Override
    public boolean visit(AstOberonMethodDeclaration node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return true;
    }

    @Override
    public boolean visit(AstOberonMethodForwardDeclaration node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return true;
    }

    @Override
    public boolean visit(AstOberonMethodReceiver node) {
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return false;
    }
    
    
    
    @Override
    public boolean visit(AstConstantDeclaration node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return true;
    }

    
    
    @Override
    public boolean visit(AstTypeDeclaration node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return true;
    }

    @Override
    public boolean visit(AstEnumElement node) {
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return false;
    }
    
    
    @Override
    public boolean visit(AstRecordSimpleFieldBlock node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        return true;
    }
    
    @Override
    public boolean visit(AstRecordField node) {
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return false;
    }
    
    @Override
    public boolean visit(AstRecordVariantFieldBlock node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        return true;
    }
    
    @Override
    public boolean visit(AstRecordVariantSelector node) {
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return false;
    }
    

    @Override
    public boolean visit(AstVariableDeclaration node) {
        textRegion = new TextRegion(node.getOffset(), node.getLength());
        return true;
    }

    @Override
    public boolean visit(AstVariable node) {
        setDeclarationTextRegion(node.getSymbol(), textRegion);
        return false;
    }

    
    
    private void setDeclarationTextRegion(IModulaSymbol symbol, TextRegion region) {
        if (symbol instanceof ModulaSymbol) {
            ((ModulaSymbol)symbol).setDeclarationTextRegion(region);
        }
    }
    
}
