/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jersey.app.ivyWorkbench.CoreSystem;

import java.util.*;
import ivyWorkbench.Misc.PatternHelper;

/**
 * <p>Title: Visualizer Component</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author Nuno Sousa
 * @version 2.0
 */

public class TraceData{
  private HashMap<String, ArrayList<String>> data;
  private ArrayList<String> states; //all the states stored as strings
  private int loop = -1;
  private String property;

  /**
   * Creates an empty data structure.
   */
  public TraceData() {
    data = new HashMap<String, ArrayList<String>> ();
    states = new ArrayList<String> ();
    loop=-1;
    property="";
  }

  public TraceData(HashMap<String, ArrayList<String>> dat, ArrayList<String> stat, int l) {
      data=dat;
      states=stat;
      loop=l;
  }
  
  public void setProperty(String prop) {
      property=prop;
  }
  
  public String getProperty() {
      return property;
  }  

    public HashMap<String, ArrayList<String>> getData() {
        return data;
    }
  
  
  
     /**
   * Creates a new data set copy of TData.
   *
   * @param bData BasicData
   */
  public TraceData getTraceData() {
    HashMap<String, ArrayList<String>> dat = new HashMap<String, ArrayList<String>> ();

    ArrayList<String> stat = new ArrayList<String> ();

    Iterator keys = data.entrySet().iterator();
    
    String aux = "";
    
    while(keys.hasNext()) { 
      String e = (String)((Map.Entry)keys.next()).getKey();
      ArrayList<String> vt = data.get(e);
      ArrayList<String> res = new ArrayList<String> ();
      for (int k = 0; k < vt.size(); k++) {
        res.add(vt.get(k));
      }
      dat.put(e, res);
    }

    ArrayList<String> sta = getStates();

    for (int m = 0; m < sta.size(); m++) {
      stat.add(sta.get(m));
    }

    int loo = getLoop();

    return new TraceData(dat, stat, loo);
  }

  /**
   * Returns all the atributes of a state by interactor.
   *
   * @param state int
   * @param inter String
   * @param subFiltering boolean
   * @return ArrayList
   */
  public ArrayList<String> getActribsStateInteractor(int state, String inter) {
    ArrayList<String> var = this.getAtribInteractor(inter);
    ArrayList<String> result = new ArrayList<String> ();
    String at, val;
    for (int i = 0; i < var.size(); i++) {
      at = this.getCell(var.get(i), state - 1);
      val = (String) var.get(i);
      result.add(val + "=" + at);
    }
    return result;
  }

  /**
   * Returns a ArrayList with the names of the states.
   *
   * @return ArrayList
   */
  public ArrayList<String> getStates() {
    return states;
  }

  /**
   * Returns the loop indice.
   * @return int
   */
  public int getLoop() {
    return loop;
  }

  /**
   * Returns all the values for one variable.
   *
   * @param var String
   * @return ArrayList
   */
  public ArrayList<String> getRow(String var) {
    return data.get(var);
  }

  /**
   * Returns the states where the variable had value.
   */
  public ArrayList<String> getStatesValue(String name, String value) {
    ArrayList<String> values = this.getRow(name);
    ArrayList<String> states = new ArrayList<String> ();
    String aux = "";
    for (int i = 0; i < values.size(); ++i) {
      if (values.get(i).equals(value)) {
        aux = getStates().get(i);
        states.add(aux);
      }
    }
    return states;
  }

  /**
   * Returns all the diferent values for a variable.
   *
   * @param name String
   * @return ArrayList
   */
  public ArrayList<String> getDifferentValues(String name) {
    ArrayList<String> values = getRow(name);
    int size = values.size();
    for (int i = 0; i < size; ++i) {
      if (values.get(i).equals(values.get(i + 1))) {
        // Delete all the repeated values
        values.remove(i);
        --size;
      }
    }
    return values;
  }

  /**
   * Returns the value of one variable in one state.
   *
   * @param name String
   * @param state int
   * @return String
   */
  public String getCell(String name, int state) {
    if (states.contains("1." + (state + 1))) {
      return getRow(name).get(states.indexOf("1." + (state + 1)));
    }
    else {
      return " ";
    }
  }

