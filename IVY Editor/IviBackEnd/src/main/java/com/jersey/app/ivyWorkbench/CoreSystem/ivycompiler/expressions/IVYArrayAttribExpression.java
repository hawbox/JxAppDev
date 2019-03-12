
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.Compiler;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYModel;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jfc
 */
public class IVYArrayAttribExpression extends IVYAttribExpression {

    // TODO: replace String by IVYAttribExpression?
    private String[] indexList;

    public IVYArrayAttribExpression(String[] path, String name, String[] indexList, boolean prime, IVYModel model) {
        super(path, name, prime);
        this.indexList = indexList;
        for (int i=0; i<indexList.length; i++)
            if (model.getDefinition(indexList[i])!=null)
                indexList[i] = model.getDefinition(indexList[i]);
    }
    
    public IVYArrayAttribExpression(IVYArrayAttribExpression expr) {
        super(expr.getPath()!=null?expr.getPath().clone():expr.getPath(), expr.getName(), expr.getPrimed());
        this.indexList = expr.indexList.clone();
    }
    
    public Iterable<String> getIndexList() { return Arrays.asList(indexList); }

    @Override
    public String toString() {
        String s="";

        for(String e: indexList)
            s+=("["+e+"]");
        return super.toString()+s;
    }

    @Override 
    public IVYExpression clone() { 
        return new IVYArrayAttribExpression(this);
    }

    @Override
    public Set<IVYAttribExpression> getAttribsAsIndices(IVYInteractor i) {
        Set<IVYAttribExpression> list = new HashSet<IVYAttribExpression>();

        for (String index: this.indexList) {
            if (i.getAttribute(index)!=null)
                list.add(new IVYAttribExpression(null, index, false));
        }
        return list;
    }

    /**
     * Replaces a variable used as index in an array expression
     * 
     * @param attr
     * @param type
     * @param v
     */
    // TODO: Check understanding above
    @Override
    public IVYExpression replaceVar(IVYAttribExpression attr, IVYType type, String v) {
        for(int i=0; i<this.indexList.length; i++)
            if (attr.getName().equals(this.indexList[i]) && attr.getPath()==null)
                this.indexList[i] = v;
        return this;
    }


   @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IVYArrayAttribExpression other = (IVYArrayAttribExpression) obj;
        if (this.indexList != other.indexList && (this.indexList == null || !this.indexList.equals(other.indexList))) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 83 * hash + (this.indexList != null ? this.indexList.hashCode() : 0);
        return hash;
    }

    @Override
    public String toSMV() {
        String s="";

        for(String e: indexList)
            s+=("["+e+"]");
        if (super.getPrimed())
            s=super.toSMV().replaceAll("\\)\\z", s+")");
        else
            s=super.toSMV()+s;
        return s;
    }

    public void setIndexList(int i, String string) {
        this.indexList[i] = string;
    }

}
