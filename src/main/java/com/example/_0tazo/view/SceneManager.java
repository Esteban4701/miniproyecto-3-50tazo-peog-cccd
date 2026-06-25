package com.example._0tazo.view;

import com.example._0tazo.controller.GameController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Singleton that manages all scene transitions in the Cincuentazo application.
 *
 * <p>Centralizes navigation so that individual controllers never need to
 * know the paths of other FXML files or hold a reference to the
 * {@link Stage}. Any controller can trigger a transition with a single call:</p>
 * <pre>{@code
 * SceneManager.getInstance().goToSelection();
 * SceneManager.getInstance().goToGame(2);
 * }</pre>
 *
 * <p>The {@link Stage} must be registered once at application startup
 * via {@link #setStage(Stage)} before any transition is attempted.</p>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @version 1.0
 */
public class SceneManager {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static SceneManager instance;

    /**
     * Returns the single instance of {@code SceneManager}.
     *
     * @return the singleton instance
     */
    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    private SceneManager() {}

    // ── State ─────────────────────────────────────────────────────────────────

    /** The primary stage of the application. */
    private Stage stage;

    /** Base resource path for FXML files. */
    private static final String FXML_PATH = "/com/example/_0tazo/fxml/";

    // ── Setup ─────────────────────────────────────────────────────────────────

    /**
     * Registers the primary stage. Must be called once from
     * {@link com.example._0tazo.Main#start(Stage)}.
     *
     * @param stage the application's primary {@link Stage}
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // ── Transitions ───────────────────────────────────────────────────────────

    /**
     * Navigates to the welcome screen.
     */
    public void goToWelcome() {
        loadScene("welcome-view.fxml", 0);
    }

    /**
     * Navigates to the machine selection screen.
     */
    public void goToSelection() {
        loadScene("selection-view.fxml", 0);
    }

    /**
     * Navigates to the game screen and initializes it with the given
     * number of machine players.
     *
     * @param machineCount the number of computer players (1–3)
     */
    public void goToGame(int machineCount) {
        loadScene("game-view.fxml", machineCount);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Loads an FXML file, optionally calls {@code initData} on its controller,
     * and sets the resulting scene on the stage.
     *
     * @param fxmlFile     the FXML filename (e.g. {@code "game-view.fxml"})
     * @param machineCount passed to {@link GameController#initData(int)} when > 0
     */
    private void loadScene(String fxmlFile, int machineCount) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(FXML_PATH + fxmlFile)
            );
            Parent root = loader.load();

            // Pass machine count to GameController if navigating to the game
            if (machineCount > 0) {
                GameController controller = loader.getController();
                controller.initData(machineCount);
            }

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            stage.show();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene: " + fxmlFile, e);
        }
    }
}