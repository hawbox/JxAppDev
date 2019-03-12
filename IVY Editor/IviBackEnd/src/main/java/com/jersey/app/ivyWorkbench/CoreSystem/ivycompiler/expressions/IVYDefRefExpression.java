
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

public class IVYDefRefExpression extends IVYConstExpression {
    private final IVYExpression expr;
    
    public IVYDefRefExpression(String value, IVYExpression expr) {
        super(value);
        this.expr = expr;
    }
    
    public IVYExpression getDef() {
        return expr;
    }
    
    
    @Override
    public IVYDefRefExpression clone() {
        return new IVYDefRefExpression(super.getValue(), expr);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final IVYDefRefExpression other = (IVYDefRefExpression) obj;
        return super.equals(other) && this.expr.equals(other.expr);
    }

    @Override
    public String toSMV() {
        return expr.toSMV();
    }

}
