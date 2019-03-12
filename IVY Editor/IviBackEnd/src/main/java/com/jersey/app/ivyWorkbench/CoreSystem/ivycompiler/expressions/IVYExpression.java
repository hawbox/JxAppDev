
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;
import java.util.Set;
import org.json.simple.JSONObject;

public abstract class IVYExpression {

    /* For now only for CTL */
    private String comment=null;   
    public void setComment(String s) {comment = s;}
    public String getComment() {return comment;}

 //   public static IVYExpression parseExpression(String expr) throws IVYCompilerException {
 //       return parseExpression(expr, new HashSet<String>());
 //   }

    @Override
    public abstract IVYExpression clone();

    /**
     * Replaces _variables by all possible combinations of their values.
     * Consider type T = {a, b} and action ac(T), ac(_v) becomes (ac(a) | ac(b))
     *
     * @param inter the interactor of this expression
     * @return an expression with all possible combinations
     * @throws IVYCompilerException
     */
    public IVYExpression unfoldVariables(IVYInteractor inter) throws IVYCompilerException {
        return this;
    }

    @Override
    public abstract int hashCode();
    
    @Override
    public abstract boolean equals(Object obj);

    protected IVYExpression () {}

    public abstract Set<IVYAttribExpression> getAttribsAsIndices(IVYInteractor i);

    public abstract IVYExpression replaceVar(IVYAttribExpression attr, IVYType type, String v);

    public abstract String toSMV();
    
    public abstract String toJSON();
    
    public abstract JSONObject toJSONObject();

    public IVYExpression unfoldActions(IVYInteractor i) throws IVYCompilerException {
        return this;
    }

    /**
     * Unfold arrays without explicit indices.
     *
     * @param i the interactor for this expression
     * @return teh expression with arrays unfolded
     * @throws IVYCompilerException
     */
    public IVYExpression unfoldAnonArrayRefs(IVYInteractor i) throws IVYCompilerException {
        return this;
    }

  // public abstract void unfoldActionsUse(IVYInteractor inter);

  //  @Override
  //  public abstract String toString();
}

