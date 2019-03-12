package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.axioms;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYAttribExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;
import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author jfc
 */
public class IVYInvariantAxiom extends IVYAxiom {

    protected IVYExpression expr;

    public IVYInvariantAxiom() {
        super();
    }

    public IVYInvariantAxiom(IVYExpression iexpr) {
        super(iexpr.toString());
        this.expr = iexpr;
    }

    private IVYInvariantAxiom(IVYExpression clone, String text) {
        this.expr = clone;
        super.text = text;
    }

    @Override
    public List<IVYAxiom> unfoldActions(IVYInteractor inter) throws IVYCompilerException {
        List<IVYAxiom> newAxioms = super.unfoldActions(inter);

        expr = expr.unfoldVariables(inter);

        return newAxioms;
    }

    public String toString() {
        return expr.toString();
    }

    @Override
    public IVYAxiom clone() {
        return new IVYInvariantAxiom(expr.clone(), super.text);
    }

    @Override
    protected void replaceVar(IVYAttribExpression attr, IVYType type, String v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toSMV() {
        String smvExpr = expr.toSMV();
        if (smvExpr.contains("next(")) {
            return "\n  --" + super.text + "\n  TRANS " + smvExpr;
        } else {
            return "\n  --" + super.text + "\n  INVAR " + smvExpr;
        }
    }

    @Override
    public String toJSON() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("expression", expr.toJSONObject());
        obj.put("typeAxiom", "IVYInvariantAxiom");
        return obj;
    }
}
