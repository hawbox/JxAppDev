package com.main;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEvent;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventListener;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventType;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.internal.Environment;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The example demonstrates how to integrate UI built with Resources+CSS+JavaScript
 * into Java desktop application.
 */
public class IvyEditorMain extends Application {

    private Browser browser;
    private String user_id = null;
    private Map<String, String> pageNav = new HashMap<String, String>();

    @Override
    public void init() throws Exception {
        // On Mac OS X Chromium engine must be initialized in non-UI thread.
        if (Environment.isMac()) {
            BrowserCore.initialize();
        }

    }

    @Override
    public void start(Stage primaryStage) {

        browser = new Browser();
        browser.setUserAgent("WebView");
        BrowserView browserView = new BrowserView(browser);


        StackPane pane = new StackPane();
        pane.getChildren().add(browserView);
        Scene scene = new Scene(pane, 700, 600);
        primaryStage.setTitle("JxBrowser: JavaFX - IVY Editor");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();

        // Here we load the file but we have to be very careful with the path
        browser.loadURL("file:///Users/ricardo/IdeaProjects/IviEditor/src/Resources/HTML/Editor.html");

        pageNavigation();

        // We can only call java methods from the interface after adding them to the Browser done here below.
        // We need also to be careful since it needs to be done inside onFinishLoadingFrame
        browser.addLoadListener(new LoadAdapter() {
            @Override
            public void onFinishLoadingFrame(FinishLoadingEvent event) {
                System.out.println("HEre");
                if (event.isMainFrame()) {
                    Browser browser = event.getBrowser();
                    JSValue value = browser.executeJavaScriptAndReturnValue("window");
                    value.asObject().setProperty("java", new JavaObject(browser));
                    event.getBrowser().executeJavaScript("setUId();");

                    setListennersToElements();
                }
            }
        });

    }

    public void setListennersToElements(){
        DOMDocument document = browser.getDocument();
        List<DOMElement> buttons = document.findElements(By.tagName("button"));
        List<DOMElement> anchors = document.findElements(By.tagName("a"));
        List<DOMElement> inputs = document.findElements(By.tagName("input"));

        addEvent(buttons);

        addEvent(anchors);

        addEvent(inputs);
    }

    private void addEvent(List<DOMElement> elements) {
        for(DOMElement ele : elements){
            ele.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                public void handleEvent(DOMEvent event) {
                    System.out.println(ele.getAttribute("name"));
                    if(pageNav.containsKey(ele.getAttribute("name"))){
                        move(pageNav.get(ele.getAttribute("name")));
                    }
                }
            }, false);
        }
    }


    public void move(String name){
        System.out.println("The name is = " + name );
        browser.loadURL("file:///Users/ricardo/IdeaProjects/IviEditor/src/Resources/HTML/" + name + ".html");
    }

    public void pageNavigation(){
        System.out.println("HERE");
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get("/Users/ricardo/IdeaProjects/IviEditor/src/Resources/navigation/test.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(new String(encoded));

        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject)jsonParser.parse(new String(encoded));

        JsonArray ja = jo.get("page-navigation").getAsJsonArray();
        for (int i = 0; i < ja.size(); i++) {
            JsonObject ele = ja.get(i).getAsJsonObject();
            JsonObject rules = ele.get("navigation-rules").getAsJsonObject();
            for(Map.Entry<String, JsonElement> entry : rules.entrySet()) {
                System.out.println("Key = " + entry.getKey() + " Value = " + entry.getValue().getAsString());
                pageNav.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }



    public static class JavaObject {

        private Browser browser;

        JavaObject(Browser browser){
            this.browser = browser;
        }

        public String print(String message) {
            return message;
        }

        public JSONString callMethod(String json) {
            System.out.println(json);
            String result = "";
            try {
                result = sendPost(json, "request");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new JSONString(result);

        }

        public void move(String name){
            System.out.println("The name is = " + name );
            browser.loadURL("file:///Users/ricardo/IdeaProjects/IviEditor/src/Resources/HTML/" + name + ".html");
        }

        public JSONString pageNavigation(){
            System.out.println("HERE");
            byte[] encoded = new byte[0];
            try {
                encoded = Files.readAllBytes(Paths.get("/Users/ricardo/IdeaProjects/IviEditor/src/Resources/navigation/test.json"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(new String(encoded));

            return new JSONString(new String(encoded));
        }
    }



    // HTTP POST request
    private static String sendPost(String content, String urlPart) throws Exception {

        String url = "http://localhost:8081/" + urlPart;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        String urlParameters = content;

        System.out.println("Before Sending POST");
        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        System.out.println("AFTER Sending POST");

        int responseCode = con.getResponseCode();
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println("Response : " + response.toString());

        return response.toString();
    }

}