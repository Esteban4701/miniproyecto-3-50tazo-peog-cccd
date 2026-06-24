package com.example._0tazo.controller;

import com.example._0tazo.view.SceneManager;
import javafx.fxml.FXML;

/**
 * Controller for the machine selection screen ({@code selection-view.fxml}).
 *
 * <p>Each button corresponds to the number of machine players the human
 * wants to play against. Clicking one navigates to the game screen and
 * passes the chosen count to {@link GameController#initData(int)}.</p>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @version 1.0
 * @see     SceneManager
 */
public class SelectionController {

    // ── Navigation ────────────────────────────────────────────────────────────

    /**
     * Starts the game against 1 machine player.
     */
    @FXML
    private void onSelectOne() {
        SceneManager.getInstance().goToGame(1);
    }

    /**
     * Starts the game against 2 machine players.
     */
    @FXML
    private void onSelectTwo() {
        SceneManager.getInstance().goToGame(2);
    }

    /**
     * Starts the game against 3 machine players.
     */
    @FXML
    private void onSelectThree() {
        SceneManager.getInstance().goToGame(3);
    }

    /**
     * Navigates back to the welcome screen.
     */
    @FXML
    private void onBack() {
        SceneManager.getInstance().goToWelcome();
    }
}
