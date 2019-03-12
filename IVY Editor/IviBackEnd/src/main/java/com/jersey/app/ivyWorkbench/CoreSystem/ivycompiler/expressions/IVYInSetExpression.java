package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

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
public class IVYInSetExpression extends IVYExpression {

    // TODO: values could be an Interface to restrict types
    // TODO: values should be IVYConst at least... (consider IVYAttrib...)
    private final IVYAttribExpression attribName;
    private final IVYEnumerationExpression values2;
//    private IVYExpression[] values;   // ConstRefDef (Attrib?)

    public IVYInSetExpression(IVYAttribExpression attribName, IVYExpression[] split) {
        this.attribName = (IVYAttribExpression) attribName.clone();
//        this.values = split;
        this.values2 = new IVYEnumerationExpression();
        for (IVYExpression e : split) {
            values2.add((IVYConstExpression) e);
        }
    }

    public IVYInSetExpression(IVYAttribExpression attribName, IVYDefRefExpression split) {
        this.attribName = (IVYAttribExpression) attribName.clone();
//        this.values = new IVYExpression[1];
//        this.values[0] = split;
        this.values2 = new IVYEnumerationExpression();
        this.values2.add(split);
    }

    public IVYInSetExpression(IVYAttribExpression attribName, IVYEnumerationExpression split) {
        this.attribName = (IVYAttribExpression) attribName.clone();
//        this.values = new IVYExpression[1];
//        this.values[0] = split;
        this.values2 = split.clone();
    }

//    public IVYExpression getName() { return attribName; }
//    public Iterable<String> getValues() { return Arrays.asList(values); }
    @Override
    public String toString() {
        String s = values2.toString();

        if (!values2.isDefRef()) {
            s = "{" + s + "}";
        }

        return attribName + " in " + s;
    }

    @Override
    public IVYExpression clone() {
        return new IVYInSetExpression(attribName, values2.clone());
    }

    @Override
    public IVYExpression replaceVar(IVYAttribExpression attr, IVYType type, String v) {
        // TODO: Process list of values!?
        return this;
    }

    @Override
    public Set<IVYAttribExpression> getAttribsAsIndices(IVYInteractor i) {
        return new HashSet<IVYAttribExpression>();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.attribName != null ? this.attribName.hashCode() : 0);
        hash = 89 * hash + (this.values2 != null ? this.values2.hashCode() : 0);
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
        final IVYInSetExpression other = (IVYInSetExpression) obj;
        if ((this.attribName == null) ? (other.attribName != null) : !this.attribName.equals(other.attribName)) {
            return false;
        }
        if (this.values2 != other.values2 && (this.values2 == null || !this.values2.equals(other.values2))) {
            return false;
        }
        return true;
    }

    @Override
    public String toSMV() {
        String s = values2.toSMV();

        if (!values2.isDefRef()) {
            s = "{" + s + "}";
        }

        return attribName.toSMV() + " in " + s;
    }

    @Override
    public String toJSON() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("typeExpr", "IVYInSetExpression");
        obj.put("attribName", this.attribName.toJSONObject());
        obj.put("values", this.values2.toJSONObject());
        return obj;
    }

}
