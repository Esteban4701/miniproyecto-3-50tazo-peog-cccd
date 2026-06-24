package com.example._0tazo.model.exception;

/**
 * Base checked exception for all domain-specific errors in the Cincuentazo game.
 *
 * <p>All custom exceptions are defined as static nested classes, keeping the
 * hierarchy in a single file. Callers can catch either a specific subclass
 * or any game error at once:</p>
 * <pre>{@code
 * try {
 *     game.humanPlayCard(index);
 * } catch (GameException.SumExceededException e) {
 *     // handle sum > 50
 * } catch (GameException e) {
 *     // handle any other game error
 * }
 * }</pre>
 *
 * <p>Hierarchy:</p>
 * <pre>
 * Exception
 * └── GameException
 *     ├── SumExceededException
 *     ├── EmptyDeckException
 *     ├── InvalidTurnException
 *     └── PlayerEliminatedException
 * </pre>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @version 1.0
 */
public class GameException extends Exception {

    /**
     * Constructs a {@code GameException} with the given detail message.
     *
     * @param message a description of the error condition
     */
    public GameException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code GameException} with a detail message and a cause.
     *
     * @param message a description of the error condition
     * @param cause   the underlying exception that triggered this one
     */
    public GameException(String message, Throwable cause) {
        super(message, cause);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Nested subclasses
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Thrown when a player attempts to play a card that would cause the
     * table sum to exceed 50, violating the main rule of Cincuentazo.
     *
     * <p>Example:</p>
     * <pre>{@code
     * if (!card.isPlayable(currentSum)) {
     *     throw new GameException.SumExceededException(currentSum, card.getValue(currentSum));
     * }
     * }</pre>
     */
    public static class SumExceededException extends GameException {

        /** The table sum at the moment the violation was detected. */
        private final int currentSum;

        /** The value the offending card would have added. */
        private final int cardValue;

        /**
         * Constructs a {@code SumExceededException} with context about
         * the card and the current table sum.
         *
         * @param currentSum the table sum before the illegal play
         * @param cardValue  the value the card would have contributed
         */
        public SumExceededException(int currentSum, int cardValue) {
            super(String.format(
                    "Playing this card (value %+d) would bring the sum to %d, exceeding 50.",
                    cardValue, currentSum + cardValue
            ));
            this.currentSum = currentSum;
            this.cardValue  = cardValue;
        }

        /** @return the table sum at the moment the violation was detected */
        public int getCurrentSum() { return currentSum; }

        /** @return the value the offending card would have added */
        public int getCardValue()  { return cardValue; }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Thrown when a card is requested from the deck but both the main deck
     * and the discard pile are empty, making it impossible to deal any card.
     *
     * <p>Under normal game conditions this should never occur, since eliminated
     * players' cards are recycled back into the deck.</p>
     */
    public static class EmptyDeckException extends GameException {

        /**
         * Constructs an {@code EmptyDeckException} with a default message.
         */
        public EmptyDeckException() {
            super("No cards available: both the main deck and discard pile are empty.");
        }

        /**
         * Constructs an {@code EmptyDeckException} with a custom message.
         *
         * @param message a description of the context in which the deck ran out
         */
        public EmptyDeckException(String message) {
            super(message);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Thrown when a player attempts to take an action outside of their turn.
     *
     * <p>Guards against UI events firing at the wrong moment — for example,
     * the human player clicking a card while a computer player's turn is
     * still in progress.</p>
     */
    public static class InvalidTurnException extends GameException {

        /** Name of the player who attempted the out-of-turn action. */
        private final String attemptingPlayer;

        /** Name of the player whose turn it actually is. */
        private final String currentPlayer;

        /**
         * Constructs an {@code InvalidTurnException} identifying who acted
         * out of turn and whose turn it actually is.
         *
         * @param attemptingPlayer the name of the player who acted out of turn
         * @param currentPlayer    the name of the player whose turn it is
         */
        public InvalidTurnException(String attemptingPlayer, String currentPlayer) {
            super(String.format(
                    "It is not %s's turn. Current turn belongs to: %s.",
                    attemptingPlayer, currentPlayer
            ));
            this.attemptingPlayer = attemptingPlayer;
            this.currentPlayer    = currentPlayer;
        }

        /** @return the name of the player who acted out of turn */
        public String getAttemptingPlayer() { return attemptingPlayer; }

        /** @return the name of the player whose turn it actually is */
        public String getCurrentPlayer()    { return currentPlayer; }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Thrown when an eliminated player attempts to perform any game action.
     *
     * <p>Once a player is eliminated they cannot play cards, draw, or interact
     * with the game in any way. This guards against stale UI references
     * targeting inactive players.</p>
     */
    public static class PlayerEliminatedException extends GameException {

        /** Name of the eliminated player who attempted the action. */
        private final String playerName;

        /**
         * Constructs a {@code PlayerEliminatedException} for the given player.
         *
         * @param playerName the name of the eliminated player
         */
        public PlayerEliminatedException(String playerName) {
            super(String.format(
                    "Player '%s' has been eliminated and cannot perform any actions.",
                    playerName
            ));
            this.playerName = playerName;
        }

        /** @return the name of the eliminated player who attempted to act */
        public String getPlayerName() { return playerName; }
    }
}