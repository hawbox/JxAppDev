package com.jersey.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JSONUtil {
    private String jsonString;
    private String response = null;
    private JsonElement jsonTree;
    private JsonParser parser;

    public JSONUtil(){
        parser = new JsonParser();

    }

    public void setParams(String jsonString){
        this.jsonString = jsonString;
        jsonTree = parser.parse(jsonString);
    }

    public String parse(String key){
        if (jsonTree.isJsonObject()){
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            JsonElement element = jsonObject.get(key);
            response = element.toString().substring(1,element.toString().length()-1);
            System.out.println("Response = " + response);
        }


        return response;
    }

    public JsonArray parseArray(String key) {
        JsonArray array = null;
        if(jsonTree.isJsonObject()){
            JsonObject jsonObject = jsonTree.getAsJsonObject();
            array = jsonObject.getAsJsonArray(key);
        }
        return array;
    }
}
