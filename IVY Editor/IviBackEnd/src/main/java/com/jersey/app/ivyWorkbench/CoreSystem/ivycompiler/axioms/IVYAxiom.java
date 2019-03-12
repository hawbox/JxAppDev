
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.axioms;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYAttribExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYBinaryExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYConstExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYEqualityExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYUnaryExpression;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author jfc
 */
public abstract class IVYAxiom {

    protected String text = "";

    protected IVYAxiom () {}

    protected IVYAxiom (String text) {
        this.text = text;
    }

    public abstract String toSMV();
    
    public abstract String toJSON();
    
    public abstract JSONObject toJSONObject();

    @Override
    public abstract String toString();

    // No actions in init axioms  - TODO: is this checked?!
    // No actions in invariants - TODO: is this checked? effect??
    public List<IVYAxiom> unfoldActions(IVYInteractor i) throws IVYCompilerException {
         List<IVYAxiom> list = new ArrayList<IVYAxiom>();
         list.add(this);
         return list;
    }

    /**
     * 
     * @param i interactor providing the scope for this axiom
     * 
     * @return List of axioms resulting from unfolding this axiom
     * @throws ivycompiler.IVYCompilerException
     */
    public List<IVYAxiom> unfoldArrays(IVYInteractor i)  throws IVYCompilerException {
        List<IVYAxiom> list = new ArrayList<IVYAxiom>();
        this.unfoldAnonArrayRefs(i);
        list.add(this);
        return list;
    }

    @Override
    public abstract IVYAxiom clone();

    protected abstract void replaceVar(IVYAttribExpression attr,IVYType type, String v);

    /**
     * replaves references to an attribute by its possible values.
     *
     * @param newAxioms the current list of axioms
     * @param attr the attribute to be replaces
     * @param values the values to use
     * @return a new list of axioms
     */
    protected List<IVYAxiom> unfoldArrays(List<IVYAxiom> newAxioms, IVYAttribExpression attr, IVYType type) {
        List<IVYAxiom> list = new ArrayList<IVYAxiom>();
        List<String> values = type.values();

        for (IVYAxiom ax: newAxioms) {
            for (String v : values) {
                // TO DO: explain the cast below!!
                IVYActionAxiom newAx = (IVYActionAxiom) ax.clone();
                newAx.replaceVar(attr, type, v);
                IVYExpression pre = new IVYEqualityExpression(attr, new IVYConstExpression(v));
                newAx.expr = new IVYUnaryExpression("()", new IVYBinaryExpression("->", pre, newAx.expr));
                list.add(newAx);
            }
        }
        return list;
    }

    protected void unfoldActionsInExpr(IVYInteractor inter) throws IVYCompilerException {
        // TODO: Nothing to do except for modal axioms?
    }

    /**
     * Unfold anonymous array references
     * 
     * @param i interactor providing the scope for this axiom
     * @throws ivycompiler.IVYCompilerException
     */
    protected void unfoldAnonArrayRefs(IVYInteractor i) throws IVYCompilerException {
        // TODO: Nothing to do except for modal axioms?
    }

    /**
     * Unfolds axioms. Nothing to do except for IVYActionAxiom subclasses
     *
     * @return in this case, a list with the axiom
     */
    public List<IVYAxiom> unfoldAxioms() {
        List<IVYAxiom> list = new ArrayList<IVYAxiom>();
        list.add(this);
        return list;
    }


}

