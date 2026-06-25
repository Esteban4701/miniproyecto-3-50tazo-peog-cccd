package com.example._0tazo;

import com.example._0tazo.view.SceneManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Main entry point of the 50TAZO application.
 * <p>
 * This class initializes the JavaFX application, configures the
 * primary stage, and displays the welcome screen.
 * </p>
 *
 * @author Paulo Esteban Ordoñez Gutiérrez
 * @author Cristian Camilo Criollo Diaz
 * @version 1.0
 */
public class Main extends Application {

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Configures and displays the primary stage of the application.
     * <p>
     * Sets the window title and icon, disables resizing,
     * and navigates to the welcome screen through the scene manager.
     * </p>
     *
     * @param primaryStage the primary stage provided by JavaFX.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("50TAZO");
        primaryStage.getIcons().add(new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/com/example/_0tazo/assets/cards.jpg")
                )
        ));
        primaryStage.setResizable(false);
        SceneManager.getInstance().setStage(primaryStage);
        SceneManager.getInstance().goToWelcome();
    }
}