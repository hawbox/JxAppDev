package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;

import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;

/**
 * Unary expression
 *
 * @author jfc
 */
public class IVYUnaryExpression extends IVYExpression {

    private final String operator;
    private IVYExpression expr;

    public IVYUnaryExpression(String op, IVYExpression group) {
        this.operator = op;
        this.expr = group;
    }

    public String getOperator() {
        return operator;
    }

    public IVYExpression getExpression() {
        return expr.clone();
    }

    @Override
    public String toString() {
        return operator.equals("()") ? "(" + expr + ")" : operator + expr;
    }

    @Override
    public IVYExpression clone() {
        return new IVYUnaryExpression(operator, expr.clone());
    }

    @Override
    public IVYExpression replaceVar(IVYAttribExpression attr, IVYType type, String v) {
        this.expr = this.expr.replaceVar(attr, type, v);
        return this;
    }

    @Override
    public Set<IVYAttribExpression> getAttribsAsIndices(IVYInteractor i) {
        Set<IVYAttribExpression> list = new HashSet<IVYAttribExpression>();
        Set<IVYAttribExpression> atts = expr.getAttribsAsIndices(i);

        list.addAll(atts);
        return list;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.operator != null ? this.operator.hashCode() : 0);
        hash = 17 * hash + (this.expr != null ? this.expr.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IVYUnaryExpression other = (IVYUnaryExpression) obj;
        if ((this.operator == null) ? (other.operator != null) : !this.operator.equals(other.operator)) {
            return false;
        }
        if (this.expr != other.expr && (this.expr == null || !this.expr.equals(other.expr))) {
            return false;
        }
        return true;
    }

    @Override
    public String toSMV() {
        StringBuilder sb = new StringBuilder();

        if (operator.equals("()")) {
            sb.append("(");
        } else if (operator.equals("{}")) {
            sb.append("{");
        } else {
            sb.append(operator);
        }
        if (operator.equals("AG") || operator.equals("AF") || operator.equals("AX")
                || operator.equals("EG") || operator.equals("EF") || operator.equals("EG")) {
            sb.append(" ");
        }
        sb.append(expr.toSMV());
        if (operator.equals("()")) {
            sb.append(")");
        } else if (operator.equals("{}")) {
            sb.append("}");
        }
        return sb.toString();
    }

    @Override
    public IVYExpression unfoldActions(IVYInteractor i) throws IVYCompilerException {
        this.expr = this.expr.unfoldActions(i);
        return this;
    }

    /**
     * Unfold arrays without explicit indices.
     *
     * @param i the interactor for this expression
     * @return teh expression with arrays unfolded
     */
    @Override
    public IVYExpression unfoldAnonArrayRefs(IVYInteractor i) throws IVYCompilerException {
        expr = expr.unfoldAnonArrayRefs(i);
        return this;
    }

    @Override
    public IVYExpression unfoldVariables(IVYInteractor inter) throws IVYCompilerException {
        expr = expr.unfoldVariables(inter);

        return this;
    }

    @Override
    public String toJSON() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("operator", operator);
        obj.put("expression", expr.toJSONObject());
        obj.put("typeExpr", "IVYUnaryExpression");
        return obj;
    }

}
