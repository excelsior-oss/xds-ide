package com.excelsior.xds.parser.modula.utils;

import java.util.ArrayList;
import java.util.List;

import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaElementType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.google.common.collect.ImmutableSet;

public abstract class AstUtils {
	/**
	 * gets the n-th upper parent of the node
	 * @return
	 */
	public static PstNode getParent(PstNode node, int levelsUp) {
		PstNode parent = node;
		while(levelsUp-- > 0) {
			parent = parent.getParent();
			if (parent == null) {
				return null;
			}
		}
		return parent;
	}
	
	/**
	 * Get PROCEDURE_IDENTIFIER or MODULE_IDENTIFIER nodes of the containing composite
	 * @param node - should be name identifier of the procedure or module composite
	 * @return
	 */
	public static List<PstNode> getIdentifierNodesOfParent(PstNode node) {
		List<PstNode> nodes = new ArrayList<PstNode>();
		PstNode parent = AstUtils.getParent(node, 2);
		ImmutableSet<ModulaElementType> elementTypes = ImmutableSet.of(ModulaElementTypes.PROCEDURE_IDENTIFIER, ModulaElementTypes.MODULE_IDENTIFIER);
		if (parent instanceof PstCompositeNode) {
			PstCompositeNode composite = (PstCompositeNode) parent;
			for (PstNode child : composite.getChildren()) {
				if (elementTypes.contains(child.getElementType())) {
					nodes.add(child);
				}
			}
		}
		return nodes;
	}
}
