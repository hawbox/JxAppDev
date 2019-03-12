package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;

import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;

/**
 *
 * @author jfc
 */
public class IVYBinaryExpression extends IVYExpression {

    private String operator = "";
    private IVYExpression left, right;

    public IVYBinaryExpression(String op, IVYExpression l, IVYExpression r) {
        this.operator = op;
        this.left = l;
        this.right = r;
    }

    public IVYExpression getLeft() {
        return left.clone();
    }

    public IVYExpression getRight() {
        return right.clone();
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

// JFC: Removed because it is alreay in the operator...
//        if (operator.equals("U"))
//            sb.append("[");
        sb.append(left);
        sb.append(" ");
        sb.append(operator);
        sb.append(" ");
        sb.append(right);
        if (operator.equals("U")) {
            sb.append("]");
        }
        return sb.toString();
    }

    @Override
    public IVYExpression clone() {
        return new IVYBinaryExpression(operator, left.clone(), right.clone());
    }

    @Override
    public IVYExpression replaceVar(IVYAttribExpression attr, IVYType type, String v) {
        this.left = this.left.replaceVar(attr, type, v);
        this.right = this.right.replaceVar(attr, type, v);
        return this;
    }

    @Override
    public Set<IVYAttribExpression> getAttribsAsIndices(IVYInteractor i) {
        Set<IVYAttribExpression> list = new HashSet<IVYAttribExpression>();

        list.addAll(left.getAttribsAsIndices(i));
        list.addAll(right.getAttribsAsIndices(i));
        return list;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.operator != null ? this.operator.hashCode() : 0);
        hash = 37 * hash + (this.left != null ? this.left.hashCode() : 0);
        hash = 37 * hash + (this.right != null ? this.right.hashCode() : 0);
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
        final IVYBinaryExpression other = (IVYBinaryExpression) obj;
        if ((this.operator == null) ? (other.operator != null) : !this.operator.equals(other.operator)) {
            return false;
        }
        if (this.left != other.left && (this.left == null || !this.left.equals(other.left))) {
            return false;
        }
        if (this.right != other.right && (this.right == null || !this.right.equals(other.right))) {
            return false;
        }
        return true;
    }

    @Override
    public String toSMV() {
        StringBuilder sb = new StringBuilder();

// JFC: Removed because it is alreay in the operator...
//        if (operator.equals("U"))
//            sb.append("[");
        sb.append(left.toSMV());
        sb.append(" ");
        sb.append(operator);
        sb.append(" ");
        sb.append(right.toSMV());
        if (operator.equals("U")) {
            sb.append("]");
        }
        return sb.toString();
    }

    @Override
    public IVYExpression unfoldActions(IVYInteractor i) throws IVYCompilerException {
        this.left = this.left.unfoldActions(i);
        this.right = this.right.unfoldActions(i);
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
        // TODO: nothing to do except for equalities x'=x? what about ac(x)?
        left = left.unfoldAnonArrayRefs(i);
        right = right.unfoldAnonArrayRefs(i);
        return this;
    }

    @Override
    public IVYExpression unfoldVariables(IVYInteractor inter) throws IVYCompilerException {
        left = left.unfoldVariables(inter);
        right = right.unfoldVariables(inter);

        return this;
    }

    @Override
    public String toJSON() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("left", this.left.toJSONObject());
        obj.put("operator", operator);
        obj.put("right", right.toJSONObject());
        obj.put("typeExpr", "IVYBinaryExpression");
        return obj;
    }

}
