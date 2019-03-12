
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import org.json.simple.JSONObject;

public class IVYAttribute {
    private String name;
    private IVYType type;
    private Compiler compiler;

    public IVYAttribute(String name, IVYType type, Compiler compiler) {
        this.name = name;
        this.type = type;
        this.compiler = compiler;
    }

    public IVYAttribute(IVYAttribute ia) {
        this.name = ia.name;
        this.type = ia.type;
        this.compiler = ia.compiler;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the typeTOREMOVE
     */
    public IVYType getType() {
        return type;
    }

    public String toString() {
        return "attribute "+this.name+": "+this.type.getName();
    }
    
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("type", type.getName());
        obj.put("typeClass", "IVYAttribute");
        return obj;
    }

    void replaceType(IVYEmptyType ptype, IVYType ctype) {
        if (this.type.equals(ptype))
            this.type=ctype;
    }

    String toSMV() {
        return name+": "+this.type.toSMV()+";";
    }

    @Override
    public IVYAttribute clone() {
        return new IVYAttribute(this);
    }

}
