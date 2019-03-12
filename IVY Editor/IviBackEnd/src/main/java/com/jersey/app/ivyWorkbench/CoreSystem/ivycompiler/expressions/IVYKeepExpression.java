package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

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
public class IVYKeepExpression extends IVYExpression {

    private List<IVYAttribExpression> list;

    public IVYKeepExpression(List<IVYAttribExpression> split) {
        this.list = split;
//        for(int i=0; i<list.length; i++)
//            list[i] = list[i].trim();
    }

    @Override
    public String toString() {
        String s = "keep(";

        for (IVYAttribExpression e : list) {
            s += (e.toString() + ",");
        }
        s = s.replaceFirst(",\\z", ")");
        return s;
    }

    public List<IVYAttribExpression> getList() {
        return list;
    }

    @Override
    public IVYExpression clone() {
        List<IVYAttribExpression> list = new ArrayList<IVYAttribExpression>();

        for (IVYAttribExpression at : this.list) {
            list.add((IVYAttribExpression) at.clone());
        }
        return new IVYKeepExpression(list);
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
        final IVYKeepExpression other = (IVYKeepExpression) obj;
        if (this.list != other.list && (this.list == null || !this.list.equals(other.list))) {
            return false;
        }
        return true;
    }

    @Override
    public String toSMV() {
        String s = "(";

        for (IVYAttribExpression e : list) {
            s += ("next(" + e.toSMV() + ")=" + e.toSMV() + " & ");
        }
        s = s.replaceFirst(" & \\z", ")");
        return s;
    }

    @Override
    public String toJSON() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("typeExpr", "IVYKeepExpression");
            JSONArray array = new JSONArray();
            if (list != null) {
                for (IVYAttribExpression exp : list) {
                    array.add(exp.toJSONObject());
                }
            }
            obj.put("list", array);
        } catch (Exception e) {
            //Logger.alert(e, "JSONObject builder error", "Error while building JSONObject at class IVYKeepExpression");
        }
        return obj;
    }

}
