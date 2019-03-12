
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYConstExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYAttribExpression;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;

/**
 *
 * @author jfc
 */
public class IVYEnumType extends IVYType {

    private List<IVYConstExpression> values;

    public IVYEnumType(String name, List<IVYConstExpression> values, Compiler compiler) {
        super(name, compiler);
        this.values = new ArrayList<IVYConstExpression>();
        this.values.addAll(values);
    }

    private IVYEnumType(IVYEnumType aThis) {
        this(aThis.getName(), aThis.values, aThis.compiler);
    }
    
    @Override
    public String toString() {
        String tdef = "{";
        for (IVYConstExpression s: values) 
            tdef += s.toString()+", ";
        tdef = tdef.replaceAll("\\, \\z", "}");
        return tdef;
    }
    
    @Override
    public JSONObject toJSONObject(){
        JSONObject obj = new JSONObject();
        try {
//            if(this.getName()!=null){ obj.put("name", this.getName());}
            obj.put("typeClass", "IVYEnumType");
            JSONArray array = new JSONArray();
            if (values!=null) {
                for(IVYConstExpression e : values){
                    array.add(e.toJSONObject());
                }
            }
            obj.put("types", array);
        } catch (Exception e) {
            //Logger.alert(e, "JSONObject builder error", "Error while building JSONObject at class IVYEnumType");
        }
        return obj;
    }

    @Override
    public boolean contains(String param) {
        return values.contains(new IVYConstExpression(param));
    }

    @Override
    public boolean contains(IVYExpression expr) throws IVYCompilerException {
        if (expr instanceof IVYConstExpression)
            return this.values.contains((IVYConstExpression)expr);
        if (expr instanceof IVYAttribExpression) {
            IVYAttribExpression att = (IVYAttribExpression) expr;
            String[] path = att.getPath();
            String name = att.getName();
            if (name.startsWith("_"))
                return true;
            IVYInteractor inter = super.compiler.model.getInteractor(super.compiler.currentInteractor, path);
            IVYType type = inter.getAttribute(name).getType();
            return this.contains(type);
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o==null) || (o.getClass()!=this.getClass()))
            return false;
        IVYEnumType t = (IVYEnumType) o;
        return t.values.equals(this.values);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.values != null ? this.values.hashCode() : 0);
        return hash;
    }

    public boolean contains(IVYType type) {
        if (!(type instanceof IVYEnumType))
            return false;
        IVYEnumType rt = (IVYEnumType) type;
        return this.values.containsAll(rt.values);
     }

    @Override
    public List<String> values() {
        List<String> res = new ArrayList<String>();
        for(IVYConstExpression e: values)
            res.add(e.getValue());
        return res;
    }

    @Override
    public IVYType clone() {
        return new IVYEnumType(this);
    }

    @Override
    public String toSMV() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("{");
        sb.append(this.values.get(0));
        for (int i = 1; i<this.values.size();i++) {
            sb.append(", ");
            sb.append(this.values.get(i).toSMV());
        }
        sb.append("}");
        return sb.toString();
    }
}
