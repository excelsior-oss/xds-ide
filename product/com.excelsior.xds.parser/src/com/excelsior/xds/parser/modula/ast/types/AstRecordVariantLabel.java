package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.expressions.AstConstantExpression;

/**
 * RecordVariantLabel = ConstantExpression [".." ConstantExpression] <br>
 */
public class AstRecordVariantLabel extends AstConstantExpression
{
    private String text;
    
    public AstRecordVariantLabel(ModulaCompositeType<AstRecordVariantLabel> elementType) {
        super(elementType);
    }

    public void setText(String labeText) {
        text = labeText;
    }
    
    public String getText() {
        return text;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        visitor.visit(this);
    }
    
}