  /**
   * Returns all the values related to one state.
   *
   * @param state int
   * @return ArrayList
   */
  public ArrayList<String> getColumn(int state) {
    ArrayList<String> column = new ArrayList<String> ();
    ArrayList<String> aux = new ArrayList<String> ();
    String s = "1." + String.valueOf(state);
   
    Iterator keys = data.entrySet().iterator();
    
    while(keys.hasNext()) { 
      String e = (String)keys.next();
      aux = (ArrayList<String>) keys.next();
      for (int i = 0; i < aux.size(); ++i) {
        if (aux.get(i).equals(s)) {
          column.add(aux.get(i));
        }
      }
    }
    return column;
  }

  /**
   * Returns all the atributes of a state.
   *
   * @param state int
   * @param subFiltering boolean
   * @return ArrayList
   */
  public ArrayList<String> getActribsState(int state) {
    ArrayList<String> result = new ArrayList<String> ();
    ArrayList<String> var = this.getVariables();
    String val, sa;
    for (int i = 0; i < var.size(); i++) {
      sa = var.get(i);
      val = getCell(sa, state - 1);
      if (val.compareTo(" ")!=0 && val.compareTo("ACTION_BASE")!=0) {
        result.add(sa + "=" + val);
      }
    }
    return result;
  }

  /**
   * Returns all the actions performed in one state.
   *
   * @param state int
   * @param inter String
   * @param subFiltering boolean
   * @return ArrayList
   */
  public ArrayList<String> getActionsStateInteractor(int state, String inter) {
    String var = new String(inter + ".action");
    ArrayList<String> result = new ArrayList<String> ();
    String act = this.getCell(var, state - 1);
    result.add(act);
    return result;
  }

  /**
   * Returns the name of all the variables in the current model.
   *
   * @return ArrayList
   */
  public ArrayList<String> getVariables() {
    ArrayList<String> variables = new ArrayList<String> ();
    Iterator keys = data.entrySet().iterator();
    
    while(keys.hasNext()) { 
      String e = (String)keys.next();
       variables.add( (String) keys.next());
    }
    Collections.sort(variables);
    return variables;
  }

  /**
   * Returns all the actions performed in the model.
   *
   * @return ArrayList
   */
  public ArrayList<String> getActions() {
    ArrayList<String> actions = new ArrayList<String> ();
    
    Iterator keys = data.entrySet().iterator();
    
    while(keys.hasNext()) { 
      String e = (String)((Map.Entry)keys.next()).getKey();
      if ( e.substring(e.lastIndexOf(".") +1).equals("action")) 
        actions.add( (String) e);
      
    }
    return actions;
  }

  /**
   * Retuns the name of all interactors in this model.
   *
   * @return ArrayList
   */
  public ArrayList<String> getInteractors() {
    String key, interactor="";
    ArrayList<String> res = new ArrayList<String> ();
    ArrayList<String> a = new ArrayList<String> ();
   
    Iterator keys = data.entrySet().iterator();
    
    String aux = "";
    
    while(keys.hasNext()) { 
      String e = (String)keys.next();
      interactor="";
   
      key = (String) keys.next();
      a = PatternHelper.splitString(key);
      a.remove(a.size()-1);
      for(int i=0;i<a.size();i++) {
        interactor += a.get(i);
        if (i!=a.size()-1)
          interactor+=".";
      }
      if (!res.contains(interactor)) {
        res.add(interactor);
      }
    }

    //Ordering the interactors names
    Collections.sort(res);

    return res;
  }

  /**
   * Returns all the actions performed in one state.
   *
   * @param state int
   * @param subFiltering boolean
   * @return ArrayList
   */
  public ArrayList<String> getActionsState(int state) {
    ArrayList<String> variables = getVariables();
    ArrayList<String> result = new ArrayList<String> ();
    String aux = "";

    for (int i = 0; i < variables.size(); ++i) {
      if (variables.get(i).substring(variables.get(i).lastIndexOf(".") + 1).
          equals("action")) {
        aux = variables.get(i) + "=" + this.getCell(variables.get(i), state - 1);
        result.add(aux);
      }
    }
    return result;
  }

  /**
   * Returns all the values related to one state.
   *
   * @param state int
   * @param subFiltering boolean
   * @return ArrayList
   */
  public ArrayList<String> getValuesState(int state) {
    ArrayList<String> result = new ArrayList<String> ();
    ArrayList<String> variables = this.getVariables();

    for (int i = 0; i < variables.size(); ++i) {
      result.add(getCell(variables.get(i), state - 1));
    }

    return result;
  }

