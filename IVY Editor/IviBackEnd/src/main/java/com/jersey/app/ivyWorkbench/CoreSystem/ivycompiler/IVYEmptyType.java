
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author jfc
 */
public class IVYEmptyType extends IVYType {

    public IVYEmptyType(String name) {
        super(name, null);
    }
    
    @Override
    public boolean contains(String expr) {
        return false;
    }

    @Override
    public boolean contains(IVYExpression paramExpr) throws IVYCompilerException {
        return false;
    }

    @Override
    public List<String> values() {
        return new ArrayList<String>();
    }

    @Override
    public IVYType clone() {
        return new IVYEmptyType(super.getName());
    }

    @Override
    public boolean contains(IVYType tdef) {
        return false;
    }

    @Override
    public String toSMV() {
        throw new UnsupportedOperationException("Not a valid operation.");
    }
    
    @Override
    public JSONObject toJSONObject() {
        throw new UnsupportedOperationException("Not a valid operation.");
    }
}
