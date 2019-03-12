package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYArrayType;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;

/**
 *
 * @author jfc
 */
public class IVYEqualityExpression extends IVYExpression {

    private IVYExpression left;
    private IVYExpression right;

    public IVYEqualityExpression(IVYExpression left, IVYExpression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public IVYExpression clone() {
        return new IVYEqualityExpression(left.clone(), right.clone());
    }

    @Override
    public IVYExpression replaceVar(IVYAttribExpression attr, IVYType type, String v) {
        // Falta desdobrar
        if (type instanceof IVYArrayType && attr instanceof IVYAttribExpression && attr.equals(right)) {
            IVYAttribExpression aux = (IVYAttribExpression) this.left;
            IVYArrayType auxT = (IVYArrayType) type;
            return auxT.createEquality(aux, v.split("-"));
        }
        this.left = this.left.replaceVar(attr, type, v);
        this.right = this.right.replaceVar(attr, type, v);
        return this;
    }

    // TODO: Falta resolver o:
    //   [[ac3(_v)]] at3 = _v
    //   TRANS next(action)=ac3_0-0-0-0-1-1 -> at3 = 0-0-0-0-1-1
    // devia ser:
    //   TRANS next(action)=ac3_0-0-0-0-1-1 -> at3[0][0] = 0 & at3[0][1]= 0 & ...-0-0-1-1
    @Override
    public Set<IVYAttribExpression> getAttribsAsIndices(IVYInteractor i) {
        Set<IVYAttribExpression> list = new HashSet<IVYAttribExpression>();

        list.addAll(left.getAttribsAsIndices(i));
        list.addAll(right.getAttribsAsIndices(i));
        return list;
    }

    @Override
    public String toString() {
        return left.toString() + " = " + right.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.left != null ? this.left.hashCode() : 0);
        hash = 31 * hash + (this.right != null ? this.right.hashCode() : 0);
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
        final IVYEqualityExpression other = (IVYEqualityExpression) obj;
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
        return left.toSMV() + " = " + right.toSMV();
    }

    @Override
    public IVYExpression unfoldActions(IVYInteractor i) throws IVYCompilerException {
        this.left = this.left.unfoldActions(i);
        this.right = this.right.unfoldActions(i);
        return this;
    }

    /**
     * Unfold arrays without explicit indices. Consider T = array 0..1 of ??,
     * and arribute a: T. a'=a becomes (a[0]'=a[0] & a[1]'=a[1])
     *
     * @param i the interactor for this expression
     * @return teh expression with arrays unfolded
     * @throws IVYCompilerException
     */
    @Override
    public IVYExpression unfoldAnonArrayRefs(IVYInteractor i) throws IVYCompilerException {
        IVYExpression res = this;

        if (left.getClass() == IVYAttribExpression.class && right.getClass() == IVYAttribExpression.class) {
            IVYAttribExpression leftAtt = (IVYAttribExpression) left;
            IVYAttribExpression rightAtt = (IVYAttribExpression) right;
            if (leftAtt.getPrimed() != rightAtt.getPrimed()) {
                leftAtt.setPrimed(!leftAtt.getPrimed());
                if (leftAtt.equals(rightAtt)) {
                    leftAtt.setPrimed(!leftAtt.getPrimed());
                    IVYInteractor inter = i.getModel().getInteractor(i, leftAtt.getPath());
                    IVYType type = inter.getAttribute(leftAtt.getName()).getType();
                    if (type instanceof IVYArrayType) {
                        IVYArrayAttribExpression leftArrAtt, rightArrAtt;
                        List<IVYExpression> listEqs = new ArrayList<IVYExpression>();
                        IVYArrayType arrType = (IVYArrayType) type;
                        for (String v : arrType.indices()) {
                            // generate a series of && com x[v]'=x[v] (change void to exprs. List
                            String[] params = v.split("\\]\\[");
                            if (params == null) {
                                params = new String[1];
                                params[0] = v;
                            }
                            params[0] = params[0].substring(1);
                            params[params.length - 1] = params[params.length - 1].substring(0, params[params.length - 1].length() - 1);
                            leftArrAtt = new IVYArrayAttribExpression(leftAtt.getPath(), leftAtt.getName(), params, leftAtt.getPrimed(), i.getModel());
                            rightArrAtt = new IVYArrayAttribExpression(rightAtt.getPath(), rightAtt.getName(), params, rightAtt.getPrimed(), i.getModel());
                            listEqs.add(new IVYEqualityExpression(leftArrAtt, rightArrAtt));
                        }
                        res = listEqs.get(0);
                        for (int j = 1; j < listEqs.size(); j++) {
                            res = new IVYBinaryExpression("&", res, listEqs.get(j));
                        }
                        res = new IVYUnaryExpression("()", res);
                    }
                } else {
                    leftAtt.setPrimed(!leftAtt.getPrimed());
                }
            }
        }
        return res;
    }

    @Override
    public IVYExpression unfoldVariables(IVYInteractor inter) throws IVYCompilerException {
        left = left.unfoldVariables(inter);
        right = right.unfoldVariables(inter);

        return this;
    }

    public IVYExpression getRight() {
        return this.right;
    }

    public IVYExpression getLeft() {
        return this.left;
    }

    public void setRight(IVYConstExpression expr) {
        this.right = expr;
    }

    @Override
    public String toJSON() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("left", left.toJSONObject());
        obj.put("right", right.toJSONObject());
        obj.put("typeExpr", "IVYEqualityExpression");
        return obj;
    }

}