  /**
   * Returns the name of all atributes of an interactor.
   * @param inter String
   * @return ArrayList
   */
  public ArrayList<String> getAtribInteractor(String inter) {
    String k="";
    ArrayList<String> intera= new ArrayList<String>();
    ArrayList<String> intera1= new ArrayList<String>();
    ArrayList<String> res = new ArrayList<String> ();
    
    Iterator keys = data.entrySet().iterator();
    
    while(keys.hasNext()) { 
      k = (String) keys.next();
      intera= PatternHelper.splitString(k);
      intera.remove(intera.size()-1);
      intera1= PatternHelper.splitString(inter);
      if (k.startsWith(inter) && intera.size()==intera1.size() && !k.contains("action")) {
        res.add(k);
      }
    }
    return res;
  }

  /**
   * Returns the states where the variable changed to value.
   *
   * @param name String
   * @param value String
   * @return ArrayList
   */
  public ArrayList<String> getChangeStatesValue(String name, String value) {
    ArrayList<String> values = this.getRow(name);
    ArrayList<String> states = new ArrayList<String> ();

    if (values.size() == 1 && values.get(0).equals(value)) {
      String aux = this.getStates().get(0);
      states.add(aux);
    }
    else {
      for (int i = 1; i <= values.size(); ++i) {
        if (values.get(i - 1).equals(value)) {
          // Skip all the repeated values and add the state on the change was performed
          if (i == 1) {
            states.add("1." + String.valueOf(i));
          }
          else
          if (!values.get(i - 2).equals(value)) {
            states.add("1." + String.valueOf(i));
          }
        }
      }
    }
    return states;
  }

  /**
   * Returns all the states where variables in ValuesLHS have values in ValuesRHS.
   *
   * @param valuesLHS ArrayList
   * @param valuesRHS ArrayList
   * @return ArrayList
   */
  public ArrayList<String> getAllStatesValue(ArrayList<String> valuesLHS,
      ArrayList<String> valuesRHS) {

    ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>> ();
    for (int i = 0; i < valuesLHS.size(); i++) {
      res.add(getStatesValue( (String) valuesLHS.get(i),
                             (String) valuesRHS.get(i)));
    }

    ArrayList<String> result = new ArrayList<String> ();

    for (int m = 0; m < res.size(); m++) {
      for (int x = 0; x < res.get(m).size(); x++) {
        String a = res.get(m).get(x);
        if (!result.contains(a)) {
          result.add(a);
        }
      }
    }

    boolean b = false;
    int k;

    ArrayList<String> states = new ArrayList<String> ();

    for (k = 0; k < result.size(); k++) {
      b = false;
      for (int l = 0; l < res.size(); l++) {
        if (res.get(l).contains(result.get(k))) {
          b = true;
        }
        else {
          b = false;
          break;
        }
      }
      if (b) {
        states.add(result.get(k));
      }
    }
    return states;

  }

  /**
   * Returns all the states where all variables in ValuesLHS changed.
   *
   * @param valuesLHS ArrayList
   * @param valuesRHS ArrayList
   * @return ArrayList
   */
  public ArrayList<String> getAllChangedStatesValue(ArrayList<String> valuesLHS,
      ArrayList<String> valuesRHS) {
    ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>> ();

    for (int i = 0; i < valuesLHS.size(); i++) {
      res.add(getChangeStatesValue( (String) valuesLHS.get(i),
                                   (String) valuesRHS.get(i)));
    }

    ArrayList<String> result = new ArrayList<String> ();

    for (int m = 0; m < res.size(); m++) {
      for (int x = 0; x < res.get(m).size(); x++) {
        String a = res.get(m).get(x);
        if (!result.contains(a)) {
          result.add(a);
        }
      }
    }

    boolean b = false;
    int k;

    ArrayList<String> states = new ArrayList<String> ();

    for (k = 0; k < result.size(); k++) {
      b = false;
      for (int l = 0; l < res.size(); l++) {
        if (res.get(l).contains(result.get(k))) {
          b = true;
        }
        else {
          b = false;
          break;
        }
      }
      if (b) {
        states.add(result.get(k));
      }
    }
    return states;

  }

  /**
   * String representation of BasicData.
   *
   * @return String
   */
  public String toString() {
    return data.toString();
  }

}
