package com.example._0tazo.model;

/**
 * Represents the human player in the Cincuentazo card game.
 *
 * <p>Card selection is driven entirely by UI click events in the controller.
 * {@link #chooseCard(int)} is a no-op that always returns {@code -1};
 * the controller passes the chosen index directly to
 * {@link AbstractPlayer#playCard(int, Deck)} when the player clicks a card.</p>
 *
 * <p>Cards drawn by the human player are always dealt face up.</p>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @version 2.0
 * @see     AbstractPlayer
 * @see     ComputerPlayer
 */
public class HumanPlayer extends AbstractPlayer {

    /**
     * Constructs the human player with the given display name.
     *
     * @param name the player's display name
     */
    public HumanPlayer(String name) {
        super(name);
    }

    /**
     * Not used for the human player — card selection is handled by the UI
     * controller via mouse click events on the hand cards.
     *
     * @param currentSum the current table sum (unused)
     * @return always {@code -1}
     */
    @Override
    public int chooseCard(int currentSum) {
        return -1;
    }

    /**
     * Human player always draws cards face up.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFaceUpDraw() {
        return true;
    }
}
