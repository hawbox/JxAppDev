package com.jersey.app.ivyWorkbench;

import java.util.Scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.Compiler;

public class ModelCompiler {

    public static String compile_model(String received_json){
        String response = "";
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(received_json).getAsJsonObject();
        String string_model = jsonObject.get("text_model").getAsString();
        Scanner model = new Scanner(string_model);
        try{
            Compiler comp = (Compiler) new Compiler().compileModel(model);
            response = comp.model.toString();
        }catch(Exception e){
            e.printStackTrace();
            response = e.toString().replaceAll("\"", "'");
        }

        return "{\"result\": \""+response+"\"}";
    }
}

