
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.axioms;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYActionRefExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYAction;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYAttribExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYAttribute;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author jfc
 */
public abstract class IVYActionAxiom extends IVYAxiom {

    protected ArrayList<IVYActionRefExpression> actions;
    protected IVYExpression expr;

    protected IVYActionAxiom() {
    }

    public IVYActionAxiom(ArrayList<IVYActionRefExpression> iac, IVYExpression iexpr) {
        this.actions = iac;
        this.expr = iexpr;
    }

    public Iterator<IVYActionRefExpression> getActions() {
        return actions.iterator();
    }

    public IVYExpression getExpression() {
        return expr.clone();
    }

    @Override
    public abstract IVYAxiom clone();

    /**
     * Replaces a variable/attribute by a value
     *
     * @param attr
     * @param v
     */
    @Override
    protected void replaceVar(IVYAttribExpression attr, IVYType type, String v) {
        ArrayList<IVYActionRefExpression> newActions = new ArrayList<IVYActionRefExpression>();
        for (IVYActionRefExpression ac: this.actions)
            newActions.add(ac.replaceVar(attr, type, v));
        this.actions = newActions;
        this.expr = this.expr.replaceVar(attr, type, v);
    }

    public abstract String toString();

    /**
     * Replaces variables in actions parameters by possible values.
     * Does two things:
     * a) works on the actions and extends the substitution to
     * the expression
     * [ac(_x)] a'=_x  becomes [ac(v)] a'=v for all v in the parameter type
     * b) works on the expression by calculating all possible combinations
     * ac(_x) becomes (ac(v)|...) for all v in the parameter type
     *
     * @param inter the interactor of this axioms
     * @return a list of axioms for the unfolded actions
     * @throws ivycompiler.IVYCompilerException
     */
    @Override
    public List<IVYAxiom> unfoldActions(IVYInteractor inter) throws IVYCompilerException {
        List<IVYAxiom> newAxioms = super.unfoldActions(inter);

        for (IVYActionRefExpression ac: this.actions) {
            List<IVYExpression> params = ac.getParams();

            if (!params.isEmpty()) {
                for(int i = 0; i<params.size(); i++) {
                    IVYExpression p = params.get(i);
                    if (p instanceof IVYAttribExpression) {
                        IVYAttribExpression attr = (IVYAttribExpression) p;
                        String attrName = attr.getName();
                        if (attrName.startsWith("_")) {
                            String[] path = ac.getPath();
                            String name = ac.getName();
                            IVYAction acDef = inter.getModel().getInteractor(inter, path).getAction(name);
                            if (acDef == null)
                                throw new IVYCompilerException(0,p.toString(),"undelcared action found during action unfold");
                            IVYType type = acDef.getTypes().get(i);
                            newAxioms = this.unfoldActions(newAxioms, attr, type);
                        }
                    }
                }
            }
        }
        for (IVYAxiom ax: newAxioms) 
            ax.unfoldActionsInExpr(inter);
        return newAxioms;
    }

    /**
     * Replaces variables in actions parameters by possible values.
     *
     * @param newAxioms the current list of axioms
     * @param attr the attribute to be replaces
     * @param values the possible values of the attribute
     * @return a new list of axioms with the attribute replaced by the possible values
     */
    // TODO make thsi static or true instance method (is this already in another method?)
    private List<IVYAxiom> unfoldActions(List<IVYAxiom> newAxioms, IVYAttribExpression attr, IVYType type) {
        List<IVYAxiom> list = new ArrayList<IVYAxiom>();
        List<String> values = type.values();

        for (IVYAxiom ax: newAxioms) {
            for (String v : values) {
                IVYAxiom newAx = (IVYAxiom) ax.clone();
                newAx.replaceVar(attr, type, v);
                list.add(newAx);
            }
        }
        return list;
    }

    /**
     * Unfolds arrays in this axiom
     *
     * @param i interactor providing the scope for this axiom
     * @return list of axioms resulting from array unfold
     * @throws ivycompiler.IVYCompilerException
     */
    @Override
    public List<IVYAxiom> unfoldArrays(IVYInteractor i) throws IVYCompilerException {
        List<IVYAxiom> newAxs = super.unfoldArrays(i);
        Set<IVYAttribExpression> usedAttribs = this.expr.getAttribsAsIndices(i);

        for (IVYAttribExpression attr: usedAttribs) {
            String[] path = attr.getPath();
            String name = attr.getName();
            IVYAttribute attrDef = i.getModel().getInteractor(i, path).getAttribute(name);
            newAxs = this.unfoldArrays(newAxs, attr, attrDef.getType());
        }
        return  newAxs;
    }

}
