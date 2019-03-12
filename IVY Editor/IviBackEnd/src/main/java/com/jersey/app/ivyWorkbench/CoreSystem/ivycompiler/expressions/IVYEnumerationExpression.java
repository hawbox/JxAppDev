package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;

/**
 *
 * @author jfc
 */
public class IVYEnumerationExpression extends IVYExpression {

    private List<IVYConstExpression> list;

    public IVYEnumerationExpression(List<IVYConstExpression> split) {
        this.list = split;
    }

    public IVYEnumerationExpression() {
        this.list = new ArrayList<IVYConstExpression>();
    }

    public void add(IVYConstExpression e) {
        this.list.add(e);
    }

    @Override
    public String toString() {
        String s = "";

        for (IVYConstExpression e : list) {
            s += (e.toString() + ", ");
        }
        s = s.replaceFirst(", \\z", "");
        return s;
    }

    @Override
    public IVYEnumerationExpression clone() {
        List<IVYConstExpression> list = new ArrayList<IVYConstExpression>();

        for (IVYConstExpression at : this.list) {
            list.add((IVYConstExpression) at.clone());
        }
        return new IVYEnumerationExpression(list);
    }

    @Override
    public IVYExpression replaceVar(IVYAttribExpression attr, IVYType type, String v) {
//        throw new UnsupportedOperationException("Not supported yet.");
        return this;
    }

    @Override
    public Set<IVYAttribExpression> getAttribsAsIndices(IVYInteractor i) {
        return new HashSet<IVYAttribExpression>();
    }

    public List<IVYConstExpression> getValues() {
        return this.list;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.list != null ? this.list.hashCode() : 0);
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
        final IVYEnumerationExpression other = (IVYEnumerationExpression) obj;
        if (this.list != other.list && (this.list == null || !this.list.equals(other.list))) {
            return false;
        }
        return true;
    }

    @Override
    public String toSMV() {
        String sep = ", ";
        StringBuilder sb = new StringBuilder();

        for (IVYConstExpression e : list) {
            sb.append(e.toSMV());
            sb.append(sep);
        }
        return sb.toString().replaceFirst(sep + "\\z", "");
    }

    boolean isDefRef() {
        return this.list.size() == 1 && this.list.get(0) instanceof IVYDefRefExpression;
    }

    @Override
    public String toJSON() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("typeExpr", "IVYEnumerationExpression");
            JSONArray array = new JSONArray();
            if (list != null) {
                for (IVYConstExpression exp : list) {
                    array.add(exp.toJSONObject());
                }
            }
            obj.put("list", array);
        } catch (Exception e) {
            //Logger.alert(e, "JSONObject builder error", "Error while building JSONObject at class IVYEnumerationExpression");
        }
        return obj;
    }

}
