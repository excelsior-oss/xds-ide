package com.excelsior.xds.core.model.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.excelsior.xds.core.model.IXdsCompositeType;
import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElementWithSymbol;
import com.excelsior.xds.core.model.IXdsRecordField;
import com.excelsior.xds.core.model.IXdsRecordType;
import com.excelsior.xds.core.model.IXdsRecordVariant;
import com.excelsior.xds.core.model.IXdsType;
import com.excelsior.xds.core.model.ProcedureType;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.text.TextRegion;
import com.excelsior.xds.parser.commons.ast.AstNode;
import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.ast.IElementType;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstDeclarations;
import com.excelsior.xds.parser.modula.ast.AstModuleName;
import com.excelsior.xds.parser.modula.ast.AstQualifiedName;
import com.excelsior.xds.parser.modula.ast.AstSymbolRef;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.constants.AstConstantDeclaration;
import com.excelsior.xds.parser.modula.ast.imports.AstImportAliasDeclaration;
import com.excelsior.xds.parser.modula.ast.imports.AstImportFragment;
import com.excelsior.xds.parser.modula.ast.imports.AstModuleAlias;
import com.excelsior.xds.parser.modula.ast.imports.AstSimpleImportFragment;
import com.excelsior.xds.parser.modula.ast.imports.AstUnqualifiedImportFragment;
import com.excelsior.xds.parser.modula.ast.imports.AstUnqualifiedImportStatement;
import com.excelsior.xds.parser.modula.ast.modules.AstFinallyBody;
import com.excelsior.xds.parser.modula.ast.modules.AstLocalModule;
import com.excelsior.xds.parser.modula.ast.modules.AstModule;
import com.excelsior.xds.parser.modula.ast.modules.AstModuleBody;
import com.excelsior.xds.parser.modula.ast.procedures.AstFormalParameter;
import com.excelsior.xds.parser.modula.ast.procedures.AstFormalParameterBlock;
import com.excelsior.xds.parser.modula.ast.procedures.AstOberonMethod;
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
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariant;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantElsePart;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantFieldBlock;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantLabel;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantSelector;
import com.excelsior.xds.parser.modula.ast.types.AstTypeDeclaration;
import com.excelsior.xds.parser.modula.ast.types.AstTypeDef;
import com.excelsior.xds.parser.modula.ast.types.AstTypeElement;
import com.excelsior.xds.parser.modula.ast.variables.AstVariable;
import com.excelsior.xds.parser.modula.symbol.IConstantSymbol;
import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;
import com.excelsior.xds.parser.modula.symbol.IFinallyBodySymbol;
import com.excelsior.xds.parser.modula.symbol.IFormalParameterSymbol;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleAliasSymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleBodySymbol;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodReceiverSymbol;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodSymbol;
import com.excelsior.xds.parser.modula.symbol.IProcedureSymbol;
import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;
import com.excelsior.xds.parser.modula.symbol.IRecordVariantSelectorSymbol;
import com.excelsior.xds.parser.modula.symbol.ISymbolWithScope;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;
import com.excelsior.xds.parser.modula.symbol.reference.ReferenceUtils;
import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ISetTypeSymbol;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class XdsCompilationUnitBuilder
{
    private static final String UNKNOWN_NAME_CAPTION = "<<Unknown name>>";
    
    private final Map<IModulaSymbol, IXdsElementWithSymbol> symbol2XdsElement;
    private XdsModule moduleElement;
    private final XdsCompilationUnit compilationUnit;
    private final ModulaAst ast;
    
    public XdsCompilationUnitBuilder( Map<IModulaSymbol
                                    , IXdsElementWithSymbol> symbol2XdsElement
                                    , XdsCompilationUnit compilationUnit
                                    , ModulaAst ast )
    {
        this.symbol2XdsElement = symbol2XdsElement;
        this.compilationUnit = compilationUnit;
        this.ast = ast;
    }
    
    public void buildUnitStructure() {
        buildUnitStructure(ast);
    }
    
    private void buildUnitStructure(ModulaAst ast) {
        ast.accept(new DeclarationAstVisitor());
    }
    
    private SourceBinding createSourceBinding(AstNode astNode) {
        ITextRegion elementRegion    = null;
        ITextRegion identifierRegion = null;

        if ((astNode instanceof AstSymbolRef<?>) && !(astNode instanceof AstImportFragment)) 
        {
            IModulaSymbol symbol = ((AstSymbolRef<?>)astNode).getSymbol();
            if (symbol != null) {
                identifierRegion = symbol.getNameTextRegion();
            }
        }
        
        if (elementRegion == null) {
            elementRegion = new TextRegion(astNode.getOffset(), astNode.getLength());
        }

        if (identifierRegion == null) {
            if (astNode instanceof IAstNodeWithIdentifier) {
                PstNode identifier = ((IAstNodeWithIdentifier)astNode).getIdentifier();
                identifierRegion = identifier != null 
                                 ? new TextRegion(identifier.getOffset(), identifier.getLength()) 
                                 : elementRegion;
            }
            
        }
        return new SourceBinding(elementRegion, identifierRegion);
    }
    
    private <T extends IModulaSymbol> String getName(AstSymbolRef<T> astSymbolRef) {
        if (astSymbolRef == null) {
            return UNKNOWN_NAME_CAPTION;
        }
        String name = astSymbolRef.getName();
        if (StringUtils.isEmpty(name)) {
            if (astSymbolRef instanceof IAstNodeWithIdentifier) {
                IAstNodeWithIdentifier astIdentifier = (IAstNodeWithIdentifier) astSymbolRef;
                PstNode identifier = astIdentifier.getIdentifier();
                if (identifier != null) {
                    int offset = identifier.getOffset();
                    int length = identifier.getLength();
                    name = ""+ast.getChars().subSequence(offset, offset + length);
                }
            }
        }
        return name;
    }
    
    public String getName(IModulaSymbol symbol) {
        return symbol != null? symbol.getName() : UNKNOWN_NAME_CAPTION;
    }
    
    private final class DeclarationAstVisitor extends ModulaAstVisitor
    {
        Map<AstUnqualifiedImportStatement, XdsUnqualifiedImportContainer> unqualifiedImportStatement2ImportStatementSection = new HashMap<AstUnqualifiedImportStatement, XdsUnqualifiedImportContainer>();

        @Override
        public boolean visit(AstModule astNode) {
            addModuleElement(astNode);
            return true;
        }

        @Override
        public boolean visit(AstLocalModule astNode) {
            addModuleElement(astNode);
            return true;
        }

        @Override
        public boolean visit(AstProcedureDefinition astNode) {
            addProcedureElement(astNode);
            return false;
        }

        @Override
        public boolean visit(AstProcedureExternalSpecification astNode) {
            addProcedureElement(astNode);
            return false;
        }
        
        @Override
		public boolean visit(AstProcedureForwardDeclaration astNode) {
        	addProcedureElement(astNode, true);
        	return true;
		}

		@Override
        public boolean visit(AstProcedureDeclaration astNode) {
            addProcedureElement(astNode);
            return true;
        }

        @Override
        public boolean visit(AstModuleBody astNode) {
            addModuleBodyElement(astNode);
            return true;
        }

        @Override
        public boolean visit(AstFinallyBody astNode) {
            addFinallyBodyElement(astNode);
            return true;
        }

        @Override
        public boolean visit(AstOberonMethodDeclaration astNode) {
            addOberonMethodElement(astNode, false);
            return true;
        }

        @Override
        public boolean visit(AstOberonMethodForwardDeclaration astNode) {
            addOberonMethodElement(astNode, true);
            return true;
        }

        @Override
        public boolean visit(AstVariable node) {
            addVariableElement(node);
            return true;
        }

        @Override
        public boolean visit(AstConstantDeclaration astNode) {
            addConstantElement(astNode);
            return true;
        }

        @Override
        public boolean visit(AstSimpleImportFragment astNode) {
            SourceBinding sourceBinding = createSourceBinding(astNode);
            if (moduleElement == null) { // AST completely messed-up
            	return false;
            }
            
            XdsImportSection importSection = moduleElement.getOrCreateImportSection(createSourceBinding(astNode.getImports()));
            AstModuleName astModuleName = astNode.getAstModuleName();
            if (astModuleName != null) {
                IModuleSymbol symbol = astModuleName.getSymbol();
                String name = getName(astNode);
                
                XdsQualifiedImportElement qualifiedImportElement = new XdsQualifiedImportElement(
                        name, compilationUnit.getXdsProject(), compilationUnit,
                        importSection, ReferenceUtils.createRef(symbol), sourceBinding);
                importSection.addImportElement(qualifiedImportElement);
                
            }
            else {
                AstImportAliasDeclaration importAliasDeclaration = astNode.getImportAliasDeclaration();
                
                astModuleName = importAliasDeclaration.getAstModuleName();
                
                IModuleSymbol symbol = null;
                if (astModuleName != null) {
                    symbol = astModuleName.getSymbol();
                }
                AstModuleAlias moduleAlias = importAliasDeclaration.getModuleAlias();
                IModuleAliasSymbol aliasSymbol = null;
                
                String name = "";
                if (moduleAlias != null && moduleAlias.getSymbol() != null) {
                    aliasSymbol = moduleAlias.getSymbol();
                    name = getName(aliasSymbol);  
                }
                
                sourceBinding = createSourceBinding(importAliasDeclaration);
                
                XdsAliasQualifiedImportElement qualifiedImportElement = new XdsAliasQualifiedImportElement(
                        name, compilationUnit.getXdsProject(), compilationUnit,
                        importSection, ReferenceUtils.createRef(symbol), ReferenceUtils.createRef(aliasSymbol), sourceBinding);
                importSection.addImportElement(qualifiedImportElement);
            }
            
            return true;
        }

        @Override
        public boolean visit(AstUnqualifiedImportFragment astNode) {
        	if (moduleElement == null) { // AST completely messed-up
        		return false;
        	}
        	 
            XdsImportSection importSection = moduleElement.getOrCreateImportSection(createSourceBinding(astNode.getImports()));
            
            AstUnqualifiedImportStatement unqualifiedImportStatement = astNode.getUnqualifiedImportStatement();
            if (unqualifiedImportStatement != null) {
                XdsUnqualifiedImportContainer importStatementSection = unqualifiedImportStatement2ImportStatementSection.get(unqualifiedImportStatement);
                if (importStatementSection == null) {
                    String name = getName(unqualifiedImportStatement.getModuleIdentifier());
                    SourceBinding sourceBinding = createSourceBinding(unqualifiedImportStatement);
                    importStatementSection = new XdsUnqualifiedImportContainer(name, compilationUnit.getXdsProject(), importSection, sourceBinding);
                    importSection.addImportElement(importStatementSection);
                    unqualifiedImportStatement2ImportStatementSection.put(unqualifiedImportStatement, importStatementSection);
                }
                
                String name = getName(astNode);
                SourceBinding sourceBinding = createSourceBinding(astNode);
                
                XdsUnqualifiedImportElement unqualifiedImportElement = new XdsUnqualifiedImportElement(name, compilationUnit.getXdsProject(), compilationUnit, importStatementSection, ReferenceUtils.createRef(astNode.getSymbol()), sourceBinding);
                importStatementSection.addImportedElement(unqualifiedImportElement);
            }
            
            return true;
        }

        @Override
        public boolean visit(AstTypeDeclaration astNode) {
            ITypeSymbol symbol = astNode.getSymbol();
            if (symbol != null) {
                IXdsContainer parentXdsElement = (IXdsContainer)symbol2XdsElement.get(symbol.getParentScope());
                String typeElementName = getName(astNode);
                SourceBinding sourceBinding = createSourceBinding(astNode);
                IXdsType xdsType = processCompositeType( parentXdsElement
                                                       , astNode.getAstTypeElement()
                                                       , typeElementName, sourceBinding );
                if (xdsType == null) {
                    xdsType = new XdsType( compilationUnit.getXdsProject() 
                                         , compilationUnit, parentXdsElement 
                                         , typeElementName, ReferenceUtils.createRef(symbol), sourceBinding );
                }
                symbol2XdsElement.put(xdsType.getSymbol(), xdsType);
                if (parentXdsElement instanceof XdsElementWithDefinitions) {
                    ((XdsElementWithDefinitions)parentXdsElement).addType(xdsType);
                }
            }
            return true;
        }

        private void addModuleElement(AstModule astNode) {
            IModuleSymbol symbol = astNode.getSymbol();
            if (symbol != null) {
                SourceBinding sourceBinding = createSourceBinding(astNode);
                IXdsContainer parentXdsElement = (IXdsContainer)symbol2XdsElement.get(symbol.getParentScope());
                XdsModule moduleElement = new XdsModule(compilationUnit.getXdsProject(), compilationUnit, parentXdsElement, getName(astNode), ReferenceUtils.createRef(symbol), sourceBinding);
                if (parentXdsElement != null ) {
                    XdsElementWithDefinitions xdsElemWithDefinitions = (XdsElementWithDefinitions) parentXdsElement;
                    xdsElemWithDefinitions.addLocalModule(moduleElement);
                }
                else {
                    XdsCompilationUnitBuilder.this.moduleElement = moduleElement;
                    compilationUnit.setModuleElement(moduleElement);
                }
                symbol2XdsElement.put(symbol, moduleElement);
            }
        }

        private void addModuleBodyElement(AstModuleBody astNode) {
            IModuleBodySymbol symbol = astNode.getSymbol();
            SourceBinding sourceBinding = createSourceBinding(astNode);
            if (symbol != null) {
                ISymbolWithScope parentScope = symbol.getParentScope();
                IXdsContainer parentXdsElement = (IXdsContainer)symbol2XdsElement.get(parentScope);
                if (parentXdsElement != null && parentScope != null) {
                    XdsProcedure procedureElement = new XdsProcedure(
                            ProcedureType.BEGIN_BODY,
                            compilationUnit.getXdsProject(),
                            compilationUnit,
                            parentXdsElement,
                            getName(symbol) + " " + getName(parentScope),
                            ReferenceUtils.createRef((IProcedureSymbol)symbol), sourceBinding, false);
                    
                    XdsElementWithDefinitions xdsElemWithDefinitions = (XdsElementWithDefinitions) parentXdsElement;
                    xdsElemWithDefinitions.addProcedure(procedureElement);
                    symbol2XdsElement.put(symbol, procedureElement);
                }
            }
        }

        private void addFinallyBodyElement(AstFinallyBody astNode) {
            IFinallyBodySymbol symbol = astNode.getSymbol();
            if (symbol != null) {
                SourceBinding sourceBinding = createSourceBinding(astNode);
                IXdsContainer parentXdsElement = (IXdsContainer)symbol2XdsElement.get(symbol.getParentScope());
                
                if (parentXdsElement != null ) {
                    XdsProcedure procedureElement = new XdsProcedure(
                            ProcedureType.FINALLY_BODY,
                            compilationUnit.getXdsProject(),
                            compilationUnit,
                            parentXdsElement,
                            symbol.getName() + " " + symbol.getParentScope().getName(),
                            ReferenceUtils.createRef((IProcedureSymbol)symbol), sourceBinding, false);
                    
                    XdsElementWithDefinitions xdsElemWithDefinitions = (XdsElementWithDefinitions) parentXdsElement;
                    xdsElemWithDefinitions.addProcedure(procedureElement);
                    symbol2XdsElement.put(symbol, procedureElement);
                }
            }
        }
        
        private void addProcedureElement(AstProcedure<? extends IProcedureSymbol> astNode) {
        	addProcedureElement(astNode, false);
        }

        private void addProcedureElement(AstProcedure<? extends IProcedureSymbol> astNode, boolean isForwardDeclaration) {
            IProcedureSymbol symbol = astNode.getSymbol();
            if (symbol != null) {
                SourceBinding sourceBinding = createSourceBinding(astNode);
                IXdsContainer parentXdsElement = (IXdsContainer)symbol2XdsElement.get(symbol.getParentScope());
                
                if (parentXdsElement != null ) {
                    XdsProcedure procedureElement = new XdsProcedure(
                            ProcedureType.MODULA,
                            compilationUnit.getXdsProject(),
                            compilationUnit,
                            parentXdsElement,
                            getName(astNode),
                            ReferenceUtils.createRef(symbol), sourceBinding, isForwardDeclaration );
                    
                    XdsElementWithDefinitions xdsElemWithDefinitions = (XdsElementWithDefinitions) parentXdsElement;
                    xdsElemWithDefinitions.addProcedure(procedureElement);

                    symbol2XdsElement.put(symbol, procedureElement);
                    AstFormalParameterBlock astFormalParameterBlock = astNode.getProcedureParameters();
                    if (astFormalParameterBlock != null) {
                        astFormalParameterBlock.accept(new FormalParameterVisitor(procedureElement));
                    }
                }
            }
        }

        private void addOberonMethodElement(AstOberonMethod<? extends IOberonMethodSymbol> astNode, boolean isForwardDeclaration) {
            IOberonMethodSymbol symbol = astNode.getSymbol();
            if (symbol != null) {
                IOberonMethodReceiverSymbol receiverSymbol = symbol.getReceiverSymbol();
                IRecordTypeSymbol boundTypeSymbol;
                if (receiverSymbol != null && (boundTypeSymbol = receiverSymbol.getBoundTypeSymbol()) != null) {
                	SourceBinding sourceBinding = createSourceBinding(astNode);
                	IXdsContainer parentXdsElement = (IXdsContainer)symbol2XdsElement.get(boundTypeSymbol);
                	if (parentXdsElement != null ) {
                		XdsProcedure procedureElement = new XdsProcedure(
                				ProcedureType.OBERON,
                				compilationUnit.getXdsProject(),
                				compilationUnit,
                				parentXdsElement,
                				getName(astNode),
                				ReferenceUtils.createRef((IProcedureSymbol)symbol), sourceBinding, isForwardDeclaration);
                		
                		XdsRecordType xdsRecordType = (XdsRecordType) parentXdsElement;
                		xdsRecordType.addProcedure(procedureElement);
                		symbol2XdsElement.put(symbol, procedureElement);
                		
                		astNode.accept(new FormalParameterVisitor(procedureElement));
                	}
                }
            }
        }

        private void addVariableElement(AstVariable node) {
            IVariableSymbol symbol = node.getSymbol();
            if (symbol != null) {
                SourceBinding sourceBinding = createSourceBinding(node);
                
                ISymbolWithScope parentScope = symbol.getParentScope();
                IXdsContainer parentXdsElement = (IXdsContainer)symbol2XdsElement.get(parentScope);
                if (parentXdsElement != null && parentScope != null) {
                    XdsVariable xdsVarElem = new XdsVariable(compilationUnit.getXdsProject(),
                            compilationUnit, getName(node), parentXdsElement, ReferenceUtils.createRef(symbol),
                            sourceBinding);
                    XdsElementWithDefinitions xdsElemWithDefinitions = (XdsElementWithDefinitions) parentXdsElement;
                    xdsElemWithDefinitions.addVariable(xdsVarElem);
                    
                    processAnonymousCompositeType(xdsVarElem, node.getAstTypeElement());
                }
            }
        }

        private void addConstantElement(AstConstantDeclaration astNode) {
            IConstantSymbol symbol = astNode.getSymbol();
            if (symbol != null) {
                SourceBinding sourceBinding = createSourceBinding(astNode);
                IXdsContainer parentXdsElement = (IXdsContainer)symbol2XdsElement.get(symbol.getParentScope());
                if (parentXdsElement != null ) {
                    XdsConstant<IConstantSymbol> xdsCosntElem = new XdsConstant<IConstantSymbol>(compilationUnit.getXdsProject(), compilationUnit, getName(astNode), parentXdsElement, ReferenceUtils.createRef(symbol), sourceBinding);
                    XdsElementWithDefinitions xdsElemWithDefinitions = (XdsElementWithDefinitions) parentXdsElement;
                    xdsElemWithDefinitions.addConstant(xdsCosntElem);
                }
            }
        }

    }

    
    private <T extends IModulaSymbol>
    void processAnonymousCompositeType( XdsElementWithAnonymusCompositeType<T> parentXdsElement
                                      , AstTypeElement astTypeElement )
    {
        IXdsCompositeType xdsAnonymousType = processCompositeType( parentXdsElement
                                                                 , astTypeElement
                                                                 , null, null );
        if (xdsAnonymousType != null) {
            parentXdsElement.setAnonymousType(xdsAnonymousType);
        }
    }

    private IXdsCompositeType processCompositeType( IXdsContainer parentXdsElement
                                                  , AstTypeElement astTypeElement
                                                  , String typeElementName
                                                  , SourceBinding sourceBinding ) 
    {
        IXdsCompositeType xdsCompositeType = null;
        if (astTypeElement != null) {
            AstTypeDef<? extends ITypeSymbol> astTypeDefinition = astTypeElement.getTypeDefinition();
            if (astTypeDefinition != null) {
                ITypeSymbol typeSymbol = astTypeDefinition.getSymbol();
                if (sourceBinding == null) {
                    sourceBinding = createSourceBinding(astTypeDefinition);
                }
                if (typeElementName == null) {
                    typeElementName = getName(astTypeDefinition);
                }

                IElementType elementType = astTypeDefinition.getElementType();
                if (elementType == ModulaElementTypes.RECORD_TYPE_DEFINITION) {
                    xdsCompositeType = createXdsRecordType(parentXdsElement, astTypeDefinition, typeElementName, true, (IRecordTypeSymbol)typeSymbol, sourceBinding);
                }
                else if (elementType == ModulaElementTypes.POINTER_TYPE_DEFINITION) {
                	IPointerTypeSymbol pointerTypeSymbol = (IPointerTypeSymbol)typeSymbol;
                	ITypeSymbol boundTypeSymbol = pointerTypeSymbol.getBoundTypeSymbol();
                	if (boundTypeSymbol instanceof IRecordTypeSymbol) {
						IRecordTypeSymbol recordTypeSymbol = (IRecordTypeSymbol) boundTypeSymbol;
						xdsCompositeType = createXdsRecordType(parentXdsElement, astTypeDefinition, typeElementName, false, recordTypeSymbol, sourceBinding);
					}
                }
                else if (elementType == ModulaElementTypes.ENUMERATION_TYPE_DEFINITION){
                	IEnumTypeSymbol enumTypeSymbol = (IEnumTypeSymbol) typeSymbol;
                    XdsEnum enumType = new XdsEnum(compilationUnit.getXdsProject(),
                            compilationUnit, parentXdsElement,
                            typeElementName, ReferenceUtils.createRef(enumTypeSymbol),
                            sourceBinding);
                    astTypeDefinition.accept(new EnumerationElementVisitor(enumType));
                    xdsCompositeType = enumType;
                }
                else if (elementType == ModulaElementTypes.SET_TYPE_DEFINITION){
                	ISetTypeSymbol setTypeSymbol = (ISetTypeSymbol) typeSymbol;
                	XdsSet setType = new XdsSet(getName(setTypeSymbol), compilationUnit.getXdsProject(),
                            compilationUnit, parentXdsElement, ReferenceUtils.createRef(setTypeSymbol),
                            sourceBinding);
                	astTypeDefinition.accept(new SetElementVisitor(setType));
                	xdsCompositeType = setType;
                }
            }
        }
        return xdsCompositeType;
    }
    
	private XdsRecordType createXdsRecordType(IXdsContainer parentXdsElement,
			AstTypeDef<? extends ITypeSymbol> astTypeDefinition,
			String typeElementName, boolean isUseNameFromSymbol, IRecordTypeSymbol typeSymbol,
			SourceBinding sourceBinding) {
		XdsRecordType recordType = new XdsRecordType(
				compilationUnit.getXdsProject(), compilationUnit,
				parentXdsElement, typeElementName, isUseNameFromSymbol,
				ReferenceUtils.createRef((IRecordTypeSymbol) typeSymbol),
				sourceBinding);
		astTypeDefinition.accept(new RecordFieldVisitor(recordType));
		return recordType;
	}
	
    private final class FormalParameterVisitor extends ModulaAstVisitor
    {
        private final XdsProcedure xdsProcedure;
        
        private FormalParameterVisitor(XdsProcedure xdsProcedure) {
            this.xdsProcedure = xdsProcedure;
        }
        
        @Override
        public boolean visit(AstFormalParameter astNode) {
            IFormalParameterSymbol symbol = astNode.getSymbol();
            SourceBinding sourceBinding = createSourceBinding(astNode);
            XdsFormalParameter xdsFormalParameter = new XdsFormalParameter(
                    compilationUnit.getXdsProject(), compilationUnit, 
                    xdsProcedure, getName(astNode), ReferenceUtils.createRef(symbol), sourceBinding );
            xdsProcedure.addParameter(xdsFormalParameter);
            symbol2XdsElement.put(symbol, xdsFormalParameter);
            return false;
        }
        
        @Override
        public boolean visit(AstOberonMethodReceiver astNode) {
            IOberonMethodReceiverSymbol symbol = astNode.getSymbol();
            SourceBinding sourceBinding = createSourceBinding(astNode);
            XdsOberonMethodReceiver xdsOberonMethodReceiver = new XdsOberonMethodReceiver(
                    compilationUnit.getXdsProject(), compilationUnit, xdsProcedure,
                    getName(astNode), ReferenceUtils.createRef(symbol), sourceBinding );
            xdsProcedure.addParameter(xdsOberonMethodReceiver);
            symbol2XdsElement.put(symbol, xdsOberonMethodReceiver);
            return false;
        }
        
        @Override
        public boolean visit(AstDeclarations astNode) {
            return false;
        }
        
        @Override
        public boolean visit(AstProcedureBody astNode) {
            return false;
        }
    }
    
    
    private final class EnumerationElementVisitor extends ModulaAstVisitor
    {
        private final XdsEnum enumType;

        private EnumerationElementVisitor(XdsEnum enumType) {
            this.enumType = enumType;
        }

        @Override
        public boolean visit(AstEnumElement astNode) {
            IEnumElementSymbol symbol = astNode.getSymbol();
            if (symbol != null) {
            	SourceBinding sourceBinding = createSourceBinding(astNode);
            	
            	XdsEnumElement enumElement = new XdsEnumElement(compilationUnit.getXdsProject(),
            			compilationUnit, getName(astNode), enumType, ReferenceUtils.createRef(symbol),
            			sourceBinding);
            	enumType.addEnumElement(enumElement);
            	symbol2XdsElement.put(symbol, enumElement);
            }
            return true;
        }
    }
    
    private final class SetElementVisitor extends ModulaAstVisitor
    {
        private final XdsSet setType;

        private SetElementVisitor(XdsSet setType) {
            this.setType = setType;
        }

        @Override
        public boolean visit(AstEnumElement astNode) {
            IEnumElementSymbol symbol = astNode.getSymbol();
            SourceBinding sourceBinding = createSourceBinding(astNode);
            XdsSetElement enumElement = new XdsSetElement(getName(astNode), compilationUnit.getXdsProject(),
                    compilationUnit, setType, ReferenceUtils.createRef((IModulaSymbol)symbol),
                    sourceBinding);
            setType.addSetElement(enumElement);
            symbol2XdsElement.put(symbol, enumElement);
            return true;
        }
    }

    
    private class RecordFieldVisitor extends AbstractRecordFieldVisitor 
    {
        private final XdsRecordType xdsRecordType;
        
        public RecordFieldVisitor(XdsRecordType recordType) {
            this.xdsRecordType = recordType;
        }

        @Override
        protected void addField(IXdsRecordField recordField) {
            xdsRecordType.addField(recordField);
        }
        
        @Override
        protected IXdsRecordType getParent() {
            return xdsRecordType;
            
        }
    }


    private class RecordVariantFieldVisitor extends AbstractRecordFieldVisitor 
    {
        private final XdsRecordVariant xdsRecordVariant;
        
        public RecordVariantFieldVisitor(XdsRecordVariant recordVariant) {
            this.xdsRecordVariant = recordVariant;
        }

        @Override
        protected void addField(IXdsRecordField recordField) {
            xdsRecordVariant.addField(recordField);
        }
        
        @Override
        protected IXdsRecordVariant getParent() {
            return xdsRecordVariant;
        }
        
        @Override
        public boolean visit(AstRecordVariantLabel astNode) {
            SourceBinding sourceBinding = createSourceBinding(astNode);
            XdsRecordVariantLabel xdsRecordVariantLabel = new XdsRecordVariantLabel(
                    astNode.getText(), compilationUnit.getXdsProject(), 
                    xdsRecordVariant, sourceBinding );
            xdsRecordVariant.addLabel(xdsRecordVariantLabel);
            return false;
        }
    }
    
    
    private abstract class AbstractRecordFieldVisitor extends ModulaAstVisitor 
    {
        protected abstract IXdsContainer getParent();
        protected abstract void addField(IXdsRecordField field);
        
        private XdsRecordField newRecordField(AstRecordField astNode) {
            IRecordFieldSymbol symbol = astNode.getSymbol();
            XdsRecordField xdsRecordField = null;
            if (symbol != null) {
            	SourceBinding sourceBinding = createSourceBinding(astNode);
            	xdsRecordField = new XdsRecordField(
            			astNode.getName(), compilationUnit.getXdsProject(),
            			compilationUnit, getParent(), ReferenceUtils.createRef(symbol),
            			sourceBinding);
            	symbol2XdsElement.put(symbol, xdsRecordField);
            }
            return xdsRecordField;
        }
        
        @Override
        public boolean visit(AstRecordField astNode) {
            XdsRecordField xdsRecordField = newRecordField(astNode);
            if (xdsRecordField != null) {
            	addField(xdsRecordField);
            	
            	processAnonymousCompositeType(xdsRecordField, astNode.getAstTypeElement());
            	return true;
            }
            else{ // cannot resolve corresponding symbol - cannot go deeper.
            	return false;
            }
        }
        
        @Override
        public boolean visit(AstRecordVariantFieldBlock astNode) {
            AstRecordVariantSelector selectorAst = astNode.getAstRecordVariantSelector();
            if (selectorAst != null) {
                IRecordVariantSelectorSymbol symbol = selectorAst.getSymbol();
                if (symbol != null) {
                    SourceBinding sourceBinding;
                    if (selectorAst.getIdentifier() != null) {
                        sourceBinding = createSourceBinding(selectorAst);
                    }
                    else {
                        AstQualifiedName astQualident = selectorAst.getAstQualident();
                        if (astQualident != null) {
                            sourceBinding = createSourceBinding(astQualident);
                        }
                        else {
                            sourceBinding = createSourceBinding(selectorAst);
                        }
                    }
                    XdsRecordVariantSelector xdsRecordVariantSelector;
                    if (symbol.isAnonymous()) {
                        xdsRecordVariantSelector = new XdsAnonymousRecordVariantSelector(
                                compilationUnit.getXdsProject(), compilationUnit, getParent(), 
                                ReferenceUtils.createRef(symbol), sourceBinding );
                    }
                    else {
                        xdsRecordVariantSelector = new XdsRecordVariantSelector(
                                symbol.getName(), 
                                compilationUnit.getXdsProject(), compilationUnit, getParent(), 
                                ReferenceUtils.createRef(symbol), sourceBinding );
                    }
                    addField(xdsRecordVariantSelector);
                    symbol2XdsElement.put(symbol, xdsRecordVariantSelector);
                    astNode.accept(new RecordVariantVisitor(xdsRecordVariantSelector));
                }
            }
            return false;
        }
        
    }
    
    
    private class RecordVariantVisitor extends ModulaAstVisitor 
    {
        final XdsRecordVariantSelector xdsVariantSelector;

        public RecordVariantVisitor(XdsRecordVariantSelector variantSelector) {
            this.xdsVariantSelector = variantSelector;
        }
        
        private XdsRecordVariant newXdsRecordVariant(AstNode sourceBoundAst, boolean isElseVariant) {
            SourceBinding sourceBinding = createSourceBinding(sourceBoundAst);
            XdsRecordVariant xdsRecordVariant = new XdsRecordVariant(
                    compilationUnit.getXdsProject(), 
                    xdsVariantSelector, sourceBinding, isElseVariant);
            return xdsRecordVariant;
        }

        @Override
        public boolean visit(AstRecordVariant astNode) {
            AstNode labelListAst = astNode.getAstRecordVariantLabelList();
            if (labelListAst == null) {
                labelListAst = astNode;
            }
            XdsRecordVariant xdsRecordVariant = newXdsRecordVariant(labelListAst, false);
            xdsVariantSelector.addVariant(xdsRecordVariant);

            astNode.accept(new RecordVariantFieldVisitor(xdsRecordVariant));
            return false;
        }
        
        @Override
        public boolean visit(AstRecordVariantElsePart astNode) {
            XdsRecordVariant xdsRecordVariant = newXdsRecordVariant(astNode, true);
            xdsVariantSelector.addVariant(xdsRecordVariant);

            astNode.accept(new RecordVariantFieldVisitor(xdsRecordVariant));
            return false;
        }
    }
    
}
