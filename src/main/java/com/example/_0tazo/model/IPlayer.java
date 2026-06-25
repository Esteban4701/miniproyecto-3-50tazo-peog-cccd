package com.example._0tazo.model;

import com.example._0tazo.model.exception.GameException;
import java.util.ArrayList;

/**
 * Contract that every player in the Cincuentazo game must fulfill,
 * regardless of whether they are human or computer-controlled.
 *
 * <p>This interface defines the behavioral contract for player actions.
 * Shared state and logic live in {@link AbstractPlayer}, which all
 * concrete players extend.</p>
 *
 * <p>Using this interface as the declared type in {@link Game} allows
 * the game logic to treat human and computer players uniformly:</p>
 * <pre>{@code
 * LinkedList<IPlayer> players = new LinkedList<>();
 * players.add(new HumanPlayer("You"));
 * players.add(new ComputerPlayer("Machine 1"));
 * }</pre>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @author Cristian Camilo Criollo Diaz
 * @version 1.0
 * @see     AbstractPlayer
 * @see     HumanPlayer
 * @see     ComputerPlayer
 */
public interface IPlayer {

    /**
     * Chooses the hand index of the card to play given the current table sum.
     *
     * <p>For {@link HumanPlayer}, selection comes from the UI — this method
     * returns {@code -1} and the controller passes the index directly.
     * For {@link ComputerPlayer}, this method applies the AI strategy.</p>
     *
     * @param currentSum the current table sum
     * @return the hand index (0–3) of the chosen card, or {@code -1} if
     *         no legal move exists or selection is handled externally
     */
    int chooseCard(int currentSum);

    /**
     * Plays the card at the given index: removes it from the hand,
     * sends it to the discard pile, and returns it so the caller
     * can update the table sum.
     *
     * @param index the hand index (0–3) of the card to play
     * @param deck  the {@link Deck} whose discard pile receives the card
     * @return the {@link Card} that was played
     * @throws GameException if the index is out of range or the player is eliminated
     */
    Card playCard(int index, Deck deck) throws GameException;

    /**
     * Draws the top card from the main deck and adds it to the hand.
     * Whether the card is face up or face down depends on the concrete implementation.
     *
     * @param deck the {@link Deck} to draw from
     * @throws GameException if both the main deck and discard pile are empty
     */
    void drawCard(Deck deck) throws GameException;

    /**
     * Adds a card to the hand. Used during the initial deal.
     *
     * @param card the {@link Card} to receive
     */
    void receiveCard(Card card);

    /**
     * Eliminates this player: sends all remaining hand cards to the
     * discard pile face down and marks the player as inactive.
     *
     * @param deck the {@link Deck} that receives the eliminated player's cards
     */
    void eliminate(Deck deck);

    /**
     * Returns {@code true} if this player has at least one card that can
     * legally be played without the table sum exceeding 50.
     *
     * @param currentSum the current table sum
     * @return {@code true} if a legal move exists
     */
    boolean hasLegalMove(int currentSum);

    /**
     * Returns the card at the given hand index.
     *
     * @param index the hand index (0–3)
     * @return the {@link Card} at that index, or {@code null} if not found
     */
    Card getCard(int index);

    /**
     * Returns a copy of the hand list.
     *
     * @return a shallow copy of the hand as an {@link ArrayList}
     */
    ArrayList<Card> getHand();

    /**
     * Returns the number of cards currently in the hand.
     *
     * @return the card count, between 0 and {@link AbstractPlayer#HAND_SIZE}
     */
    int handSize();

    /**
     * Returns the player's display name.
     *
     * @return the name of this player
     */
    String getName();

    /**
     * Returns {@code true} if this player is still active in the game.
     *
     * @return {@code true} if active, {@code false} if eliminated
     */
    boolean isActive();

    /**
     * Determines whether cards drawn by this player are dealt face up.
     *
     * @return {@code true} if drawn cards should be face up
     */
    boolean isFaceUpDraw();
}
