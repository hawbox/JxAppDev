
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author jfc
 */
public abstract class IVYType {
    private final String name;
    protected Compiler compiler;

    protected IVYType(String name, Compiler compiler) {
        this.name = name;
        this.compiler = compiler;
    }

    /**
     * @return the typeName
     */
    public String getName() {
        return name;
    }

    public int getDimensions() {
        return 0;
    }

    public abstract boolean contains(String expr);

    public abstract boolean contains(IVYExpression paramExpr) throws IVYCompilerException;

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if ((o == null) || (o.getClass() != this.getClass()))
            return false;
        IVYType t = (IVYType) o;
        return this.name.equals(t.getName());
    }

    public abstract List<String> values();

    @Override
    public abstract IVYType clone();

    public abstract boolean contains(IVYType tdef);

    /**
     * Calculates the list of array lower bounds of a type.
     * @return an empty list (this is the general case)
     */
    protected List<Integer> getLBs() {
        return new ArrayList<Integer>();
    }

    /**
     * Calculates the list of array upper bounds of a type.
     * @return an empty list (this is the general case)
     */
    protected List<Integer> getUBs() {
        return new ArrayList<Integer>();
    }

    /**
     * Method to be used for  attribute declarations - remember 
     * that type definitions are not present at SMV level
     * @return the SMV code for the type 
     */
    public abstract String toSMV();
    public abstract JSONObject toJSONObject();
}
