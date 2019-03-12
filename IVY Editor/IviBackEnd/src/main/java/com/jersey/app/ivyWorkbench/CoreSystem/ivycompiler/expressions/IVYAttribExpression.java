package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.json.simple.*;
//import com.jersey.app.ivyWorkbench.Logger;

/**
 *
 * @author jfc
 */
public class IVYAttribExpression extends IVYExpression {

    private String attrib;
    private boolean primed;
    private String[] path;
    private IVYType type;

    public IVYAttribExpression(String[] path, String att, boolean p, IVYType type) {
        this(path, att, p);
        this.type = type;
    }

    public IVYAttribExpression(String[] path, String att, boolean p) {
        this.path = path;
        this.attrib = att.trim();
        this.primed = p;
        this.type = null;
    }

    public String getName() {
        return this.attrib;
    }

    public void setName(String string) {
        attrib = string;
    }

    @Override
    public String toString() {
        String s = "";

        if (path != null) {
            for (String sa : path) {
                s += (sa + ".");
            }
        }
        return s + attrib + (primed ? "'" : "");
    }

    public String[] getPath() {
        return path;
    }

    @Override
    public IVYExpression clone() {
        return new IVYAttribExpression(path != null ? path.clone() : path, attrib, primed);
    }

    @Override
    public IVYExpression replaceVar(IVYAttribExpression attr, IVYType type, String v) {
        if (this.attrib.equals(attr.attrib) && Arrays.equals(this.path, attr.path)) {
            return new IVYConstExpression(v);
        }
        return this;
    }

    @Override
    public Set<IVYAttribExpression> getAttribsAsIndices(IVYInteractor i) {
        return new HashSet<IVYAttribExpression>();
    }

    public boolean getPrimed() {
        return primed;
    }

    public IVYType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.attrib != null ? this.attrib.hashCode() : 0);
        hash = 67 * hash + (this.primed ? 1 : 0);
        hash = 67 * hash + (this.path != null ? this.path.hashCode() : 0);
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
        final IVYAttribExpression other = (IVYAttribExpression) obj;
        if ((this.attrib == null) ? (other.attrib != null) : !this.attrib.equals(other.attrib)) {
            return false;
        }
        if (this.primed != other.primed) {
            return false;
        }
        if (this.path != other.path && (this.path == null || !this.path.equals(other.path))) {
            return false;
        }
        return true;
    }

    @Override
    public String toSMV() {
        String s = "";

        if (path != null) {
            for (String sa : path) {
                s += (sa + ".");
            }
        }
        s += attrib;
        if (primed) {
            s = "next(" + s + ")";
        }
        return s;
    }

    void setPrimed(boolean b) {
        this.primed = b;
    }

    @Override
    public String toJSON() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            if(path!=null){
            array.addAll(Arrays.asList(path));
            }
            if(attrib!=null){ obj.put("attrib", attrib);}else{ obj.put("attrib", "");}
            if(type!=null){ obj.put("type", type.getName());}else{ obj.put("type", "");}
            obj.put("primed", primed);
            obj.put("typeExpr", "IVYAttribExpression");
            obj.put("path", array);
        } catch (Exception e) {
            //Logger.alert(e, "JSONObject builder error", "Error while building JSONObject at class IVYAttribExpression");
        }
        return obj;
    }

}
