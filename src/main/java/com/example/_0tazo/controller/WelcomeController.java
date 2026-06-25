package com.example._0tazo.controller;

import com.example._0tazo.view.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

/**
 * Controller for the welcome screen ({@code welcome-view.fxml}).
 *
 * <p>Handles navigation to the selection screen and toggling
 * the How to Play overlay.</p>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @author Cristian Camilo Criollo Diaz
 * @version 1.0
 * @see     SceneManager
 */
public class WelcomeController {

    @FXML private VBox howToPlayOverlay;

    // ── Navigation ────────────────────────────────────────────────────────────

    /**
     * Navigates to the machine selection screen.
     * Called when the player clicks the "Start" button.
     */
    @FXML
    private void onStart() {
        SceneManager.getInstance().goToSelection();
    }

    // ── How to Play overlay ───────────────────────────────────────────────────

    /**
     * Shows the How to Play overlay.
     * Called when the player clicks the "How to Play" button.
     */
    @FXML
    private void onHowToPlay() {
        howToPlayOverlay.setVisible(true);
    }

    /**
     * Hides the How to Play overlay.
     * Called when the player clicks the close button inside the overlay.
     */
    @FXML
    private void onCloseHowToPlay() {
        howToPlayOverlay.setVisible(false);
    }
}