package com.excelsior.xds.parser.internal.modula.nls;

import org.eclipse.osgi.util.NLS;

public class XdsMessages extends NLS {
    
    private static final String BUNDLE_NAME = "com.excelsior.xds.parser.internal.modula.nls.xds_messages"; //$NON-NLS-1$
    
    public static String IllegalCharacter;                           // 0001
    public static String IllegalNumber;                              // 0003
    public static String IdentifierExpected;                         // 0007
    public static String ExpectedSymbol;                             // 0008
    
    public static String UndeclaredIdentifier;                       // 0020
    public static String IdentifierAlreadyDefined;                   // 0022
    
    public static String RecursiveImportDisabled;                    // 0024
    public static String UnsatisfiedExportedObject;                  // 0025
    
    public static String IncompatibleTypes;                          // 0030
    public static String IdentifierDoesNotDenoteType;                // 0031
    public static String ExpectedType;                               // xxxx
    public static String ExpectedOrdinalType;                        // 0033
    public static String IllegalOpenArrayTypeUsage;                  // 0046
    
    public static String ObjectIsNotArray;                           // 0050
    public static String ObjectIsNotRecord;                          // 0051
    public static String ObjectIsNotPointer;                         // 0052
    public static String ObjectIsNotVariable;                        // 0054
    
    public static String ObjectIsNotPointerOrRecord;                 // 0059

    public static String PointerNotBoundRecord;                      // 0062 
    public static String BaseTypeOfOpenArrayAggregateShuldBeSimple;  // 0064 
    public static String OberonTypeIsRequired;                       // 0071
    
    public static String ExpectedStartOfFactor;                      // 0081
    public static String ExpectedDeclarationStart;                   // 0082
    public static String ExpectedTypeStart;                          // 0083
    public static String ExpectedStartOfStatement;                   // 0086
    public static String ProcedureNotImplemented;                    // 0089
    
    public static String NotAllowedInDefinitionModule;               // 0093
    public static String AllowedOnlyInGlobalScope;                   // 0095
    public static String UnsatisfiedForwardType;                     // 0097
    
    public static String IllegalDeclarationOrder;                    // 0100
    public static String ExtensionNotAllowed;                        // 0102

    public static String ForwardTypeCannotBeOpaque;                  // 0109
    public static String ExpressionForFieldWasExpected;              // 0109
    
    public static String TypeIsNotDefined;                           // 0117
    public static String VariantFieldsNotAllowedInOberonRecord;      // 0119
    public static String ExpressionOutOfBounds;                      // 0122
    public static String ReadOnlyDesignator;                         // 0123
    public static String LowBoundGreaterThanHighBound;               // 0124
    
    public static String ForLoopControlVariableMustBeLocal;          // 0128
    public static String MoreExpressionsThanFieldsInRecord;          // 0129
    
    public static String InterruptProceduresNotImplemented;          // 0140
    public static String NotAllowedInOberon;                         // 0143
    
    public static String ForLoopControlVariableMustNotBeFormalParameter;  // 0145
    public static String ForLoopControlVariableCannotBeExported;     // 146
    
    public static String InvalidLanguageValue;                       // 0150
    public static String InvalidParameterSpecificationExpectedNIL;   // 0160
    public static String ControlVariableCannotBeVolatile;            // 0163
    public static String LanguageIsNotValidForExternalProcedure;     // 0164
    public static String DirectLanguageSpecification;                // xxxx
    
    public static String IllegalConditionCompilation;                // 0171
    public static String InvalidPragmaSyntax;                        // 0175
    public static String IllegalConditionCompilationAtPosition;      // xxxx

    public static String ImplicitSystemCast;                         // 0209
    
    public static String CaseWithoutElsePart;                        // 0319
    public static String UnknownOption;                              // 0320
    public static String OptionAlreadyDefined;                       // 0321
    public static String UnknownEquation;                            // 0322
    public static String EquationAlreadyDefined;                     // 0323
    public static String ModuleConstructorWillNotBeInvoked;          // 0341
    
    public static String StringExpected;                             // 0418
    
    public static String UnresolvedModuleName;                       // 0470
    public static String CannotFindDefinitionModule;                 // 0471
    
    public static String ArrayRecotdSimpleAgregate;                  // 0706
    public static String ObsoleteTypeCast;                           // 0707
    public static String ReadOnlyTag;                                // 0711
    public static String SpecialKindsParameters;                     // 0712
    public static String ReadOnlyParameters;                         // 0713
    public static String UnusedParameters;                         
    public static String ParameterValueByDefault;                    // 0714
    public static String SeqParameter;                               // 0716
    public static String ArrayOfType;                                // 0717
    public static String RenamingInImport;                           // 0718
    public static String ExponentiationOperator;                     // 0719
    public static String IsoPragmaSyntax;                            // 0720
    public static String UseOptionCppComments;                       // 0721
    public static String UseCppLineComments;                         // 0722
    public static String UseCppBlockComments;                        // 0723
    
    public static String DefinitionModuleOption;                     // 1022
    public static String KeywordDisabledByK26Option;                 // 2000
    
    public static String ImportFromObjectIllegal;                    // xxxx
    

    public static String OutlineNodeText_ImportList;
    public static String OutlineNodeText_VariantRecord;
    public static String OutlineNodeText_Variant;
    
    public static String ImportCycleDetected;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, XdsMessages.class);
    }

    private XdsMessages() {
    }

}
