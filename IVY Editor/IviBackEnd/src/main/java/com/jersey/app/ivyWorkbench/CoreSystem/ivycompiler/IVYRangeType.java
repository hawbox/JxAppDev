
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYConstExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYAttribExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYDefRefExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYNumberExpression;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

/**
 *
 * @author jfc
 */
public class IVYRangeType extends IVYType {

    private String lowerBound;
    private String upperBound;

    public IVYRangeType(String tname, String lowerBound, String upperBound, Compiler compiler) {
        super(tname, compiler);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    private IVYRangeType(IVYRangeType aThis) {
        this(aThis.getName(), aThis.lowerBound, aThis.upperBound, aThis.compiler);
    }

    @Override
    public String toString() {
        return lowerBound+".."+upperBound;
    }
    
    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("min", lowerBound);
        obj.put("max", upperBound);
        obj.put("typeClass", "IVYRangeType");
        return obj;
    }

    @Override
    public boolean contains(String expr) {
        int min = Integer.parseInt(lowerBound);
        int max = Integer.parseInt(upperBound);
        int value;
        try {
            value = Integer.parseInt(expr);
        } catch(NumberFormatException e) {
            return false;
        }
        return min<=value && max>=value;
    }

    @Override
    public boolean contains(IVYExpression expr) throws IVYCompilerException {
        int min = Integer.parseInt(lowerBound);
        int max = Integer.parseInt(upperBound);

        
        // If it is a definition we need to replace it
        if (expr instanceof IVYDefRefExpression) 
            expr = ((IVYDefRefExpression)expr).getDef();
        
        if (expr instanceof IVYConstExpression) {
            int v = Integer.parseInt(((IVYConstExpression)expr).getValue());
            return v>=min && v<=max;
        }
        if (expr instanceof IVYAttribExpression) {
            IVYAttribExpression att = (IVYAttribExpression) expr;
            String[] path = att.getPath();
            String name = att.getName();
            if (name.startsWith("_"))
                return true;
            IVYInteractor inter = super.compiler.model.getInteractor(super.compiler.currentInteractor, path);
            return this.contains(inter.getAttribute(name).getType());
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o!=null && o instanceof IVYRangeType) {
            IVYRangeType t = (IVYRangeType) o;
            return t.lowerBound.equals(this.lowerBound) && t.upperBound.equals(this.upperBound);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.lowerBound != null ? this.lowerBound.hashCode() : 0);
        hash = 73 * hash + (this.upperBound != null ? this.upperBound.hashCode() : 0);
        return hash;
    }

    public boolean contains(IVYType type) {
        if (!(type instanceof IVYRangeType))
            return false;
        IVYRangeType rt = (IVYRangeType) type;
        int min = Integer.parseInt(lowerBound);
        int max = Integer.parseInt(upperBound);
        int rtMin = Integer.parseInt(rt.lowerBound);
        int rtMax = Integer.parseInt(rt.upperBound);
        return min<=rtMin && max>=rtMax;
    }

    @Override
    public List<String> values() {
        int min = Integer.parseInt(lowerBound);
        int max = Integer.parseInt(upperBound);
        List <String> values = new ArrayList<String>();
        for (int i = min; i<=max; i++)
            values.add(""+i);
        return values;
    }

    @Override
    public IVYType clone() {
        return new IVYRangeType(this);
    }
    
    public String getUpperBound() {
        return this.upperBound;
    }

    public String getLowerBound() {
        return this.lowerBound;
    }

    @Override
    public String toSMV() {
        return this.lowerBound+" .. "+this.upperBound;
    }

}
