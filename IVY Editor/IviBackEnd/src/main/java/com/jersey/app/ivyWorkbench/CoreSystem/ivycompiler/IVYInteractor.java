
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.axioms.IVYAxiom;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.axioms.IVYModalAxiom;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYActionRefExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;

/**
 *
 * @author Jose.Campos@di.uminho.pt
 */
public class IVYInteractor {
    private String name;
    private List<IVYEmptyType> params = new ArrayList<IVYEmptyType>();
    private Set<String> allIds = new HashSet<String>();
    private Map<String,IVYAttribute> attributes = new HashMap<String,IVYAttribute>();
    private Map<String,IVYAction> actions = new HashMap<String,IVYAction>();
    private List<IVYAxiom> axioms = new ArrayList<IVYAxiom>();
    private Map<String,IVYAggregation> aggregations = new HashMap<String,IVYAggregation>(); // AGGREGATION!!
    private List<IVYExpression> theorems = new ArrayList<IVYExpression>();
    private boolean SMVdone = false;
    private IVYModel model;
    private IVYAxiom fairness;

    /**
     * Create an IVY interactor instance
     * @param name
     * @param model 
     */
    protected IVYInteractor(String name, IVYModel model) {
        this.name = name;
        this.params = null;
        this.model = model;
        this.allIds.add(name);
    }

    /**
     * Create an IVY interactor instance (by clone)
     * @param i 
     */
    protected IVYInteractor(IVYInteractor i) {
        name = i.name;
        model = i.model;
        if (i.params == null)
            params = null;
        else {
            params.addAll(i.params);
        }
        allIds.addAll(i.allIds);
        for(Map.Entry<String,IVYAttribute> e: i.attributes.entrySet())
            attributes.put(e.getKey(), e.getValue().clone());
        for(Map.Entry<String,IVYAction> e: i.actions.entrySet())
            actions.put(e.getKey(), e.getValue().clone());
        for(IVYAxiom ax: i.axioms)
            axioms.add(ax.clone());
        for(Map.Entry<String,IVYAggregation> e: i.aggregations.entrySet())
            aggregations.put(e.getKey(), e.getValue().clone());
        for(IVYExpression t : i.theorems)
            theorems.add(t.clone());
    }

    protected IVYInteractor(String name, List<IVYEmptyType> ltypes, IVYModel model) {
        this(name, model);
        this.params = ltypes;
    }

    void addAction(IVYAction action) throws IVYCompilerException {
        String acName = action.getName();

        if (this.usedId(acName))
            throw new IVYCompilerException(0, acName, "duplicated identifier");

        this.actions.put(acName, action);
        this.allIds.add(acName);
    }

    void addAggregation(IVYAggregation aggreg) {
        this.aggregations.put(aggreg.getName(), aggreg);
        this.allIds.add(aggreg.getName());
    }

    void addAttribute(IVYAttribute attrib) {
        this.attributes.put(attrib.getName(), attrib);
        this.allIds.add(attrib.getName());
    }

    public boolean usedId(String name) {
        return this.allIds.contains(name);
    }
    
