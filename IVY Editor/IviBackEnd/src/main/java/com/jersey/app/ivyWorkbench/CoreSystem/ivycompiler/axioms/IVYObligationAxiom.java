
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.axioms;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYActionRefExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;


/**
 *
 * @author jfc
 */
public class IVYObligationAxiom extends IVYActionAxiom {

    public IVYObligationAxiom(ArrayList<IVYActionRefExpression> iac, IVYExpression iexpr) {
        super(iac, iexpr);
        super.text = this.toString();
    }

    private IVYObligationAxiom(ArrayList<IVYActionRefExpression> acs, IVYExpression clone, String text) {
        super(acs, clone);
        super.text = text;
    }

    public String toString () {
        return expr.toString()+" -> obl("+actions.toString()+")";
    }

    @Override
    public IVYAxiom clone() {
        ArrayList<IVYActionRefExpression> acs = new ArrayList<IVYActionRefExpression>();
        for (IVYActionRefExpression ac: this.actions)
            acs.add((IVYActionRefExpression)ac.clone());
        return new IVYObligationAxiom(acs, expr.clone(), super.text);
    }

    @Override
    // TODO DO it!! (validate that there is only one action!)
    public String toSMV() {
        throw new UnsupportedOperationException("Obligations not yet implemented");
/*        StringBuffer sb = new StringBuffer();

        for (IVYActionRefExpression ac: this.actions) {
            sb.append("\n  -- Obligation: ");
            sb.append(super.text);
            sb.append(" -> obl(");
            sb.append(actions.toString());
            sb.append(")");
        }
        return sb.toString();*/
    }

    /**
     * Unfolds the axiom by creating an axiom for each action in the list of actions
     *
     * @param i the current interactor;
     * @return a list of axioms with only on action each
     */
    @Override
    public List<IVYAxiom> unfoldAxioms() {
        List<IVYAxiom> res = new ArrayList<IVYAxiom>();
        ArrayList<IVYActionRefExpression> list;
        for(IVYActionRefExpression ac: this.actions) {
            list = new ArrayList<IVYActionRefExpression>();
            list.add(ac);
            res.add(new IVYObligationAxiom(list, this.expr, super.text));
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
            JSONArray array = new JSONArray();
            for (IVYActionRefExpression exp : actions) {
                array.add(exp.toJSONObject());
            }
            obj.put("actions", array);
        } catch (Exception e) {
            //Logger.alert(e, "JSONObject builder error", "Error while building JSONObject at class IVYObligationAxiom");
        }
        obj.put("expression", this.expr.toJSONObject());
        obj.put("typeAxiom", "IVYObligationAxiom");
        return obj;
    }

}
