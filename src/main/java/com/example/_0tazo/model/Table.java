package com.example._0tazo.model;

import com.example._0tazo.model.exception.GameException;

/**
 * Represents the physical table in the Cincuentazo game.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Tracking the current table sum.</li>
 *   <li>Placing the initial card that starts the game.</li>
 *   <li>Receiving cards played by players and updating the sum.</li>
 *   <li>Exposing the top card of the discard pile for the UI.</li>
 * </ul>
 *
 * <p>{@code Table} holds a reference to the shared {@link Deck} so it can
 * send played cards to the discard pile directly, keeping {@link Game}
 * focused on turn flow and player management.</p>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @author Cristian Camilo Criollo Diaz
 * @version 1.0
 * @see     Game
 * @see     Deck
 */
public class Table {

    /** Maximum allowed table sum before a player is eliminated. */
    public static final int MAX_SUM = 50;

    /** The shared deck — played cards go to its discard pile. */
    private final Deck deck;

    /** The running sum of all cards played on the table. */
    private int tableSum;

    /**
     * Constructs a {@code Table} with a reference to the shared deck.
     * The table sum starts at zero until {@link #placeInitialCard()} is called.
     *
     * @param deck the shared {@link Deck}; must not be {@code null}
     */
    public Table(Deck deck) {
        this.deck     = deck;
        this.tableSum = 0;
    }


    /**
     * Deals one card face up from the deck and places it on the table,
     * initializing the table sum with that card's value.
     *
     * <p>Per the game rules, the initial card determines the starting sum:</p>
     * <ul>
     *   <li>9 → sum starts at 0</li>
     *   <li>A → sum starts at 1 (since currentSum is 0, getValue returns 10;
     *       however, per the rules the initial A counts as 1 — handled here)</li>
     *   <li>J, Q, K → sum starts at −10</li>
     *   <li>2–8, 10 → sum starts at their face value</li>
     * </ul>
     *
     * @throws GameException if the deck has no cards available
     */
    public void placeInitialCard() throws GameException {
        try {
            Card firstCard = deck.dealCard(true);
            // For the initial card, Ace always counts as 1 per game rules
            tableSum = firstCard.getNumber().equalsIgnoreCase("ace")
                    ? 1
                    : firstCard.getValue(0);
            deck.discard(firstCard);
        } catch (IllegalStateException e) {
            throw new GameException.EmptyDeckException();
        }
    }

    /**
     * Places a card on the table and updates the sum.
     *
     * <p>The card's value is added to the current sum. This method does
     * <em>not</em> validate whether the sum would exceed 50 — that check
     * is the caller's responsibility via {@link Card#isPlayable(int)} before
     * calling this method.</p>
     *
     * @param card the {@link Card} to place on the table; must not be {@code null}
     */
    public void placeCard(Card card) {
        tableSum += card.getValue(tableSum);
        card.setFaceUp(true);
        deck.discard(card);
    }

    /**
     * Returns {@code true} if playing the given card would keep the
     * table sum at or below {@value #MAX_SUM}.
     *
     * <p>Delegates to {@link Card#isPlayable(int)} with the current sum.</p>
     *
     * @param card the card to check
     * @return {@code true} if the card is legally playable
     */
    public boolean isCardPlayable(Card card) {
        return card.isPlayable(tableSum);
    }


    /**
     * Returns the current table sum.
     *
     * @return the table sum
     */
    public int getTableSum() { return tableSum; }

    /**
     * Returns the top card of the discard pile — the last card played
     * on the table — without removing it.
     *
     * @return the top {@link Card}, or {@code null} if the discard pile is empty
     */
    public Card getTopCard() { return deck.peekDiscard(); }

    /**
     * Returns {@code true} if the current sum has reached or exceeded
     * the maximum allowed value of {@value #MAX_SUM}.
     *
     * @return {@code true} if the table is at its limit
     */
    public boolean isAtLimit() { return tableSum >= MAX_SUM; }

    /**
     * Adds a value directly to the table sum without going through
     * {@link Card#getValue(int)}.
     *
     * <p>Used exclusively when the human player manually selects the
     * Ace's value (1 or 10) via the overlay, bypassing the automatic
     * value resolution in {@link Card#getValue(int)}.</p>
     *
     * @param value the integer value to add to the table sum;
     *              must be either 1 or 10 when called for an Ace
     */
    public void addToSum(int value) {
        tableSum += value;
    }
}