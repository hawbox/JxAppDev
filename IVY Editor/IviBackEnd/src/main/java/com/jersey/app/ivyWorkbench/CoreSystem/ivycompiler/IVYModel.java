
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.IVYExpression;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.simple.*;
//import com.jersey.app.ivyWorkbench.Logger;

/**
 *
 * @author Jose.Campos@di.uminho.pt
 */
public class IVYModel {

    private Map<String,String> defines = new LinkedHashMap<String,String>();
    private Map<String,IVYType> types = new LinkedHashMap<String,IVYType>();
    private Map<String, IVYInteractor> interactors = new LinkedHashMap<String,IVYInteractor>();

    public void addDefine(IVYDefine idef) {
        this.defines.put(idef.getName(), idef.getExpr());
    }

    public void addType(IVYType itype) {
        this.types.put(itype.getName(), itype);
    }

    public boolean actionHasParam(String intName, String acName) {
        return this.interactors.get(intName).actionHasParam(acName);
    }

    void addInteractor(IVYInteractor i) {
        this.interactors.put(i.getName(), i);
    }

    @Override
    public String toString() {
        return "Defines: "+this.defines.toString()+"\n"+
                "Types: "+this.types.values().toString()+"\n"+
                "Interactors: "+this.interactors.values().toString();
    }
    
    public JSONObject toJSONObject(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("typeClass", "IVYModel");
            JSONArray array = new JSONArray();
            if (defines != null) {
                for(Entry<String,String> e : defines.entrySet()){
                    JSONObject obj2 = new JSONObject();
                    obj2.put("name", e.getKey());
                    obj2.put("value", e.getValue());
                    array.add(obj2);
                }
            }
            obj.put("defines", array);
            array = new JSONArray();
            if (types != null) {
                for (Entry<String,IVYType> e : types.entrySet()) {
//                    System.out.println(e.getKey());
                    JSONObject obj2 = new JSONObject();
                    obj2.put("name", e.getKey());
                    obj2.put("type", e.getValue().toJSONObject());
                    array.add(obj2);
                }
            }
            obj.put("types", array);
            array = new JSONArray();
            if (interactors != null) {
                for (Entry<String,IVYInteractor> e : interactors.entrySet()) {
//                    System.out.println(e.getKey());
                    JSONObject obj2 = new JSONObject();
                    obj2.put("name", e.getKey());
                    obj2.put("interactor", e.getValue().toJSONObject());
                    array.add(obj2);
                }
            }
            obj.put("interactors", array);
        } catch (Exception e) {
            //Logger.alert(e, "JSONObject builder error", "Error while building JSONObject at class IVYModel");
        }
        return obj;
    }

    /**
     * Generates SMV code
     * 
     * @return the SMV code
     * @throws ivycompiler.IVYCompilerException
     */
    public String toSMV() throws IVYCompilerException {
        StringBuilder sb = new StringBuilder();

        toSMV(sb, "main");

        return sb.toString();
    }

    /**
     * Unfolds axioms by replacing axioms with lists of actions by multiple axioms
     */
    void unfoldAxioms() {
        for(IVYInteractor i: this.interactors.values())
            i.unfoldAxioms();
    }

    /**
     * Generates SMV code for an interactor and its aggregated interactors
     *
     * @param sb a StringBuilder where the code is generated into
     * @param iname the interactor name
     * @throws ivycompiler.IVYCompilerException
     */
    private void toSMV(StringBuilder sb, String iname) throws IVYCompilerException {
        IVYInteractor i = this.interactors.get(iname);
        if (i==null)
            throw new IVYCompilerException("Missing "+iname+" interactor.");

        for(IVYAggregation agg: i.getAggregations()) {
            String iaggname = agg.getInteractorName();
            toSMV(sb, iaggname);
        }
        sb.append(i.toSMV()).append("\n");

    }

    public IVYAction getAction(String intName, String acName) {
        return this.interactors.get(intName).getAction(acName);
    }

    /**
     * Get a definition given its name.
     *
     * @param key the name of the definition
     * @return the definition with the given name (or null)
     */
    public String getDefinition(String key) {
        return defines.get(key);
    }

    /**
     * Get the names of all definitions.
     *
     * @return a set with the names of the definitions
     */
    public Set<String> getDefNames() {
        return defines.keySet();
    }

    /**
     * Get an interactor given its name.
     * 
     * @param name the name of the interactor
     * @return the interactor with the given name (or null)
     */
    public IVYInteractor getInteractor(String name) {
        return this.interactors.get(name);
    }

    /**
     * Follows an aggregation path to find an interactor
     * 
     * @param interactor where to start looking
     * @param path the aggregation path
     * @return the aggregated interactor
     * @throws IVYCompilerException
     */
    public IVYInteractor getInteractor(IVYInteractor startPoint, String[] path) throws IVYCompilerException {
        IVYInteractor interactor = startPoint;
        if (path != null) {
            try {
                for (String agr : path) {
                    interactor = this.getInteractor(interactor.getAggregation(agr).getInteractorName());
                }
            }
            catch (NullPointerException e) {
                StringBuilder sb = new StringBuilder("invalid path: ");
                for (String agr : path) {
                    sb.append(agr);
                    sb.append(".");
                }
                sb.append(" in interactor ");
                sb.append(startPoint.getName());
                throw new IVYCompilerException(sb.toString());
            }
        }
        return interactor;
    }

    public boolean hasType(String type) {
        return this.types.containsKey(type);
    }

    public IVYType getType(String attribType) {
        return this.types.get(attribType);
    }

    public boolean hasConstant(String name) {
        for (IVYType t: this.types.values())
            if (t.contains(name))
                return true;
        return false;
    }

    void setupStuttering(Compiler compiler) throws IVYCompilerException {
        for(IVYInteractor i: this.interactors.values())
            i.setupStuttering(compiler);
    }

    void translateModalAxioms() {
    }

    void translateObligations() {
    }

    void translatePermissions() {
    }

    void unfoldActions() throws IVYCompilerException {
        for(IVYInteractor i: this.interactors.values())
            if (!i.hasParams())
                i.unfoldActionsUse();
        for(IVYInteractor i: this.interactors.values())
            if (!i.hasParams())
                i.unfoldActionsDeclaration();
    }

    /**
     * unfolds arrays in the model
     * 
     * @throws ivycompiler.IVYCompilerException
     */
    void unfoldArrays() throws IVYCompilerException {
        for(IVYInteractor i: this.interactors.values())
            i.unfoldArrays();
    }

    /**
     * Creates parameter-less interactors for all parameteresied aggregations
     *
     * @throws ivycompiler.IVYCompilerException
     */
    void unfoldInteractors() throws IVYCompilerException {
        IVYInteractor i = this.interactors.get("main");
        if (i==null)
            throw new IVYCompilerException("Missing main interactor");

        this.interactors.putAll(i.unfoldInteractors());
    }

    void unfoldModalAxioms() {
    }

    void unfoldPermissions() {
    }

    public Iterable<String> getTypeNames() { return types.keySet(); }

    public Iterable<String> getInteractorNames() { return interactors.keySet(); }

    
