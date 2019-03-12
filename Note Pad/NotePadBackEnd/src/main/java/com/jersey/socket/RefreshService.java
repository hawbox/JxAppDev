package com.jersey.socket;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;

public class RefreshService implements Runnable {

    private static RefreshService instance;
    private static Map<String, Session> sMap = new HashMap<String, Session>();
    private static Map<String, ArrayList<String>> grpMap = new HashMap<>();

    @Override
    public void run() {
    }

    public static void add(Session s) {
        sMap.put(s.getId(), s);
    }
    public static void add2(Session s) {
        String token = UUID.randomUUID().toString();
        sMap.put(token, s);
        sendToken(s, token);
        System.out.println("Map Len = " + sMap.size());
    }

    public static void initialize() {
        if (instance == null) {
            instance = new RefreshService();
            new Thread(instance).start();
        }
    }

    public void printMap(){
        System.out.println("This Map is = " + grpMap.size());
    }

    public static RefreshService getInstance(){
        return instance;
    }

    public static void registerToGroup(String token, String groupName){
        System.out.println("Registering to Group");
        if(grpMap.get(groupName) != null){
            ArrayList<String> arrayList = grpMap.get(groupName);
            if(!arrayList.contains(token)){
                arrayList.add(token);
            }
        }else{
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(token);
            grpMap.put(groupName, arrayList);
        }
    }

    public static void unregisterFromGroup(String token, String groupName){
        if(grpMap.get(groupName) != null){
            ArrayList<String> arrayList = grpMap.get(groupName);
            if(arrayList.contains(token)){
                arrayList.remove(token);
                if(arrayList.isEmpty()){
                    grpMap.remove(groupName);
                }
            }
        }
    }

    public static void unregisterFromAllGroups(String token){
        if(!grpMap.isEmpty()){
            Set<String> keySet = grpMap.keySet();
            for (String key: keySet) {
                ArrayList<String> arrayList = grpMap.get(key);
                if(arrayList.contains(token)){
                    System.out.println("The key is = " + key);
                    arrayList.remove(token);
                }
            }
        }
    }

    public static String getTokenFromSession(Session sess){
        if(!sMap.isEmpty()){
            Set<String> keySet = sMap.keySet();
            for (String key: keySet) {
                Session s = sMap.get(key);
                if(s == sess){
                    return key;
                }
            }
        }
        return null;
    }

    public static String getGroupFromToken(String token){
        if(!grpMap.isEmpty()){
            Set<String> keySet = grpMap.keySet();
            for (String key: keySet) {
                ArrayList<String> arrayList = grpMap.get(key);
                if(arrayList.contains(token)){
                    return key;
                }
            }
        }
        return null;
    }

    public static void sendGroupNotification(String message, String groupName){
        try {
            System.out.println("GROUP NAME = " + groupName);
            System.out.println("Map Len = " + grpMap.size());
            ArrayList<String> arrayList = grpMap.get(groupName);
            if(arrayList != null) {
                for (String tk : arrayList) {
                    Session s = sMap.get(tk);
                    if (s.isOpen()) {
                        System.out.println("Sending");
                        String newMessage = "{\"type\":\"newNotification\", \"message\":\"" + message + "\"}";
                        s.getBasicRemote().sendText(newMessage);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeFromAllGroups(String token){
        if(!grpMap.isEmpty()){
            Set<String> keySet = grpMap.keySet();
            for (String key: keySet) {
                ArrayList<String> arrayList = grpMap.get(key);
                if(arrayList.contains(token)){
                    arrayList.remove(token);
                    if(arrayList.isEmpty()){
                        grpMap.remove(key);
                    }
                }
            }
        }
    }

    public static void sendNotification(){
        try {
            for (String key : sMap.keySet()) {

                Session s = sMap.get(key);

                if (s.isOpen()) {
                    s.getBasicRemote().sendText("refresh-table");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendToken(Session s, String token){
        try {
            if(s.isOpen()){
                System.out.println("HERE");
                String newToken = "{\"type\":\"newToken\", \"token\":\"" + token + "\"}";
                s.getBasicRemote().sendText(newToken);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
