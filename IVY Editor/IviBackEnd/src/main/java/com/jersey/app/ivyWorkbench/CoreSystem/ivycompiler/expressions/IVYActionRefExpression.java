package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYType;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYAction;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYArrayType;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYAttribute;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.IVYInteractor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;

/**
 *
 * @author jfc
 */
public class IVYActionRefExpression extends IVYExpression {

    private String[] path;
    private String name;
    private List<IVYExpression> params;
    private boolean primed, negated;

    public IVYActionRefExpression(String[] path, String acName, List<IVYExpression> acParams, String prime, boolean negate) {
        this.path = path;
        this.name = acName;
        this.params = acParams;
        this.primed = prime != null;
        this.negated = negate;
    }

    public IVYActionRefExpression(String[] path, String acName, List<IVYExpression> acParams, boolean prime, boolean negate) {
        this.path = path;
        this.name = acName;
        this.params = acParams;
        this.primed = prime;
        this.negated = negate;
    }

    public String getName() {
        return name;
    }

    public String[] getPath() {
        return path;
    }

    public void setPrime(boolean b) {
        this.primed = b;
    }

    public void setNegated(boolean b) {
        this.negated = b;
    }

    @Override
    public String toString() {
        String sPath = "", sParam = "";

        if (negated) {
            sPath = "!";
        }
        if (path != null) {
            for (String sa : path) {
                sPath += (sa + ".");
            }
        }
        if (params != null) {
            for (IVYExpression sa : params) {
                sParam += (sa.toString() + ",");
            }
            sParam = sParam.replaceFirst(",\\z", "");
        }
        return sPath + name + (sParam.equals("") ? "" : ("(" + sParam + ")")) + (primed ? "'" : "");
    }

    public List<IVYExpression> getParams() {
        List<IVYExpression> s = new ArrayList<IVYExpression>();
        if (params != null) {
            for (IVYExpression p : params) {
                s.add(p);
            }
        }
        return s;
    }

    public List<String> getParamNames() {
        List<String> s = new ArrayList<String>();
        if (params != null) {
            for (IVYExpression p : params) {
                if (p instanceof IVYAttribExpression) {
                    s.add(((IVYAttribExpression) p).getName());
                }
            }
        }
        return s;
    }

    @Override
    public IVYExpression clone() {
        List<IVYExpression> newParams = new ArrayList<IVYExpression>();
        for (IVYExpression e : this.params) {
            newParams.add(e.clone());
        }
        return new IVYActionRefExpression(this.path != null ? this.path.clone() : this.path,
                this.name, newParams, this.primed, this.negated);
    }

    @Override
    public IVYActionRefExpression replaceVar(IVYAttribExpression attr, IVYType type, String v) {
        List<IVYExpression> newParams = new ArrayList<IVYExpression>();
        for (IVYExpression p : this.params) {
            newParams.add(p.replaceVar(attr, type, v));
        }
        this.params = newParams;
        return this;
    }

