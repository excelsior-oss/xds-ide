package com.excelsior.xds.parser.modula.ast.pragmas;

import com.excelsior.xds.parser.commons.ast.IAstFrameChild;
import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;

public class AstPragmaEndStatement extends AstPragmaConditionalStatement implements IAstFrameChild
{
    private IAstFrameNode frameNode;

    public AstPragmaEndStatement(ModulaCompositeType<AstPragmaEndStatement> elementType) {
        super(elementType);
    }

    @Override
    public IAstFrameNode getAstFrameNode() {
        return frameNode;
    }

    @Override
    public void setAstFrameNode(IAstFrameNode frameNode) {
        this.frameNode = frameNode;
    }

}
