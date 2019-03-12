package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.*;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYUnaryExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYEqualityExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYConstExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYBinaryExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYAttribExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYArrayAttribExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;


/**
 * This class represents an array type
 *
 * @author jfc
 */
public class IVYArrayType extends IVYType {

    private String lowerBound;
    private String upperBound;
    private IVYType tdef;

    /**
     * Create an instance of an array type
     *
     * @param tname
     * @param lowerBound
     * @param upperBound
     * @param tdef
     * @param compiler
     */
    public IVYArrayType(String tname, String lowerBound, String upperBound, IVYType tdef, Compiler compiler) {
        super(tname, compiler);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.tdef = tdef;
    }

    /**
     * Create an instance of an array type (copy constructer)
     *
     * @param aThis
     */
    private IVYArrayType(IVYArrayType aThis) {
        this(aThis.getName(), aThis.lowerBound, aThis.upperBound, aThis.tdef.clone(), aThis.compiler);
    }

    /**
     * Return the dimensions
     *
     * @return
     */
    @Override
    public int getDimensions() {
        return 1 + tdef.getDimensions();
    }

    /**
     * Return textual representation
     *
     * @return
     */
    @Override
    public String toString() {
        return "array " + this.lowerBound + " .. " + this.upperBound + " of " + this.tdef;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("min", lowerBound);
        obj.put("max", upperBound);
        obj.put("tdef", tdef.getName());
        obj.put("typeClass", "IVYArrayType");
        return obj;
    }

    /**
     * Unimplemented!
     *
     * @param expr
     * @return
     */
    // TODO: define contains methods for arrays?!
    @Override
    public boolean contains(String expr) {
        return false;
    }

    /**
     * Verifies if array contains expression
     *
     * @param expr
     * @return
     * @throws IVYCompilerException
     */
    @Override
    public boolean contains(IVYExpression expr) throws IVYCompilerException {
        if (expr instanceof IVYConstExpression) {
            return this.contains(((IVYConstExpression) expr).getValue());
        }
        if (expr instanceof IVYAttribExpression) {
            IVYAttribExpression att = (IVYAttribExpression) expr;
            String[] path = att.getPath();
            String name = att.getName();
            if (name.startsWith("_")) {
                return true;
            }
            IVYInteractor inter = super.compiler.model.getInteractor(super.compiler.currentInteractor, path);
            return this.equals(inter.getAttribute(name).getType());
        }
        return false;
    }

    /**
     * Verfifies if array contains type
     *
     * @param o
     * @return
     */
    public boolean contains(IVYType o) {
        if (o != null && o instanceof IVYArrayType) {
            IVYArrayType t = (IVYArrayType) o;
            return t.lowerBound.equals(this.lowerBound)
                    && t.upperBound.equals(this.upperBound) && this.tdef.contains(t.tdef);
        }
        return false;
    }

    /**
     * Verifies if two instances are equal
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof IVYArrayType) {
            IVYArrayType t = (IVYArrayType) o;
            return t.lowerBound.equals(this.lowerBound)
                    && t.upperBound.equals(this.upperBound) && t.tdef.equals(this.tdef);
        }
        return false;
    }

    /**
     * The hashcode
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.lowerBound != null ? this.lowerBound.hashCode() : 0);
        hash = 13 * hash + (this.upperBound != null ? this.upperBound.hashCode() : 0);
        hash = 13 * hash + (this.tdef != null ? this.tdef.hashCode() : 0);
        return hash;
    }

    /**
     * Calculates all possibles values of the array...
     *
     * @return
     */
    @Override
    public List<String> values() {
        int min = Integer.parseInt(lowerBound);
        int max = Integer.parseInt(upperBound);
        List<String> list = tdef.values(); // this.values(this.tdef);
        List<StringBuilder> newList = new ArrayList<StringBuilder>();
        newList.add(new StringBuilder());
        boolean first = true;
        for (int i = min; i <= max; i++) {
            List<StringBuilder> auxList = new ArrayList<StringBuilder>();
            for (StringBuilder sb : newList) {
                for (String s : list) {
                    StringBuilder sbCopy = new StringBuilder(sb);
                    if (first || s.startsWith("-")) {
                        sbCopy.append(s);
                    } else {
                        sbCopy.append("-").append(s);
                    }
                    auxList.add(sbCopy);
                }
                if (first) {
                    first = false;
                }
            }
            newList = auxList;
        }
        list = new ArrayList<String>();
        for (StringBuilder sb : newList) {
            list.add(sb.toString());
        }
        return list;
    }

