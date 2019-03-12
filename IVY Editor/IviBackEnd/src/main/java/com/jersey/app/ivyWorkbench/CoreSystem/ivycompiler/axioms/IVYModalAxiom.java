package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.axioms;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYActionRefExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYAttribExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;


/**
 *
 * @author jfc
 */
public class IVYModalAxiom extends IVYActionAxiom {

    private IVYExpression pre;

    public IVYModalAxiom(IVYExpression pre, ArrayList<IVYActionRefExpression> iac, IVYExpression iexpr) {
        super(iac, iexpr);
        this.pre = pre;
        super.text = this.toString();
    }

    private IVYModalAxiom(IVYExpression pre, ArrayList<IVYActionRefExpression> acs, IVYExpression iexpr, String text) {
        super(acs, iexpr);
        this.pre = pre;
        super.text = text;
    }

    public ArrayList<IVYActionRefExpression> getListActions() {
        return actions;
    }

    public IVYExpression getPre() {
        return pre != null ? pre.clone() : null;
    }

    public String toString() {
        return (pre != null ? pre.toString() + "-> " : "") + "[" + actions.toString() + "] " + expr.toString();
    }

    @Override
    public IVYAxiom clone() {
        ArrayList<IVYActionRefExpression> acs = new ArrayList<IVYActionRefExpression>();
        for (IVYActionRefExpression ac : this.actions) {
            acs.add((IVYActionRefExpression) ac.clone());
        }
        return new IVYModalAxiom(this.pre != null ? (IVYExpression) this.pre.clone() : pre,
                acs, (IVYExpression) this.expr.clone(), super.text);
    }

    @Override
    protected void replaceVar(IVYAttribExpression attr, IVYType type, String v) {
        if (pre != null) {
            this.pre = this.pre.replaceVar(attr, type, v);
        }
        super.replaceVar(attr, type, v);
    }

    @Override
    public String toSMV() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n  --");
        sb.append(super.text);
        for (IVYActionRefExpression ac : this.actions) {
            ac.setPrime(true);
            sb.append("\n  TRANS ");
            if (pre != null) {
                sb.append(pre.toSMV());
                sb.append(" -> ");
            }
            sb.append(ac.toSMV());
            sb.append(" -> ");
            sb.append(expr.toSMV());
        }
        return sb.toString();
    }

    @Override
    protected void unfoldActionsInExpr(IVYInteractor i) throws IVYCompilerException {
        super.expr = super.expr.unfoldActions(i);
    }

    @Override
    protected void unfoldAnonArrayRefs(IVYInteractor i) throws IVYCompilerException {
        super.expr = super.expr.unfoldAnonArrayRefs(i);
    }

    /**
     * Unfolds the axiom by creating an axiom for each action in the list of
     * actions
     *
     * @param i the current interactor;
     * @return a list of axioms with only on action each
     */
    @Override
    public List<IVYAxiom> unfoldAxioms() {
        List<IVYAxiom> res = new ArrayList<IVYAxiom>();
        ArrayList<IVYActionRefExpression> list;
        for (IVYActionRefExpression ac : this.actions) {
            list = new ArrayList<IVYActionRefExpression>();
            list.add(ac);
            res.add(new IVYModalAxiom(this.pre, list, this.expr, super.text));
        }
        return res;
    }

    @Override
    public String toJSON() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            if (pre != null) {
                obj.put("preExpression", this.pre.toJSONObject());
            } else {
                obj.put("preExpression", "null");
            }
            JSONArray array = new JSONArray();
            for (IVYActionRefExpression exp : actions) {
                array.add(exp.toJSONObject());
            }
            obj.put("actions", array);
        } catch (Exception e) {
            //Logger.alert(e, "JSONObject builder error", "Error while building JSONObject at class IVYModalAxiom");
        }
        obj.put("postExpression", this.expr.toJSONObject());
        obj.put("typeAxiom", "IVYModalAxiom");
        return obj;
    }

}
