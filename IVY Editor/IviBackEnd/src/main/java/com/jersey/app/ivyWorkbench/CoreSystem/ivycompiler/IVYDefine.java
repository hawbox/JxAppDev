
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import org.json.simple.JSONObject;

public class IVYDefine {
    private final String name;
    private final String expr;

    public IVYDefine(String name, String expr) {
        this.name = name;
        this.expr = expr;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the expr
     */
    public String getExpr() {
        return expr;
    }
    
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("expression", expr);
        obj.put("typeClass", "IVYDefine");
        return obj;
    }

    @Override
    public String toString() {
        return "defines "+name+" = "+expr;
    }


}
