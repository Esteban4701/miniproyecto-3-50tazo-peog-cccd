package com.example._0tazo;

import com.example._0tazo.view.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("50TAZO");
        primaryStage.setResizable(false);
        SceneManager.getInstance().setStage(primaryStage);
        SceneManager.getInstance().goToWelcome();
    }
}