    /**
     * Creates an equality between an array attribute and a value for the array.
     * The array value must be expressed in the v1-v2-v2-... notation
     *
     * @param aux the attribute to be used
     * @param values the values for the attribute
     * @return a conjunction of equalities properly indexed
     */
    // TODO: check for 3+ dimensional arrays
    public IVYExpression createEquality(IVYAttribExpression aux, String[] values) {
        IVYExpression res;
        int dim = this.getDimensions();
        List<IVYEqualityExpression> auxList, resList = new ArrayList<IVYEqualityExpression>();
        List<Integer> lbs = this.getLBs();
        List<Integer> ubs = this.getUBs();

        int baseIdx = 0; // ubs.get(0)-lbs.get(0)+1;
        for (int ind = lbs.get(0); ind <= ubs.get(0); ind++) {
            String[] auxArr = new String[dim];
            auxArr[0] = "" + ind;
            resList.add(
                    new IVYEqualityExpression(
                            new IVYArrayAttribExpression(aux.getPath(), aux.getName(), auxArr, aux.getPrimed(), super.compiler.model),
                            new IVYConstExpression(values[baseIdx++])));
        }
        baseIdx = 0;
        for (int i = 1; i < dim; i++) {
            auxList = new ArrayList<IVYEqualityExpression>();
            for (IVYEqualityExpression expr : resList) {
                for (int ind = lbs.get(i); ind <= ubs.get(i); ind++) {
                    IVYEqualityExpression exprCopy = (IVYEqualityExpression) expr.clone();
                    ((IVYArrayAttribExpression) exprCopy.getLeft()).setIndexList(i, "" + ind);
                    if (i == dim - 1) {
                        exprCopy.setRight(new IVYConstExpression(values[baseIdx++]));
                    }
                    auxList.add(exprCopy);
                }
            }
            resList = auxList;
        }
        res = resList.get(0);
        for (int i = 1; i < resList.size(); i++) {
            res = new IVYBinaryExpression("&", res, resList.get(i));
        }
        return new IVYUnaryExpression("()", res);
    }

    /**
     * Return list of lower bounds
     *
     * @return
     */
    @Override
    protected List<Integer> getLBs() {
        List<Integer> res = tdef.getLBs();
        res.add(0, Integer.parseInt(lowerBound));
        return res;
    }

    /**
     * Return list of upper bounds
     *
     * @return
     */
    @Override
    protected List<Integer> getUBs() {
        List<Integer> res = tdef.getUBs();
        res.add(0, Integer.parseInt(upperBound));
        return res;
    }

    /**
     * unused method
     *
     * @param tdef
     * @return
     */
    private List<String> values(IVYType tdef) {
        List<String> list = new ArrayList<String>();

        if (tdef instanceof IVYArrayType) {
            list = tdef.values();
        } else {
            list.add("");
        }
        return list;
    }

    /**
     * Calculates all possibles indices of the array...
     *
     * @return
     */
    public List<String> indices() {
        int min = Integer.parseInt(lowerBound);
        int max = Integer.parseInt(upperBound);
        List<String> list = (tdef instanceof IVYArrayType) ? ((IVYArrayType) tdef).indices() : new ArrayList<String>();
        List<StringBuilder> newList = new ArrayList<StringBuilder>();
        for (int i = min; i <= max; i++) {
            newList.add(new StringBuilder("[" + i + "]"));
        }

        List<StringBuilder> auxList = new ArrayList<StringBuilder>();
        for (StringBuilder sb : newList) {
            if (list.size() > 0) {
                for (String s : list) {
                    StringBuilder sbCopy = new StringBuilder(sb);
                    sbCopy.append(s);
                    auxList.add(sbCopy);
                }
            } else {
                auxList.add(sb);
            }
        }
        list = new ArrayList<String>();
        for (StringBuilder sb : auxList) {
            list.add(sb.toString());
        }
        return list;
    }

    /**
     * Deep clone method
     *
     * @return
     */
    @Override
    public IVYType clone() {
        return new IVYArrayType(this);
    }

    /**
     * Returns array of elements types (clone)
     *
     * @return
     */
    public IVYType getArrayElementsType() {
        return tdef.clone();
    }

    /**
     * Return lower bound
     *
     * @return
     */
    public String getLowerBound() {
        return lowerBound;
    }

    /**
     * Return upper bound
     *
     * @return
     */
    public String getUpperBound() {
        return upperBound;
    }

    @Override
    public String toSMV() {
        StringBuilder sb = new StringBuilder();

        sb.append("array ");
        sb.append(this.lowerBound);
        sb.append("..");
        sb.append(this.upperBound);
        sb.append(" of ");
        sb.append(this.tdef.toSMV());
        return sb.toString();
    }

}
