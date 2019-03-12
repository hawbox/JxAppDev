
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
public class IVYPermissionAxiom extends IVYActionAxiom {

    public IVYPermissionAxiom(ArrayList<IVYActionRefExpression> iac, IVYExpression iexpr) {
        super(iac, iexpr);
        super.text = this.toString();
    }

    private IVYPermissionAxiom(ArrayList<IVYActionRefExpression> acs, IVYExpression clone, String text) {
        super(acs, clone);
        super.text = text;
    }

    public ArrayList<IVYActionRefExpression> getListActions() {
        return actions;
    }

    @Override
    public String toString()  {
        return "per(" + actions.toString() + ") -> " + expr.toString();
    }

    @Override
    public IVYAxiom clone() {
        ArrayList<IVYActionRefExpression> acs = new ArrayList<IVYActionRefExpression>();
        for (IVYActionRefExpression ac: this.actions)
            acs.add((IVYActionRefExpression)ac.clone());
        return new IVYPermissionAxiom(acs, expr.clone(), super.text);
    }

    @Override
    public String toSMV() {
        StringBuilder sb = new StringBuilder();

            sb.append("\n  --");
            sb.append(super.text);
        for (IVYActionRefExpression ac: this.actions) {
            ac.setPrime(true);
            sb.append("\n  TRANS ");
            sb.append(ac.toSMV());
            sb.append(" -> ");
            sb.append(expr.toSMV());

                }
        return sb.toString();
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
            res.add(new IVYPermissionAxiom(list, this.expr, super.text));
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
            //Logger.alert(e, "JSONObject builder error", "Error while building JSONObject at class IVYPermissionAxiom");
        }
        obj.put("expression", this.expr.toJSONObject());
        obj.put("typeAxiom", "IVYPermissionAxiom");
        return obj;
    }

}

