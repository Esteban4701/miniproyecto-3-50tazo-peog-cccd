package com.example._0tazo.model;

import com.example._0tazo.model.exception.GameException;
import java.util.LinkedList;

/**
 * Orchestrates the Cincuentazo game: manages player order, turn progression,
 * player elimination, and the win condition.
 *
 * <p>The table state (sum and top card) is delegated to {@link Table}.
 * JavaFX dependencies (threads, timelines, UI updates) belong exclusively
 * in the controller — this class is pure Java and fully testable with JUnit.</p>
 *
 * <p>Typical usage from the controller:</p>
 * <pre>{@code
 * Game game = new Game(2);           // human + 2 machines
 * game.setup();                      // deal hands and place first table card
 *
 * // Human turn — index comes from a card click in the UI:
 * game.humanPlayCard(clickedIndex);
 * game.currentPlayerDrawCard();
 * game.nextTurn();
 *
 * // Computer turn — fully automatic:
 * game.computerTakeTurn();
 * game.nextTurn();
 * }</pre>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @author Cristian Camilo Criollo Diaz
 * @version 1.0
 * @see     Table
 * @see     IPlayer
 * @see     Deck
 */
public class Game {

    /**
     * Ordered list of players still in the game.
     * {@link LinkedList} allows O(1) removal of eliminated players.
     */
    private final LinkedList<IPlayer> players;

    /** The shared deck used by all players. */
    private final Deck deck;

    /** The table — owns the sum and the discard pile view. */
    private final Table table;

    /** Index into {@code players} pointing to the current player. */
    private int currentPlayerIndex;

    /** Whether the game has ended. */
    private boolean gameOver;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Constructs a new game with one human player and the specified number
     * of computer players (1, 2, or 3).
     *
     * @param machineCount the number of computer players (1–3)
     * @throws IllegalArgumentException if {@code machineCount} is not between 1 and 3
     */
    public Game(int machineCount) {
        if (machineCount < 1 || machineCount > 3) {
            throw new IllegalArgumentException(
                    "Machine count must be between 1 and 3, got: " + machineCount
            );
        }
        this.deck               = new Deck();
        this.table              = new Table(deck);
        this.players            = new LinkedList<>();
        this.currentPlayerIndex = 0;
        this.gameOver           = false;

        players.add(new HumanPlayer("You"));
        for (int i = 1; i <= machineCount; i++) {
            players.add(new ComputerPlayer("Machine " + i));
        }
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    /**
     * Prepares the game: deals 4 cards to each player and places the
     * initial card on the table to set the starting sum.
     *
     * @throws GameException if the deck runs out of cards during setup
     */
    public void setup() throws GameException {
        for (int i = 0; i < AbstractPlayer.HAND_SIZE; i++) {
            for (IPlayer player : players) {
                player.receiveCard(deck.dealCard(player.isFaceUpDraw()));
            }
        }
        table.placeInitialCard();
    }

    // ── Turn flow ─────────────────────────────────────────────────────────────

    /**
     * Processes the human player's chosen card.
     *
     * @param cardIndex the index of the card in the human player's hand (0–3)
     * @throws GameException.InvalidTurnException      if it is not the human player's turn
     * @throws GameException.PlayerEliminatedException if the human player is eliminated
     * @throws GameException.SumExceededException      if the card would exceed 50
     * @throws GameException                           if the card index is invalid
     */
    public void humanPlayCard(int cardIndex) throws GameException {
        IPlayer current = getCurrentPlayer();

        if (!(current instanceof HumanPlayer)) {
            throw new GameException.InvalidTurnException("You", current.getName());
        }

        Card chosen = current.getCard(cardIndex);
        if (chosen == null || !table.isCardPlayable(chosen)) {
            int value = (chosen != null) ? chosen.getValue(table.getTableSum()) : 0;
            throw new GameException.SumExceededException(table.getTableSum(), value);
        }

        current.playCard(cardIndex, deck);
        table.placeCard(chosen);
    }

    /**
     * Executes a full computer player turn automatically:
     * chooses the best card, plays it, and draws a replacement card.
     * If the computer has no legal move it is eliminated immediately.
     *
     * @throws GameException.InvalidTurnException if it is the human player's turn
     * @throws GameException                      if any deck or player error occurs
     */
    public void computerTakeTurn() throws GameException {
        IPlayer current = getCurrentPlayer();

        if (current instanceof HumanPlayer) {
            throw new GameException.InvalidTurnException(current.getName(), current.getName());
        }

        if (!current.hasLegalMove(table.getTableSum())) {
            eliminateCurrentPlayer();
            return;
        }

        int chosenIndex = current.chooseCard(table.getTableSum());
        Card chosen     = current.getCard(chosenIndex);
        current.playCard(chosenIndex, deck);
        table.placeCard(chosen);

        current.drawCard(deck);
    }

    /**
     * Makes the current player draw a card from the deck.
     * Called by the controller after the human player plays a card.
     *
     * @throws GameException if the deck is empty or the player is eliminated
     */
    public void currentPlayerDrawCard() throws GameException {
        getCurrentPlayer().drawCard(deck);
    }

    /**
     * Checks whether the current player has any legal move and eliminates
     * them automatically if not. The controller should call this at the
     * start of every turn before enabling UI interaction.
     *
     * @throws GameException if an error occurs during elimination
     */
    public void checkCurrentPlayerElimination() throws GameException {
        if (!getCurrentPlayer().hasLegalMove(table.getTableSum())) {
            eliminateCurrentPlayer();
        }
    }

    /**
     * Advances the turn to the next active player.
     * Marks the game as over if only one player remains.
     */
    public void nextTurn() {
        if (players.size() <= 1) {
            gameOver = true;
            return;
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    // ── Elimination ───────────────────────────────────────────────────────────

    /**
     * Eliminates the current player: sends their cards to the deck,
     * removes them from the list, and adjusts the turn index.
     *
     * @throws GameException if the player is already eliminated
     */
    public void eliminateCurrentPlayer() throws GameException {
        IPlayer eliminated = getCurrentPlayer();
        eliminated.eliminate(deck);
        players.remove(currentPlayerIndex);

        if (players.size() == 1) {
            gameOver = true;
            return;
        }
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /** @return the player whose turn it currently is */
    public IPlayer getCurrentPlayer() {
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
        return players.get(currentPlayerIndex);
    }

    /** @return the last remaining player if the game is over, otherwise {@code null} */
    public IPlayer getWinner() {
        return (gameOver && players.size() == 1) ? players.getFirst() : null;
    }

    /** @return {@code true} if the game has ended */
    public boolean isGameOver() { return gameOver; }

    /** @return the current table sum */
    public int getTableSum() { return table.getTableSum(); }

    /** @return the top card on the table, or {@code null} if none */
    public Card getTableCard() { return table.getTopCard(); }

    /** @return the number of cards remaining in the main deck */
    public int getDeckSize() { return deck.mainDeckSize(); }

    /** @return the {@link Table} instance */
    public Table getTable() { return table; }

    /** @return the shared {@link Deck} */
    public Deck getDeck() { return deck; }

    /** @return a snapshot of active players in turn order */
    public LinkedList<IPlayer> getPlayers() { return new LinkedList<>(players); }
}