    @Override
    public Set<IVYAttribExpression> getAttribsAsIndices(IVYInteractor i) {
        Set<IVYAttribExpression> list = new HashSet<IVYAttribExpression>();

        for (IVYExpression e : this.params) {
            list.addAll(e.getAttribsAsIndices(i));
        }
        return list;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.path != null ? this.path.hashCode() : 0);
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + (this.params != null ? this.params.hashCode() : 0);
        hash = 29 * hash + (this.primed ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IVYActionRefExpression other = (IVYActionRefExpression) obj;
        if (this.path != other.path && (this.path == null || !Arrays.equals(this.path, other.path))) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.params != other.params && (this.params == null || !this.params.equals(other.params))) {
            return false;
        }
        if (this.primed != other.primed) {
            return false;
        }
        if (this.negated != other.negated) {
            return false;
        }
        return true;
    }

    @Override
    public String toSMV() {
        StringBuilder sbPath = new StringBuilder();

        if (path != null) {
            for (String s : path) {
                sbPath.append(s);
                sbPath.append(".");
            }
        }
        sbPath.append("action");
        if (primed) {
            sbPath.insert(0, "next(");
            sbPath.append(")");
        }

        return "(" + sbPath.toString() + (this.negated ? "!=" : "=") + name + paramsAsSMV() + ")";
    }

    private String paramsAsSMV() {
        StringBuilder sbParam = new StringBuilder();
        if (params != null) {
            for (IVYExpression sa : params) {
                sbParam.append("_");
                sbParam.append(sa.toSMV());
            }
        }
        return sbParam.toString();
    }

    /**
     * Unfolds the parameters in the action by creating all possible
     * combinations. Attributes are replaced by the possible values according to
     * their type. Consider type T={a, b}, attribute at: T, and action ac(T).
     * The expression ac(at) becomes (at=a -> ac(a)) & (at=b -> ac(b)) Variables
     * are also replaced. ac(_v) becomes (ac(a) | ac(b)) -- this does not seem
     * right, at lest with effect!! -- consider effect(ac(_v)) <->
     * effect(ac2(_v))
     *
     * This is needed because parameters to actions must be concrete values
     * before they can be turned into SMV code.
     *
     * @param i the base interactor for this expression
     * @return the ufolding of the action parameters
     * @throws CoreSystem.Server.IVYCompilerException
     */
    @Override
    public IVYExpression unfoldActions(IVYInteractor i) throws IVYCompilerException {
        IVYExpression r = null;
        if (this.params != null && !this.params.isEmpty()) {
            r = createActionsUnfold(getPossibleValues(i), r, i).unfoldVariables(i);
        } else {
            r = this;
        }
        return r;
    }

    /**
     * Performs the actual unfolding of attributes used as action parameters,
     * given the possible values.
     *
     * One problem is when the action has a less/more restrictive type than the
     * the attribute!!
     *
     * @param valuesList the possible values
     * @param r the expression to build upon
     * @param i the interactor of this expression
     * @return the unfolding of the actions
     * @throws IVYCompilerException
     */
    private IVYExpression createActionsUnfold(List<List<IVYExpression>> valuesList, IVYExpression r, IVYInteractor i) throws IVYCompilerException {
        IVYExpression pre;
        IVYActionRefExpression pos;
        IVYExpression equality;
        IVYType type = null;

        for (int k = 0; k < valuesList.size(); k++) {
            List<IVYExpression> l = valuesList.get(k);
            pos = (IVYActionRefExpression) this.clone();
            pre = null;
            for (int j = 0; j < l.size(); j++) {
                IVYExpression param = this.params.get(j);
                IVYExpression value = l.get(j);
                if (!param.equals(value)) {
                    if (param instanceof IVYAttribExpression && value instanceof IVYConstExpression) {
                        IVYAttribExpression paramAsAttrExpr = (IVYAttribExpression) param;
                        IVYAttribute attrib = i.getModel().getInteractor(i, paramAsAttrExpr.getPath()).getAttribute(((IVYAttribExpression) param).getName());
                        if (attrib == null) {
                            throw new IVYCompilerException("Unexpected error: undeclared attribute " + param.toString() + " in interactor " + i.getName());
                        }
                        type = attrib.getType();
                        if (type instanceof IVYArrayType) {
                            equality = ((IVYArrayType) type).createEquality((IVYAttribExpression) param, ((IVYConstExpression) value).getValue().split("-"));
                        } else {
                            equality = new IVYEqualityExpression(param, value);
                        }
                    } else {
                        equality = new IVYEqualityExpression(param, value);
                    }

                    if (pre == null) {
                        pre = equality;
                    } else {
                        pre = new IVYBinaryExpression("&", pre, equality);
                    }
                    pos.params.set(j, l.get(j));
                }
                //  pos.path = null;
            }
            if (pre != null) {
                if (r == null) {
                    r = new IVYUnaryExpression("()", new IVYBinaryExpression("->", pre, pos.clone()));
                } else {
                    r = new IVYBinaryExpression("&", r, new IVYUnaryExpression("()", new IVYBinaryExpression("->", pre, pos.clone())));
                }
            } else if (r == null) {
                r = pos.clone();
            } else {
                r = new IVYBinaryExpression("&", r, new IVYUnaryExpression("()", pos.clone()));
            }
 //  This is nolonger needed as we are being strict about parameters' types
            //  (in any case it does not work! - action are not unfolded in the interactor)
            //           if (interAc.getAction(pos.name+pos.paramsAsSMV())==null) {
            //               try {
            //                   interAc.addAction(new IVYAction(pos.name+pos.paramsAsSMV(), null));
            //                   pos.path=null;
            //                   interAc.addAxiom(new IVYModalAxiom(null, pos, new IVYConstExpression("false")));
            //              } catch (IVYCompilerException e) {}
            //           }
        }
        return new IVYUnaryExpression("()", r);
    }

    /**
     * Calculates all the permutations of values of the parameters of the
     * action, taking into consideration the possible values of variables (from
     * their type).
     *
     * @param i the interactor of this expression
     * @return a list of lists containing all the possible inputs
     */
    private List<List<IVYExpression>> getPossibleValues(IVYInteractor i) throws IVYCompilerException {
        List<List<IVYExpression>> valuesList = new ArrayList<List<IVYExpression>>();
//            IVYInteractor inter = ModelCompiler.model.getInteractor(i, path);
        IVYInteractor interAtt;
        Collection<String> values;
        IVYAttribExpression att;
        for (IVYExpression expr : this.params) {
            List<List<IVYExpression>> tempList;
            List<IVYExpression> valueList;
            if (expr instanceof IVYAttribExpression
                    && !(att = (IVYAttribExpression) expr).getName().startsWith("_")) {
                interAtt = i.getModel().getInteractor(i, att.getPath());
                IVYAttribute attr = interAtt.getAttribute(att.getName());
                if (attr == null) {
                    throw new IVYCompilerException(0, this.toString(), "Unkown attribute " + att.getName());
                }

                values = attr.getType().values();
                if (valuesList.isEmpty()) {
                    for (String v : values) {
                        valueList = new ArrayList<IVYExpression>();
                        valueList.add(new IVYConstExpression(v));
                        valuesList.add(valueList);
                    }
                } else {
                    tempList = new ArrayList<List<IVYExpression>>();
                    for (List<IVYExpression> l : valuesList) {
                        for (String v : values) {
                            valueList = new ArrayList<IVYExpression>();
                            for (IVYExpression le : l) {
                                valueList.add(le.clone());
                            }
                            valueList.add(new IVYConstExpression(v));
                            tempList.add(valueList);
                        }
                    }
                    valuesList = tempList;
                }
//                valuesList.add(valueList);
            } else {
                if (valuesList.isEmpty()) {
                    valueList = new ArrayList<IVYExpression>();
                    valueList.add(expr);
                    valuesList.add(valueList);
                } else {
                    tempList = new ArrayList<List<IVYExpression>>();
                    for (List<IVYExpression> l : valuesList) {
                        valueList = new ArrayList<IVYExpression>();
                        for (IVYExpression le : l) {
                            valueList.add(le.clone());
                        }
                        valueList.add(expr);
                        tempList.add(valueList);
                    }
                    valuesList = tempList;
                }
            }
        }
        return valuesList;
    }

    /**
     * Replaces _variables by all possible combinations of their values.
     * Consider type T = {a, b} and action ac(T), ac(_v) becomes (ac(a) | ac(b))
     *
     * @param inter the interactor of this expression
     * @return an expression with all possible combinations
     * @throws ivycompiler.IVYCompilerException
     */
    @Override
    public IVYExpression unfoldVariables(IVYInteractor inter) throws IVYCompilerException {
        IVYExpression res;
        List<IVYActionRefExpression> unfolded, preUnfolded = new ArrayList<IVYActionRefExpression>();
        preUnfolded.add(this);

        inter = inter.getModel().getInteractor(inter, this.path);
        if (inter == null) {
            throw new IVYCompilerException(0, inter.getName(), "undeclared interactor found during action unfold");
        }
        IVYAction acDef = inter.getAction(this.name);
        if (acDef == null) {
            throw new IVYCompilerException(0, this.name, "undeclared action found during action unfold");
        }
        for (int i = 0; i < params.size(); i++) {
            IVYExpression p = params.get(i);
            if (p instanceof IVYAttribExpression) {
                IVYAttribExpression attr = (IVYAttribExpression) p;
                if (attr.getName().startsWith("_")) {
                    IVYType type = acDef.getTypes().get(i);
                    List<String> values = type.values();
                    unfolded = new ArrayList<IVYActionRefExpression>();
                    for (IVYActionRefExpression acRef : preUnfolded) {
                        IVYActionRefExpression newAcRef;
                        for (String v : values) {
                            newAcRef = (IVYActionRefExpression) acRef.clone();
                            unfolded.add((IVYActionRefExpression) newAcRef.replaceVar(attr, type, v).clone());
                        }
                    }
                    preUnfolded = unfolded;
                }
            }
        }
        res = preUnfolded.get(0);
        for (int i = 1; i < preUnfolded.size(); i++) {
            IVYActionRefExpression acRef = preUnfolded.get(i);
            res = new IVYBinaryExpression("|", res, acRef);
        }

        return new IVYUnaryExpression("()", res);
    }

    @Override
    public String toJSON() {
        return toJSONObject().toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("name", name);
            obj.put("primed", primed);
            obj.put("negated", negated);
            obj.put("typeExpr", "IVYActionRefExpression");
            JSONArray array = new JSONArray();
            if (path != null) {
                array.addAll(Arrays.asList(path));
            }
            obj.put("path", array);
            array = new JSONArray();
            if (params != null) {
                for (IVYExpression exp : params) {
                    array.add(exp.toJSONObject());
                }
            }
            obj.put("params", array);
        } catch (Exception e) {
            //Logger.alert(e, "JSONObject builder error", "Error while building JSONObject at class IVYActionRefExpression");
        }
        return obj;
    }

}
