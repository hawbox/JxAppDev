
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;
import java.util.HashSet;
import java.util.Set;
import org.json.simple.*;

/**
 *
 * @author jfc
 */
public class IVYConstExpression extends IVYExpression {

    private final String value;

    protected IVYConstExpression() {
        value = "";
    }

    public IVYConstExpression(String name) {
        this.value = name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public IVYConstExpression clone() {
        return new IVYConstExpression(this.value);
    }

    @Override
    public IVYExpression replaceVar(IVYAttribExpression attr,IVYType type,String v) {
 //       throw new UnsupportedOperationException("Not supported yet.");
        return this;
    }

    @Override
    public Set<IVYAttribExpression> getAttribsAsIndices(IVYInteractor i) {
        return new HashSet<IVYAttribExpression>();
    }

   @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final IVYConstExpression other = (IVYConstExpression) obj;
        return this.value.equals(other.value);
    }

    @Override
    public String toSMV() {
        return (value.equals("false") || value.equals("true")) ? value.toUpperCase(): value;
    }

    @Override
    public String toJSON() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject(){
        JSONObject obj = new JSONObject();
        obj.put("value", value);
        obj.put("typeExpr", "IVYConstExpression");
        return obj;
    }
}
