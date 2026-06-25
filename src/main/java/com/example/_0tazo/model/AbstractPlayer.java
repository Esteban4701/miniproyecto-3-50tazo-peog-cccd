package com.example._0tazo.model;

import com.example._0tazo.model.exception.GameException;
import java.util.ArrayList;

/**
 * Abstract base implementation of {@link IPlayer} that manages the shared
 * state and logic common to all player types in the Cincuentazo game.
 *
 * <p>The player's hand is stored as an {@link ArrayList} of up to
 * {@value #HAND_SIZE} cards. Concrete subclasses only need to implement:</p>
 * <ul>
 *   <li>{@link #chooseCard(int)} — card selection strategy.</li>
 *   <li>{@link #isFaceUpDraw()} — whether drawn cards are revealed.</li>
 * </ul>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @author Cristian Camilo Criollo Diaz
 * @version 1.0
 * @see     IPlayer
 * @see     HumanPlayer
 * @see     ComputerPlayer
 */
public abstract class AbstractPlayer implements IPlayer {

    /** The fixed number of cards every player holds in their hand. */
    public static final int HAND_SIZE = 4;

    /** The player's display name. */
    private final String name;

    /**
     * The player's hand stored as an {@link ArrayList}.
     * Its size is always between 0 and {@value #HAND_SIZE}.
     */
    private final ArrayList<Card> hand;

    /** Whether this player is still active in the game. */
    private boolean active;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs a player with the given name and an empty hand.
     *
     * @param name the display name of this player; must not be {@code null}
     */
    public AbstractPlayer(String name) {
        this.name   = name;
        this.hand   = new ArrayList<>(HAND_SIZE);
        this.active = true;
    }

    // ── IPlayer — shared implementations ────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public void receiveCard(Card card) {
        if (hand.size() < HAND_SIZE) {
            hand.add(card);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws GameException.PlayerEliminatedException if this player is eliminated
     * @throws GameException                           if the index is out of range
     */
    @Override
    public Card playCard(int index, Deck deck) throws GameException {
        if (!active) {
            throw new GameException.PlayerEliminatedException(name);
        }
        if (index < 0 || index >= hand.size()) {
            throw new GameException("Invalid hand index " + index + " for player " + name);
        }
        Card card = hand.remove(index);
        deck.discard(card);
        return card;
    }

    /**
     * {@inheritDoc}
     *
     * @throws GameException.PlayerEliminatedException if this player is eliminated
     * @throws GameException.EmptyDeckException        if the deck is empty
     */
    @Override
    public void drawCard(Deck deck) throws GameException {
        if (!active) {
            throw new GameException.PlayerEliminatedException(name);
        }
        try {
            receiveCard(deck.dealCard(isFaceUpDraw()));
        } catch (IllegalStateException e) {
            throw new GameException.EmptyDeckException();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void eliminate(Deck deck) {
        for (Card card : hand) {
            card.setFaceUp(false);
            deck.discard(card);
        }
        hand.clear();
        this.active = false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasLegalMove(int currentSum) {
        for (Card card : hand) {
            if (card.isPlayable(currentSum)) return true;
        }
        return false;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Card getCard(int index) {
        if (index < 0 || index >= hand.size()) return null;
        return hand.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<Card> getHand() { return new ArrayList<>(hand); }

    /** {@inheritDoc} */
    @Override
    public int handSize() { return hand.size(); }

    /** {@inheritDoc} */
    @Override
    public String getName() { return name; }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() { return active; }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public String toString() {
        return name + (active ? "" : " [ELIMINATED]");
    }
}
