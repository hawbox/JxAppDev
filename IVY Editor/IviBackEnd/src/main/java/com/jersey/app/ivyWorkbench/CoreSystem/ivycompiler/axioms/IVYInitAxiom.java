package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.axioms;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYAttribExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYAttribute;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.Compiler;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;

import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;


/**
 *
 * @author jfc
 */
public class IVYInitAxiom extends IVYAxiom {

    private IVYExpression expr;

    public IVYInitAxiom(IVYExpression iexpr) {
        super(iexpr.toString());
        this.expr = iexpr;
    }

    private IVYInitAxiom(IVYExpression clone, String text) {
        this.expr = clone;
        super.text = text;
    }

    public String toString() {
        return "[] " + expr.toString();
    }

    @Override
    public IVYAxiom clone() {
        return new IVYInitAxiom(expr.clone(), super.text);
    }

    @Override
    protected void replaceVar(IVYAttribExpression attr, IVYType type, String v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toSMV() {
        return "\n  --" + super.text + "\n  INIT " + expr.toSMV();
    }

    @Override
    public List<IVYAxiom> unfoldArrays(IVYInteractor i) throws IVYCompilerException {
        List<IVYAxiom> newAx = super.unfoldArrays(i);
        Set<IVYAttribExpression> usedAttribs = this.expr.getAttribsAsIndices(i);

        for (IVYAttribExpression attr : usedAttribs) {
            String[] path = attr.getPath();
            String name = attr.getName();
            IVYAttribute attrDef = i.getModel().getInteractor(i, path).getAttribute(name);
            newAx = super.unfoldArrays(newAx, attr, attrDef.getType());
        }
        return newAx;
    }

    public IVYExpression getExpr() {
        return expr;
    }

    @Override
    public String toJSON() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("expression", expr.toJSONObject());
        obj.put("typeAxiom", "IVYInitAxiom");
        return obj;
    }

}
