
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;


/**
 *
 * @author jfc
 */
public class IVYAction {

    private String name = "";
    private List<IVYType> types = new ArrayList<IVYType>();
    private Compiler compiler;

    public IVYAction(String name, List<IVYType> types, Compiler compiler) {
        this.name = name;
        this.compiler= compiler;
        this.types = new ArrayList<IVYType>();
        this.types.addAll(types);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public boolean hasParam() {
        return types.size() > 0;
    }

    @Override
    public String toString() {
        return "action "+name+(types.size()>0?"("+types+")":"");
    }

    public JSONObject toJSONObject(){
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        try {
            obj.put("typeClass", "IVYAction");
            JSONArray array = new JSONArray();
            if (types!=null) {
                for(IVYType t : types){
                    array.add(t.getName());
                }
            }
            obj.put("types", array);
        } catch (Exception e) {
            //Logger.alert(e, "JSONObject builder error", "Error while building JSONObject at class IVYAction");
        }
        return obj;
    }
    
    public List<IVYType> getTypes() {
        return types;
    }

    void replaceType(IVYEmptyType ptype, IVYType ctype) {
        for (int i=0; i<this.types.size(); i++)
            if (types.get(i).equals(ptype))
                types.set(i, ctype);
    }

    String toSMV() {
        StringBuilder sb = new StringBuilder(name);
        for (IVYType t: types)
            sb.append("_").append(t.getName());
        return sb.toString();
    }

    Map<String,IVYAction> unfoldActions() {
        Map<String,IVYAction> unfolded = new HashMap<String,IVYAction>();

        unfolded.put(name, this);

        for (IVYType t: this.types) {
            List<String> values = t.values();
            unfolded = unfoldActions(unfolded, values);
        }
       
        return unfolded;
    }


    // THis only works for one types actions - needs recursions!!!
    Map<String,IVYAction> unfoldActions(Map<String,IVYAction> actions, List<String> values)  {
        Map<String,IVYAction> unfolded = new HashMap<String,IVYAction>();

        if (types.isEmpty())
            unfolded.putAll(actions);
        else
            for (IVYAction ac: actions.values()) {
                for (String v: values) {
                    IVYAction newAc = ac.clone();
                    newAc.name = ac.getName()+"_"+v;
                    newAc.types = new ArrayList<IVYType>();
                    unfolded.put(newAc.name, newAc);
                }
            }
        return unfolded;
    }

    @Override
    public IVYAction clone() {
        return new IVYAction(name, types, compiler);
    }
}
