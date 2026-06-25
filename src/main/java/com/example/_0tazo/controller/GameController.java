package com.example._0tazo.controller;

import com.example._0tazo.model.*;
import com.example._0tazo.model.exception.GameException;
import com.example._0tazo.utilities.GameTimer;
import com.example._0tazo.utilities.ITimerListener;

import com.example._0tazo.view.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Controller for the main game view (GameView.fxml).
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Receiving the machine count from the selection screen via {@link #initData(int)}.</li>
 *   <li>Setting up the UI layout according to the number of players.</li>
 *   <li>Rendering each player's hand and the table state.</li>
 *   <li>Handling human player card clicks and deck clicks.</li>
 *   <li>Delegating computer turns to {@link Game} triggered by {@link GameTimer} callbacks.</li>
 *   <li>Showing elimination and game-over messages via the overlay.</li>
 * </ul>
 *
 * <p>This controller implements {@link ITimerListener} so it receives
 * timer callbacks directly. All UI updates inside callbacks are wrapped
 * in {@code Platform.runLater()} since they arrive from background threads.</p>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @author Cristian Camilo Criollo Diaz
 * @version 1.0
 * @see     Game
 * @see     GameTimer
 */
public class GameController implements ITimerListener {

    // ── FXML injected nodes ───────────────────────────────────────────────────

    @FXML private Label    timerLabel;
    @FXML private Label    sumLabel;
    @FXML private Label    turnIndicator;
    @FXML private Label    deckCountLabel;
    @FXML private Label    messageLabel;

    @FXML private StackPane tablePane;
    @FXML private StackPane deckPane;

    @FXML private VBox  machine1Zone;
    @FXML private VBox  machine2Zone;
    @FXML private VBox  machine3Zone;
    @FXML private VBox humanZone;
    @FXML private HBox machine1HandBox;
    @FXML private VBox machine2HandBox;
    @FXML private VBox machine3HandBox;
    @FXML private Label machine1Label;
    @FXML private Label machine2Label;
    @FXML private Label machine3Label;

    @FXML private HBox  humanHandBox;
    @FXML private Label humanLabel;

    @FXML private VBox aceOverlay;
    @FXML private Button aceButton1;
    @FXML private Button aceButton10;

    @FXML private VBox  messageOverlay;
    @FXML private Button messageButton;

    // ── Model ─────────────────────────────────────────────────────────────────

    private Game      game;
    private GameTimer timer;
    private int machineCount;
    private int pendingAceIndex = -1;
    private boolean isFirstTurn = true;
    // ── Internal state ────────────────────────────────────────────────────────

    private int sessionId = 0;
    /** Tracks whether the human has already played a card this turn. */
    private boolean humanHasPlayed;

    /** Card image base path inside resources. */
    private static final String IMG_PATH = "/com/example/_0tazo/assets/cards/";

    // ── Initialization ────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        initData(1);
    }
    /**
     * Receives the machine count from the selection screen, creates the
     * {@link Game} and {@link GameTimer}, sets up the layout, deals the
     * initial hands, and starts the general timer.
     *
     * @param machineCount number of computer players (1–3)
     */
    public void initData(int machineCount) {
        final int currentSession = ++sessionId;
        this.machineCount = machineCount;
        this.game          = new Game(machineCount);
        this.timer         = new GameTimer(this);
        this.humanHasPlayed = false;
        this.isFirstTurn     = true;
        setupLayout(machineCount);

        try {
            game.setup();
        } catch (GameException e) {
            showMessage("Error setting up game: " + e.getMessage(), false);
            return;
        }

        timer.startGeneralTimer();
        renderAll();
        startTurn();
        Platform.runLater(this::setupKeyboardShortcuts);
    }

    // ── Layout setup ──────────────────────────────────────────────────────────

    /**
     * Shows only the machine zones needed for this game configuration.
     *
     * @param machineCount number of computer players (1–3)
     */
    private void setupLayout(int machineCount) {
        machine1Zone.setVisible(machineCount >= 1);
        machine2Zone.setVisible(machineCount >= 2);
        machine3Zone.setVisible(machineCount == 3);

        // Label each machine zone with the player name from the model
        java.util.LinkedList<IPlayer> players = game.getPlayers();
        int machineIndex = 0;
        Label[] machineLabels = { machine1Label, machine2Label, machine3Label };
        for (IPlayer player : players) {
            if (player instanceof ComputerPlayer && machineIndex < 3) {
                machineLabels[machineIndex].setText(player.getName());
                machineIndex++;
            }
        }
    }

    // ── Turn flow ─────────────────────────────────────────────────────────────

    /**
     * Starts the current player's turn:
     * checks for automatic elimination, updates the turn indicator,
     * and either enables human interaction or triggers the machine timer.
     */
    private void startTurn() {
        if (game.isGameOver()) {
            handleGameOver();
            return;
        }

        if (!isFirstTurn) {
            try {
                game.checkCurrentPlayerElimination();
            } catch (GameException e) {
                renderAll();
            }
        }
        isFirstTurn = false;

        if (game.isGameOver()) {
            handleGameOver();
            return;
        }

        IPlayer current = game.getCurrentPlayer();

        if (!current.isActive()) {
            game.nextTurn();
            startTurn();
            return;
        }

        turnIndicator.setText(
                current instanceof HumanPlayer ? "▶ YOUR TURN" : "▶ " + current.getName()
        );
        updateSumStyle();
        highlightActivePlayer(current);

        if (current instanceof HumanPlayer) {
            if (!current.hasLegalMove(game.getTableSum())) {
                try {
                    game.eliminateCurrentPlayer();
                    renderAll();
                    if (game.isGameOver()) {
                        handleGameOver();
                        return;
                    }
                    game.nextTurn();
                    startTurn();
                } catch (GameException e) {
                    showMessage(e.getMessage(), false);
                }
                return;
            }
            humanHasPlayed = false;
            renderHumanHand(true);
        } else {
            renderHumanHand(false);
            timer.startMachineTurnTimer();
        }
    }

    // ── Human interaction ─────────────────────────────────────────────────────

    /**
     * Handles a click on one of the human player's cards.
     * Plays the card if legal, then renders the updated state.
     *
     * @param cardIndex the index of the clicked card in the hand
     */
    private void onHumanCardClicked(int cardIndex) {
        if (humanHasPlayed) return;

        Card card = game.getCurrentPlayer().getCard(cardIndex);
        if (card == null) return;

        // Si es un As, mostrar el overlay de selección
        if (card.getNumber().equalsIgnoreCase("ace")) {
            pendingAceIndex = cardIndex;
            showAceOverlay();
            return;
        }

        playHumanCard(cardIndex);
    }

    /**
     * Handles a click on the deck — the human draws a replacement card
     * after having played one this turn.
     */
    @FXML
    private void onDeckClicked() {
        if (!humanHasPlayed) return;
        if (!(game.getCurrentPlayer() instanceof HumanPlayer)) return;

        try {
            game.currentPlayerDrawCard();
            humanHasPlayed = false;
            renderAll();
            game.nextTurn();
            startTurn();
        } catch (GameException e) {
            showMessage(e.getMessage(), false);
        }
    }

    /**
     * Called by the message overlay button — restarts the game with
     * the same number of machine players.
     */
    @FXML
    private void onPlayAgain() {
        timer.stopAll();
        SceneManager.getInstance().goToGame(machineCount);
    }
    // ── ITimerListener callbacks ──────────────────────────────────────────────

    /**
     * {@inheritDoc}
     * Updates the elapsed-time label every second on the JavaFX thread.
     */
    @Override
    public void onTick(int elapsedSeconds) {
        final int capturedSession = sessionId;
        Platform.runLater(() -> {
            if (capturedSession != sessionId) return; // callback de sesión vieja
            timerLabel.setText(formatTime(elapsedSeconds));
        });
    }

    /**
     * {@inheritDoc}
     * The machine plays its card when the thinking delay has elapsed.
     */
    @Override
    public void onMachineTurnReady() {
        Platform.runLater(() -> {
            if (game.isGameOver()) return;
            if (game.getCurrentPlayer() instanceof HumanPlayer) return;

            try {
                game.computerTakeTurn();
                if (game.isGameOver()) {
                    handleGameOver();
                    return;
                }
                renderAll();
            } catch (GameException e) {
                showMessage(e.getMessage(), false);
            }
        });
    }

    /**
     * {@inheritDoc}
     * The machine draws a replacement card, then the turn advances.
     */
    @Override
    public void onMachineDrawReady() {
        Platform.runLater(() -> {
            if (game.isGameOver()) return;
            if (game.getCurrentPlayer() instanceof HumanPlayer) return;

            renderAll();
            game.nextTurn();
            startTurn();
        });
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    /**
     * Re-renders all UI components to reflect the current model state.
     */
    private void renderAll() {
        if (game.isGameOver()) return;
        renderTable();
        renderDeck();
        renderMachineHands();
        renderHumanHand(!humanHasPlayed && game.getCurrentPlayer() instanceof HumanPlayer);
    }

    /**
     * Updates the table pane with the current top card image and the sum label.
     */
    private void renderTable() {
        tablePane.getChildren().clear();
        Card topCard = game.getTableCard();
        if (topCard != null) {
            tablePane.getChildren().add(createCardView(topCard, true, -1, false));
        }
        sumLabel.setText(String.valueOf(game.getTableSum()));
        updateSumStyle();
    }

    /**
     * Updates the deck pane and the remaining card count label.
     */
    private void renderDeck() {
        deckPane.getChildren().clear();
        ImageView back = new ImageView(loadImage("blue_back.png"));
        back.setFitWidth(80);
        back.setFitHeight(120);
        deckPane.getChildren().add(back);
        deckCountLabel.setText(String.valueOf(game.getDeckSize()));
    }

    /**
     * Re-renders all machine player hands (cards face down).
     */
    private void renderMachineHands() {
        machine1HandBox.getChildren().clear();
        machine2HandBox.getChildren().clear();
        machine3HandBox.getChildren().clear();

        VBox[] zones = { machine1Zone, machine2Zone, machine3Zone };
        Pane[] boxes = { machine1HandBox, machine2HandBox, machine3HandBox };
        Label[] labels = { machine1Label, machine2Label, machine3Label };

        int machineIndex = 0;
        for (int i = 0; i < 3 && machineIndex < machineCount; i++) {
            String machineName = "Machine " + (machineIndex + 1);
            IPlayer player = game.getPlayers().stream()
                    .filter(p -> p.getName().equals(machineName))
                    .findFirst()
                    .orElse(null);

            if (player != null && player.isActive()) {
                for (Card card : player.getHand()) {
                    boxes[machineIndex].getChildren()
                            .add(createCardView(card, false, -1, false));
                }
                labels[machineIndex].getStyleClass().remove("player-label-eliminated");
            } else {
                labels[machineIndex].getStyleClass().add("player-label-eliminated");
            }
            machineIndex++;
        }
    }

    /**
     * Re-renders the human player's hand.
     *
     * @param clickable whether the cards should respond to mouse clicks
     */
    private void renderHumanHand(boolean clickable) {
        humanHandBox.getChildren().clear();
        IPlayer human = game.getPlayers().stream()
                .filter(p -> p instanceof HumanPlayer)
                .findFirst()
                .orElse(null);

        if (human == null || !human.isActive()) {
            humanLabel.getStyleClass().add("player-label-eliminated");
            return;
        }

        ArrayList<Card> hand = human.getHand();
        for (int i = 0; i < hand.size(); i++) {
            Card card     = hand.get(i);
            boolean legal = game.getTable().isCardPlayable(card);
            ImageView iv  = createCardView(card, true, i, clickable && legal);
            if (clickable) {
                iv.getStyleClass().add(legal ? "card-playable" : "card-not-playable");
            }
            humanHandBox.getChildren().add(iv);
        }
    }

    // ── Card view factory ─────────────────────────────────────────────────────

    /**
     * Creates an {@link ImageView} for the given card.
     *
     * @param card      the card to render
     * @param faceUp    whether to show the front or back image
     * @param index     hand index used for click handler (-1 to skip)
     * @param clickable whether to attach a mouse click handler
     * @return the configured {@link ImageView}
     */
    private ImageView createCardView(Card card, boolean faceUp, int index, boolean clickable) {
        String imageName = faceUp ? card.getCardImage() : card.getBackColor() + "_back.png";
        ImageView iv     = new ImageView(loadImage(imageName));
        iv.setFitWidth(80);
        iv.setFitHeight(120);
        iv.setPreserveRatio(true);
        iv.getStyleClass().add("card");

        if (clickable && index >= 0) {
            final int cardIndex = index;
            iv.setOnMouseClicked(e -> onHumanCardClicked(cardIndex));
        }
        return iv;
    }

    // ── Game over / elimination ───────────────────────────────────────────────

    /**
     * Handles the end of the game by stopping the timers and showing
     * the winner overlay.
     */
    private void handleGameOver() {
        timer.stopAll();
        IPlayer winner = game.getWinner();
        String message = (winner instanceof HumanPlayer)
                ? "You win!"
                : winner.getName() + " wins!";
        showMessage(message, true);
    }

    /**
     * Displays the message overlay with the given text.
     *
     * @param message     the message to display
     * @param showButton  whether to show the "Play again" button
     */
    private void showMessage(String message, boolean showButton) {
        messageLabel.setText(message);
        messageButton.setVisible(showButton);
        messageOverlay.setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Loads an image from the resources images directory.
     *
     * @param filename the image filename (e.g. {@code "ace_of_spades.png"})
     * @return the loaded {@link Image}
     */
    private Image loadImage(String filename) {
        return new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(IMG_PATH + filename)
        ));
    }

    /**
     * Formats elapsed seconds as {@code MM:SS}.
     *
     * @param totalSeconds total seconds elapsed
     * @return formatted time string
     */
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Updates the sum label CSS class based on proximity to 50.
     * Normal → warning (≥40) → danger (≥47).
     */
    private void updateSumStyle() {
        sumLabel.getStyleClass().removeAll("sum-label-warning", "sum-label-danger");
        int sum = game.getTableSum();
        if (sum >= 47) {
            sumLabel.getStyleClass().add("sum-label-danger");
        } else if (sum >= 40) {
            sumLabel.getStyleClass().add("sum-label-warning");
        }
    }

    /**
     * Briefly flashes the sum label to give visual feedback when the
     * human tries to play an illegal card.
     */
    private void flashSumLabel() {
        sumLabel.getStyleClass().add("sum-label-danger");
        new Thread(() -> {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> updateSumStyle());
        }).start();
    }
    /**
     * Displays the Ace value selection overlay, allowing the human player
     * to choose between adding 1 or 10 to the table sum.
     *
     * <p>The +10 button is automatically disabled if adding 10 would
     * cause the table sum to exceed 50.</p>
     */
    private void showAceOverlay() {
        int sum = game.getTableSum();
        aceButton10.setDisable(sum + 10 > 50);
        aceOverlay.setVisible(true);
    }

    /**
     * Handles the human player's choice of Ace value 1.
     * Hides the overlay, plays the Ace with value 1, and resets
     * the pending Ace index.
     */
    @FXML
    private void onAceOne() {
        aceOverlay.setVisible(false);
        playHumanCardWithAceValue(pendingAceIndex, 1);
        pendingAceIndex = -1;
    }

    /**
     * Handles the human player's choice of Ace value 10.
     * Hides the overlay, plays the Ace with value 10, and resets
     * the pending Ace index.
     */
    @FXML
    private void onAceTen() {
        aceOverlay.setVisible(false);
        playHumanCardWithAceValue(pendingAceIndex, 10);
        pendingAceIndex = -1;
    }

    /**
     * Plays the Ace card at the given hand index with the human-chosen value,
     * bypassing the automatic value selection in {@link Card#getValue(int)}.
     *
     * <p>The card is removed from the hand, sent to the discard pile,
     * and the table sum is updated directly via {@link Table#addToSum(int)}
     * with the chosen value.</p>
     *
     * @param cardIndex   the index of the Ace in the human player's hand (0–3)
     * @param chosenValue the value chosen by the human player (1 or 10)
     */
    private void playHumanCardWithAceValue(int cardIndex, int chosenValue) {
        try {
            IPlayer human = game.getCurrentPlayer();
            Card ace = human.getCard(cardIndex);
            human.playCard(cardIndex, game.getDeck());
            game.getTable().addToSum(chosenValue);
            ace.setFaceUp(true);
            game.getDeck().discard(ace);
            humanHasPlayed = true;
            renderAll();
            renderHumanHand(false);
        } catch (GameException e) {
            showMessage(e.getMessage(), false);
        }
    }

    /**
     * Plays a non-Ace card from the human player's hand at the given index.
     *
     * <p>Delegates to {@link Game#humanPlayCard(int)} and updates the UI.
     * Shows a visual flash on the sum label if the card is not legally
     * playable.</p>
     *
     * @param cardIndex the index of the card in the human player's hand (0–3)
     */
    private void playHumanCard(int cardIndex) {
        try {
            game.humanPlayCard(cardIndex);
            humanHasPlayed = true;
            renderAll();
            renderHumanHand(false);
        } catch (GameException.SumExceededException e) {
            flashSumLabel();
        } catch (GameException e) {
            showMessage(e.getMessage(), false);
        }
    }

    /**
     * Highlights the active player's zone with a golden border and full
     * card opacity, while dimming all other players' hands to
     * {@code 0.35} opacity.
     *
     * <p>Called at the start of every turn from {@link #startTurn()}.</p>
     *
     * @param current the {@link IPlayer} whose turn it currently is
     */
    private void highlightActivePlayer(IPlayer current) {
        machine1Zone.getStyleClass().remove("player-zone-active");
        machine2Zone.getStyleClass().remove("player-zone-active");
        machine3Zone.getStyleClass().remove("player-zone-active");
        humanZone.getStyleClass().remove("player-zone-active");

        setHandOpacity(machine1HandBox, 0.35);
        setHandOpacity(machine2HandBox, 0.35);
        setHandOpacity(machine3HandBox, 0.35);
        setHandOpacity(humanHandBox, 0.35);

        java.util.LinkedList<IPlayer> players = game.getPlayers();
        int machineIndex = 0;
        for (IPlayer player : players) {
            if (player == current) {
                if (player instanceof HumanPlayer) {
                    humanZone.getStyleClass().add("player-zone-active");
                    setHandOpacity(humanHandBox, 1.0);
                } else {
                    switch (machineIndex) {
                        case 0 -> { machine1Zone.getStyleClass().add("player-zone-active");
                            setHandOpacity(machine1HandBox, 1.0); }
                        case 1 -> { machine2Zone.getStyleClass().add("player-zone-active");
                            setHandOpacity(machine2HandBox, 1.0); }
                        case 2 -> { machine3Zone.getStyleClass().add("player-zone-active");
                            setHandOpacity(machine3HandBox, 1.0); }
                    }
                }
            }
            if (player instanceof ComputerPlayer) machineIndex++;
        }
    }

    /**
     * Sets the opacity of all card nodes inside the given container.
     *
     * <p>Used by {@link #highlightActivePlayer(IPlayer)} to dim or
     * restore the visibility of a player's hand.</p>
     *
     * @param box     the {@link javafx.scene.layout.Pane} containing the card nodes
     * @param opacity the opacity value between {@code 0.0} (invisible)
     *                and {@code 1.0} (fully visible)
     */
    private void setHandOpacity(javafx.scene.layout.Pane box, double opacity) {
        box.getChildren().forEach(node -> node.setOpacity(opacity));
    }
    /**
     * Navigates back to the welcome screen.
     * Stops all timers before leaving the game screen.
     */
    @FXML
    private void onGoHome() {
        timer.stopAll();
        SceneManager.getInstance().goToWelcome();
    }
    /**
     * Registers keyboard shortcuts for the human player's turn:
     * 1–4 to highlight a card, Enter to play the selected card,
     * T to draw from the deck.
     */
    private void setupKeyboardShortcuts() {
        humanHandBox.getScene().setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DIGIT1 -> selectCard(0);
                case DIGIT2 -> selectCard(1);
                case DIGIT3 -> selectCard(2);
                case DIGIT4 -> selectCard(3);
                case ENTER  -> playSelectedCard();
                case T      -> onDeckClicked();
            }
        });
    }

    /** Index of the card currently highlighted via keyboard. */
    private int selectedCardIndex = -1;

    /**
     * Highlights the card at the given index as if the mouse hovered over it,
     * removing the highlight from any previously selected card.
     *
     * @param index the hand index (0–3) to select
     */
    private void selectCard(int index) {
        if (!humanHasPlayed && game.getCurrentPlayer() instanceof HumanPlayer) {
            // Remove previous selection
            humanHandBox.getChildren().forEach(node ->
                    node.getStyleClass().remove("card-selected")
            );
            // Apply new selection if index is valid
            if (index < humanHandBox.getChildren().size()) {
                humanHandBox.getChildren().get(index)
                        .getStyleClass().add("card-selected");
                selectedCardIndex = index;
            }
        }
    }

    /**
     * Plays the currently keyboard-selected card, equivalent to clicking it.
     */
    private void playSelectedCard() {
        if (selectedCardIndex >= 0) {
            onHumanCardClicked(selectedCardIndex);
            selectedCardIndex = -1;
            humanHandBox.getChildren().forEach(node ->
                    node.getStyleClass().remove("card-selected")
            );
        }
    }
}