    public boolean isAction(String name)
    {
        return actions.containsKey(name);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public IVYAction getAction(String acName) {
        return this.actions.get(acName);
    }

    public IVYModel getModel() {
        return model;
    }

    @Override
    public String toString() {
        String s = "\ninteractor "+this.name+"\n"+
               " aggregations "+this.aggregations.toString()+"\n"+
               " attributes "+this.attributes.values().toString()+"\n"+
               " actions "+this.actions.values().toString()+"\n"+
               " axioms \n";
        for (IVYAxiom a : this.axioms)
            s += (a+"\n");
        return s;
    }
    
    public JSONObject toJSONObject(){
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        try {
            obj.put("typeClass", "IVYInteractor");
            JSONArray array = new JSONArray();
            if (!actions.isEmpty()) {
                for(IVYAction a : actions.values()){
                    array.add(a.toJSONObject());
                }
            }
            obj.put("actions", array);
            array = new JSONArray();
            if (!attributes.isEmpty()) {
                for(IVYAttribute a : attributes.values()){
                    array.add(a.toJSONObject());
                }
            }
            obj.put("attributes", array);
            array = new JSONArray();
            if (!aggregations.isEmpty()) {
                for(IVYAggregation a : aggregations.values()){
                    array.add(a.toJSONObject());
                }
            }
            obj.put("aggregations", array);
            array = new JSONArray();
            if (!axioms.isEmpty()) {
                for(IVYAxiom a : axioms){
                    array.add(a.toJSONObject());
                }
            }
            obj.put("axioms", array);
            array = new JSONArray();
            if (!theorems.isEmpty()) {
                for(IVYExpression e : theorems){
                    array.add(e.toJSONObject());
                }
            }
            obj.put("theorems", array);
            if(fairness!=null){ obj.put("fairness", fairness.toJSONObject());}
        } catch (Exception e) {
            //Logger.alert(e, "JSONObject builder error", "Error while building JSONObject at class IVYInteractor");
        }
        return obj;
    }
    
    public void printTheorem(){
        System.out.println(this.theorems);
    }
    
    public List<IVYExpression> getTheorems(){
        return this.theorems;
    }

    public void addAxiom(IVYAxiom axiom) {
        this.axioms.add(axiom);
    }
    
    public boolean actionHasParam(String acName) {
        return this.actions.get(acName).hasParam();
    }

    public IVYAttribute getAttribute(String name) {
        return this.attributes.get(name);
    }

    public void addCTLAxiom(IVYExpression t) {
        this.theorems.add(t);
    }

    public IVYAggregation getAggregation(String agr) {
        return this.aggregations.get(agr);
    }

    void addFairness(IVYAxiom expr) {
//        throw new UnsupportedOperationException("Not yet implemented");
        this.fairness = expr;
    }

    public Collection<IVYAction> getActions() {
        return this.actions.values();
    }

    public Iterable<IVYAxiom> getAxioms() {
        return this.axioms;
    }



    public Iterable<IVYAggregation> getAggregations() {
        return this.aggregations.values();
    }

    public Iterable<IVYAttribute> getAttributes() {
        return this.attributes.values();
    }

    /**
     * Whether a type is defined in the interactor's parameters
     *
     * @param type the type name
     * @return true if the list of parameters contains type
     */
    boolean hasParam(String type) {
        boolean has = false;
        for (int i=0; has && i<this.params.size(); i++)
            has = this.params.get(i).getName().equals(name);
        return has;
    }

    /**
     * Whether the interactor has parameters
     *
     * @return true is the interactor has parameters
     */
    boolean hasParams() {
        return params!=null && !params.isEmpty();
    }

    void setupStuttering(Compiler compiler) throws IVYCompilerException {
        if (this.attributes.size()>0) {
            if (!this.actions.containsKey("nil")) 
                this.addAction(new IVYAction("nil", new ArrayList(), compiler));
            List<String> vars = new ArrayList<String>();
            IVYActionRefExpression iac = new IVYActionRefExpression(null, "nil", null, null,false);
            ArrayList<IVYActionRefExpression> list = new ArrayList<IVYActionRefExpression>();
            list.add(iac);
            StringBuilder sb = new StringBuilder("");
            for (IVYAttribute at: this.attributes.values()) {
                vars.add(at.getName());
                sb.append(at.getName()).append("'=").append(at.getName()).append(" & ");
            }
            String ax = sb.toString().replaceFirst(" \\& \\z", "");
            IVYExpression iexpr = compiler.parseExpression(ax, vars, false);
            IVYAxiom mAx = new IVYModalAxiom(null, list, iexpr);
            this.axioms.addAll(mAx.unfoldArrays(this));
        }
    }

    /**
     * Generates SMV code for this interactor (if it has not already been generated)
     *
     * @return a String with the code
     * @throws ivycompiler.IVYCompilerException
     */
    String toSMV() throws IVYCompilerException {
        if (SMVdone)
            return "";
        StringBuilder sb = new StringBuilder("\nMODULE "+this.name+"\n");
        if (!this.aggregations.isEmpty()) {
            sb.append("\n  -- aggregations\n");
            sb.append("  VAR \n");
            for (IVYAggregation agg: this.aggregations.values()) {
                sb.append(agg.toSMV());
                sb.append("\n");
            }
        }
        if (!this.attributes.isEmpty()) {
            sb.append("\n  -- attributes\n");
            sb.append("  VAR \n");
            for (IVYAttribute at: this.attributes.values()) {
                sb.append("    ");
                sb.append(at.toSMV());
                sb.append("\n");
            }
        }
        if (!this.actions.isEmpty()) {
            sb.append("\n  -- actions\n");
            sb.append("  VAR \n");
            sb.append("    action: {");
            for (IVYAction ac: this.actions.values())
                sb.append(ac.toSMV()).append(", ");
            sb.delete(sb.length()-2, sb.length()-1);
            sb.append("};\n");
        }

        sb.append("\n  -- axioms \n");
        for (IVYAxiom a : this.axioms)
            sb.append(a.toSMV()).append("\n");
        if(!this.actions.isEmpty())
            sb.append("  INIT action = nil");

        if (this.fairness!=null) {
            sb.append("\n  -- fairness: ");
            sb.append(this.fairness.toString());
            sb.append("\n");
            sb.append(this.fairness.toSMV());
            sb.append("\n");
        }

        if (this.theorems.size() > 0) {
            sb.append("\n  -- specifications \n");
            for (IVYExpression t : this.theorems)
                sb.append("  SPEC\n    ").append(t.toSMV()).append("\n");
        }
        SMVdone = true;

        return sb.toString().replaceAll(", \\}", "}");
    }

    /**
     * Replaces variables in actions parameters by concrete values by 
     * calculating all possible combinations.
     * 
     * @throws ivycompiler.IVYCompilerException
     */
    void unfoldActionsUse() throws IVYCompilerException {
        List<IVYAxiom> newAxioms = new ArrayList<IVYAxiom>();
        for (IVYAxiom ax: this.axioms) {
            newAxioms.addAll(ax.unfoldActions(this));
        }
        this.axioms = newAxioms;
    }

    /**
     * Replaces parameterised action declarations by concrete values by
     * calculating all possible combinations.
     *
     * @throws ivycompiler.IVYCompilerException
     */
    void unfoldActionsDeclaration() {
        Map<String, IVYAction> newActions = new HashMap<String,IVYAction>();
        for (IVYAction ac: this.actions.values())
            newActions.putAll(ac.unfoldActions());
        this.actions = newActions;
    }

    /**
     * Unfolds arrays in the interactor
     * 
     * @throws ivycompiler.IVYCompilerException
     */
    void unfoldArrays() throws IVYCompilerException {
        List<IVYAxiom> newAxioms = new ArrayList<IVYAxiom>();
        for (IVYAxiom ax: this.axioms)  {
            newAxioms.addAll(ax.unfoldArrays(this));
        }
        this.axioms = newAxioms;
    }

    /**
     * Unfolds axioms by replacing axioms with lists of actions by multiple axioms
     */
    void unfoldAxioms() {
        List<IVYAxiom> newAxioms = new ArrayList<IVYAxiom>();

        for (IVYAxiom ax: this.axioms)
            newAxioms.addAll(ax.unfoldAxioms());
        this.axioms = newAxioms;
    }

    Map<String, IVYInteractor> unfoldInteractors() {
        Map<String, IVYInteractor> res = new HashMap<String, IVYInteractor>();
        Map<String,IVYAggregation> newAggs = new HashMap<String,IVYAggregation>();

        for (Map.Entry<String,IVYAggregation> e : this.aggregations.entrySet()) {
            if (e.getValue().hasTypes()) {
                IVYAggregation agg = e.getValue();
                List<IVYType> types = agg.getTypes();
                IVYInteractor newi = model.getInteractor(agg.getInteractorName()).clone();
                newi.replaceTypes(types);
                newi.params = new ArrayList<IVYEmptyType>();

                newAggs.put(e.getKey(), new IVYAggregation(agg.getName(), newi.getName(), new ArrayList<IVYType>()));
                res.put(newi.getName(), newi);

            } else
                newAggs.put(e.getKey(), e.getValue());
        }
        this.aggregations = newAggs;

        return res;
    }

    /**
     * Deep copy clone.
     *
     * @return a deep copy of the interactor
     */
    @Override
    public IVYInteractor clone() {
        return new IVYInteractor(this);
    }

    /**
     * Updates an interactor with parameters replaced by concrete types
     * 
     * @param types a list of types
     */
    private void replaceTypes(List<IVYType> types) {
        StringBuilder sb = new StringBuilder(name);

        for (IVYType t: types) {
            sb.append("_");
            sb.append(t.getName());
        }
        name = sb.toString();

        for (int i=0; i<types.size(); i++) {
            for(IVYAttribute att: this.attributes.values())
                att.replaceType(this.params.get(i), types.get(i));
            for(IVYAction act: this.actions.values())
                act.replaceType(this.params.get(i), types.get(i));
        }

    }

    /**
     * Generates PVS code for this interactor 
     *
     * @return a String with the code
     * @throws IVYCompilerException
     */
    public String toPVS() throws IVYCompilerException {
        StringBuilder sb = new StringBuilder(this.name+"_th: THEORY\n BEGIN\n");
        if (!model.getDefNames().isEmpty() || model.getTypeNames().iterator().hasNext())
            sb.append(" %-- type definitions\n IMPORTING types_and_constants_th\n");
        if (!this.aggregations.isEmpty()) {
            throw new IVYCompilerException("Aggregations not supported in PVS translation.");
        }
        if (!this.attributes.isEmpty()) {
            sb.append(" main : TYPE = [#\n");
            for (IVYAttribute at: this.attributes.values()) {
                sb.append("   ").append(at.getName()).append(" : ").append(at.getType());
                sb.append("\n");
            }
            sb.append("  #]");
        }
        
        // Permission axioms
        
        // Implement by looping through axioms for each action
        
        
        
        /*if (!this.actions.isEmpty()) {
            sb.append("\n  -- actions\n");
            sb.append("  VAR \n");
            sb.append("    action: {");
            for (IVYAction ac: this.actions.values())
                sb.append(ac.toSMV()).append(", ");
            sb.delete(sb.length()-2, sb.length()-1);
            sb.append("};\n");
        }*/

        sb.append("\n  -- axioms \n");
        JOptionPane.showMessageDialog(null,"Axiom translation still not implemented!","Error Compiling to PVS",JOptionPane.WARNING_MESSAGE);
        //for (IVYAxiom a : this.axioms) {
        //    sb.append(a.toPVS());
        //}
        sb.append("  INIT action = nil");

        if (this.fairness!=null) {
            sb.append("\n  -- fairness: ");
            sb.append(this.fairness.toString());
            sb.append("\n");
            sb.append(this.fairness.toSMV());
            sb.append("\n");
        }

        if (this.theorems.size() > 0) {
            sb.append("\n  -- specifications \n");
            for (IVYExpression t : this.theorems)
                sb.append("  SPEC\n    ").append(t.toSMV()).append("\n");
        }
        SMVdone = true;

        return sb.toString().replaceAll(", \\}", "}");
    }
}
