
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYConstExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYAttribExpression;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author jfc
 */
public class IVYBooleanType extends IVYType {

    public IVYBooleanType(Compiler compiler) {
        super("boolean", compiler);
    }

    @Override
    public boolean contains(String expr) {
        return expr.equalsIgnoreCase("TRUE") || expr.equalsIgnoreCase("FALSE");
    }

    @Override
    public boolean contains(IVYExpression expr) throws IVYCompilerException {
        if (expr instanceof IVYConstExpression)
            return this.contains(((IVYConstExpression)expr).getValue());
        if (expr instanceof IVYAttribExpression) {
            IVYAttribExpression att = (IVYAttribExpression) expr;
            String[] path = att.getPath();
            String name = att.getName();
            if (name.startsWith("_"))
                return true;
            IVYInteractor inter = super.compiler.model.getInteractor(super.compiler.currentInteractor, path);
            IVYAttribute atr = inter.getAttribute(name);
            return this.equals(inter.getAttribute(name).getType());
        }
        return false;
    }


    @Override
    public List<String> values() {
        List<String> res = new ArrayList<String>();
        res.add("TRUE");
        res.add("FALSE");
        return res;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("name", super.getName());
        obj.put("typeClass", "IVYBooleanType");
        return obj;
    }
    
    @Override
    public IVYType clone() {
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public String toString() {
        return super.getName();
    }

    @Override
    public boolean contains(IVYType tdef) {
        return tdef instanceof IVYBooleanType;
    }

    @Override
    public String toSMV() {
        return super.getName();
    }

}
