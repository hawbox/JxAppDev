
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import com.jersey.app.ivyWorkbench.Logger;

/**
 *
 * @author jfc
 */
public class IVYAggregation {

    private String name;
    private String iname;
    private List<IVYType> types;


    public IVYAggregation(String name, String iname, List<IVYType> types) {
        this.name = name;
        this.iname = iname;
        this.types = types;
    }

    /** Copy constructior
     *
     * @param aThis the Aggregation to be copied
     */
    private IVYAggregation(IVYAggregation aThis) {
        name = aThis.name;
        iname = aThis.iname;
        types = new ArrayList<IVYType>();
        types.addAll(aThis.types);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "aggregates "+iname+(types.size()!=0?"("+types.toString()+"":"")+" via "+name;
    }

    public JSONObject toJSONObject(){
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("iname", iname);
        try {
            obj.put("typeClass", "IVYAggregation");
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
    
    public String getInteractorName() {
        return this.iname;
    }

    public String toSMV() throws IVYCompilerException {
        if (types.size()!=0)
            throw new IVYCompilerException(0,this.toString(), "Aggregations with parameters must be unfold before code generation.");
        return "    "+name+": "+iname+";";
    }

    /**
     * Whether the aggregation has (un-unfolded) parameters
     *
     * @return true is it has parameters to be unfolded
     */
    public boolean hasTypes() {
        return this.types.size()!=0;
    }

    /**
     * Deep copy clone
     *
     * @return a deep copy of the aggregation
     */
    public IVYAggregation clone() {
        return new IVYAggregation(this);
    }

    /**
     * The types used as parameters in this aggregation.
     * 
     * @return a List of type names
     */
    List<IVYType> getTypes() {
        return this.types;
    }
}