///   P V S
    
    /**
     * Generates PVS code
     * 
     * @return the PVS code
     * @throws CoreSystem.Server.ivycompiler.IVYCompilerException
     */
    public String toPVS() throws IVYCompilerException {
        StringBuilder sb = new StringBuilder();

        if (!this.types.isEmpty()) {
            sb.append("types_and_constants_th: THEORY\n BEGIN\n\n  %-- defines\n");
            for(Entry<String,String> e: this.defines.entrySet())
                sb.append("  ").append(e.getKey()).append(" : ").append(e.getValue()).append("\n");
            sb.append("  %-- type definitions\n");
            for(Entry<String,IVYType> e: this.types.entrySet())
                if (!e.getKey().equals("boolean")) {
                    if (e.getValue() instanceof IVYEnumType)
                        sb.append("  ").append(e.getKey()).append(" : TYPE = ").append(e.getValue()).append("\n");
                    else if (e.getValue() instanceof IVYRangeType) {
                        IVYRangeType irt = (IVYRangeType) e.getValue();
                        sb.append("  ").append(e.getKey())
                                .append(" : TYPE = { x: int | x >= ")
                                .append(irt.getLowerBound()).append(" AND x <= ")
                                .append(irt.getUpperBound()).append(" }\n");
                    }
                    else
                        throw new IVYCompilerException("Type "+e.getKey()+" not supported in PVS translation");
                }
            sb.append("\n END types_and_constants_th\n\n");
        }
        
        
        toPVS(sb, "main");

        return sb.toString();
    }


    /**
     * Generates PVS code for an interactor (and its aggregated interactors?)
     *
     * @param sb a StringBuilder where the code is generated into
     * @param iname the interactor name
     * @throws CoreSystem.Server.ivycompiler.IVYCompilerException
     */
    public void toPVS(StringBuilder sb, String iname) throws IVYCompilerException {
        IVYInteractor i = this.interactors.get(iname);
        if (i==null)
            throw new IVYCompilerException("Missing "+iname+" interactor.");

        //for(IVYAggregation agg: i.getAggregations()) {
        //    String iaggname = agg.getInteractorName();
        //    toSMV(sb, iaggname);
        //}
        sb.append(i.toPVS()).append("\n");

    }


}
