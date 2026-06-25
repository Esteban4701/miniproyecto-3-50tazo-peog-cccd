package com.example._0tazo;

import com.example._0tazo.view.SceneManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("50TAZO");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/_0tazo/assets/cards.jpg"))));
        primaryStage.setResizable(false);
        SceneManager.getInstance().setStage(primaryStage);
        SceneManager.getInstance().goToWelcome();
    }
}