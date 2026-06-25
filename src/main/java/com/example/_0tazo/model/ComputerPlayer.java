package com.example._0tazo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an AI-controlled player in the Cincuentazo card game.
 *
 * <p>Card selection is performed automatically by {@link #chooseCard(int)},
 * which applies a greedy strategy: among all legally playable cards, it picks
 * the one whose value brings the table sum closest to 50 without exceeding it,
 * maximizing pressure on the next player.</p>
 *
 * <p>Cards drawn by computer players are always dealt face down, as required
 * by the game rules.</p>
 *
 * <p>The actual turn delay (2–4 s for playing, 1–2 s for drawing) is handled
 * by a {@code Thread} or {@code Timeline} in the controller — not here, to
 * keep the model free of JavaFX dependencies.</p>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @author Cristian Camilo Criollo Diaz
 * @version 1.0
 * @see     AbstractPlayer
 * @see     HumanPlayer
 */
public class ComputerPlayer extends AbstractPlayer {

    /**
     * Constructs a computer player with the given display name.
     *
     * @param name the player's display name (e.g. {@code "Machine 1"})
     */
    public ComputerPlayer(String name) {
        super(name);
    }

    /**
     * Selects the index of the best legal card to play given the current table sum.
     *
     * <p>Strategy: among all cards whose value would not cause the sum to exceed 50,
     * choose the one that results in the highest new sum, putting maximum pressure
     * on the next player. Ties are broken by the lowest hand index.</p>
     *
     * @param currentSum the current table sum
     * @return the hand index (0–3) of the chosen card,
     *         or {@code -1} if no legal move exists
     */
    @Override
    public int chooseCard(int currentSum) {
        int bestIndex  = -1;
        int bestResult = Integer.MIN_VALUE;

        for (int i = 0; i < handSize(); i++) {
            Card card = getCard(i);
            if (card != null && card.isPlayable(currentSum)) {
                int result = currentSum + card.getValue(currentSum);
                if (result > bestResult) {
                    bestResult = result;
                    bestIndex  = i;
                }
            }
        }
        return bestIndex;
    }

    /**
     * Computer player always draws cards face down.
     *
     * @return {@code false}
     */
    @Override
    public boolean isFaceUpDraw() {
        return false;
    }

    /**
     * Returns a list of all hand indices that are legally playable
     * given the current table sum.
     *
     * <p>Useful for the controller when animating or logging the set
     * of options the computer evaluated before choosing.</p>
     *
     * @param currentSum the current table sum
     * @return a list of playable hand indices; empty if no legal move exists
     */
    public List<Integer> getPlayableIndices(int currentSum) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < handSize(); i++) {
            Card card = getCard(i);
            if (card != null && card.isPlayable(currentSum)) indices.add(i);
        }
        return indices;
    }
}
