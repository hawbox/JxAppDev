
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.axioms;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;

/**
 *
 * @author jfc
 */
public class IVYFairnessAxiom extends IVYInvariantAxiom {

    public IVYFairnessAxiom(IVYExpression iexpr) {
        super(iexpr);
    }

    @Override
    public String toSMV() {
        String smvExpr = super.expr.toSMV();
        return "  FAIRNESS " + smvExpr;
    }

}

