package com.excelsior.xds.ui.editor.modula.contentassist2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.templates.Template;

/**
 * @author lsa80
 * TODO : move here ContributionTemplateStore and ContributionContextTypeRegistry from XdsEditorsPlugin
 */
public class ModulaTemplateRegistry {
	public static final String ARRAY_TEMPLATE_ID="ARRAY";
	public static final String BEGIN_TEMPLATE_ID="BEGIN";
	public static final String CASE_ELSE_TEMPLATE_ID="CASE-ELSE";
	public static final String CASE_TEMPLATE_ID="CASE";
	public static final String CONST_TEMPLATE_ID="CONST";
	public static final String ELSIF_TEMPLATE_ID="ELSIF";
	public static final String FOR_BY_TEMPLATE_ID="FOR-BY";
	public static final String FOR_TEMPLATE_ID="FOR";
	public static final String FROM_TEMPLATE_ID="FROM";
	public static final String FUNCTION_TEMPLATE_ID="function";
	public static final String FUNCTIONDEF_TEMPLATE_ID="functiondef";
	public static final String IF_ELSE_TEMPLATE_ID="IF-ELSE";
	public static final String IF_ELSIF_TEMPLATE_ID="IF-ELSIF";
	public static final String IF_TEMPLATE_ID="IF";
	public static final String IMPORT_TEMPLATE_ID="IMPORT";
	public static final String POINTER_TEMPLATE_ID="POINTER";
	public static final String PROCARGS_TEMPLATE_ID="procargs";
	public static final String PROCARGSDEF_TEMPLATE_ID="procargsdef";
	public static final String PROCDEF_TEMPLATE_ID="procdef";
	public static final String PROCEDURE_TEMPLATE_ID="PROCEDURE";
	public static final String RANGE_TEMPLATE_ID="range";
	public static final String RECORD_TEMPLATE_ID="RECORD";
	public static final String REPEAT_TEMPLATE_ID="REPEAT";
	public static final String RETURN_TEMPLATE_ID="RETURN";
	public static final String SET_TEMPLATE_ID="set";
	public static final String TYPE_TEMPLATE_ID="TYPE";
	public static final String VAR_TEMPLATE_ID="VAR";
	public static final String WHILE_TEMPLATE_ID="WHILE";
	public static final String WITH_TEMPLATE_ID="WITH";
	
	private static final Map<String, Set<RegionType>> templateId2RegionTypes = new HashMap<>();
	
	static{
		associate(ARRAY_TEMPLATE_ID, RegionType.UNKNOWN);
		associate(BEGIN_TEMPLATE_ID, RegionType.UNKNOWN);
		
		associate(CASE_TEMPLATE_ID, RegionType.PROCEDURE_BODY);
		associate(CASE_ELSE_TEMPLATE_ID, RegionType.PROCEDURE_BODY);
		
		associate(CONST_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		
		associate(ELSIF_TEMPLATE_ID, RegionType.PROCEDURE_BODY);
		associate(FOR_TEMPLATE_ID, RegionType.PROCEDURE_BODY);
		associate(FOR_BY_TEMPLATE_ID, RegionType.PROCEDURE_BODY);
		
		associate(FROM_TEMPLATE_ID, RegionType.IMPORT_STATEMENT, RegionType.MODULE);
		
		associate(FUNCTION_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		associate(FUNCTIONDEF_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		
		associate(IF_ELSE_TEMPLATE_ID, RegionType.PROCEDURE_BODY);
		associate(IF_ELSIF_TEMPLATE_ID, RegionType.PROCEDURE_BODY);
		associate(IF_TEMPLATE_ID, RegionType.PROCEDURE_BODY);
		
		associate(IMPORT_TEMPLATE_ID, RegionType.IMPORT_STATEMENT, RegionType.MODULE);
		
		associate(POINTER_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		associate(PROCARGSDEF_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		
		associate(PROCARGS_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		associate(PROCDEF_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		associate(PROCEDURE_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		associate(RANGE_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		associate(RECORD_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		
		associate(RETURN_TEMPLATE_ID, RegionType.PROCEDURE_BODY);
		
		associate(SET_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		
		associate(TYPE_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		
		associate(REPEAT_TEMPLATE_ID, RegionType.PROCEDURE_BODY);
		
		associate(VAR_TEMPLATE_ID, RegionType.DECLARATIONS, RegionType.MODULE);
		associate(WHILE_TEMPLATE_ID, RegionType.PROCEDURE_BODY);
		associate(WITH_TEMPLATE_ID, RegionType.PROCEDURE_BODY);
	}
	
	public static boolean isEnabled(Template template, RegionType currentRegionType) {
		Set<RegionType> regionTypes = templateId2RegionTypes.get(template.getName());
		if (regionTypes == null) {
			return true;
		}
		return regionTypes.contains(currentRegionType);
	}
	
	private static void associate(String templateId, RegionType... regionTypes) {
		templateId2RegionTypes.put(templateId, new HashSet<>(Arrays.asList(regionTypes)));
	}
}
