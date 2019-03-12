/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jersey.app.ivyWorkbench;


//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
import com.jersey.app.ivyWorkbench.AlertController;

/**
 *
 * @author Pedro
 */
public class Logger {

    //private static FXMLLoader fxmlLoader;
    private final static boolean debug = true;

    public static void alert(Exception e, String title, String description) {
        if (debug) {
            e.printStackTrace();
            //imprimir para ficheiro?
        }
        //abrir o ALert

        start(e, title, description);

    }

    public static void start(Exception exc, String title, String description) {
        try {
            /*fxmlLoader = new FXMLLoader(Logger.class.getResource("/testfx/Alert.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            AlertController controller = (AlertController) fxmlLoader.getController();
            controller.setData(exc, title, description);
            stage.show();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
