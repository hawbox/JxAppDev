package com.jersey.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jersey.socket.RefreshService;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;


@Path("/request")
public class ReceiveRequest {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String getRequest (final String input) throws IOException {

        String response = "";

        JsonParser parser = new JsonParser();
        JsonArray jarray = parser.parse(input).getAsJsonArray();
        JsonObject firstObject = jarray.get(0).getAsJsonObject();
        JsonObject secondObject = jarray.get(1).getAsJsonObject();
        JsonObject thirdObject = jarray.get(2).getAsJsonObject();

        System.out.println("Received JSON = " + input);

        String className = firstObject.get("className").getAsString();
        String methodName = firstObject.get("methodName").getAsString();

        Class clasz = null;
        Constructor constructor = null;
        Object instance = null;

        Set<Class<?>> classes = null;

        Reflections reflections;

        InputStream i = this.getClass().getResourceAsStream("/paths.txt");
        BufferedReader r = new BufferedReader(new InputStreamReader(i));

        // reads each line
        String line;
        boolean fst = true;
        while((line = r.readLine()) != null) {
            reflections = new Reflections(line, new SubTypesScanner(false));
            if(fst){
                classes = reflections.getSubTypesOf(Object.class);
                fst = false;
            }else{
                classes.addAll(reflections.getSubTypesOf(Object.class));

            }
        }
        i.close();

        String fullPath = null;

        assert classes != null;
        for(Class c: classes){
            if(className.equals(c.getSimpleName())){
                fullPath = c.getName();
            }
        }

        try {
            clasz = Class.forName(fullPath);
            constructor = clasz.getConstructor();
            instance = constructor.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            response = "{\"Result\": \"Error\", \"Type\": \"Wrong Class Name\"}";
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        if(response.length() == 0){
            assert clasz != null;
            Method[] methods = clasz.getMethods();

            for(Method method : methods){
                if(methodName.equals(method.getName())){
                    try {
                        Method methodz = clasz.getDeclaredMethod(method.getName(), String.class);
                        response = (String) methodz.invoke(instance, secondObject.toString());

                        if(thirdObject.size() > 0){
                            String groupName = thirdObject.get("notificationGroup").getAsString();
                            String notificationName = thirdObject.get("notificationName").getAsString();
                            RefreshService.sendGroupNotification(notificationName, groupName);
                        }
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

            if(response.length() == 0){
                response = "{\"Result\": \"Error\", \"Type\": \"Wrong Method Name\"}";
            }
        }

        //System.out.println("RESPONSE = " + response);

        return response;
    }
}

