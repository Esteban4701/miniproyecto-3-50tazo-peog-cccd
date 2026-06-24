package com.example._0tazo.model;

import java.util.Stack;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Represents a double standard deck of 104 playing cards, composed of
 * two complete 52-card sets — one with red backs and one with blue backs.
 *
 * <p>The deck is split into two piles:</p>
 * <ul>
 *   <li><b>Main deck:</b> the draw pile players take cards from, initially face down.</li>
 *   <li><b>Discard pile:</b> where players send used cards, always face up.</li>
 * </ul>
 *
 * <p>When the main deck runs out of cards, the discard pile is automatically
 * recycled: all cards except the last played are flipped face down, transferred
 * to the main deck, and shuffled before play resumes. The last played card
 * remains on top of the discard pile, as required by the game rules.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * Deck deck = new Deck();
 * Card card = deck.dealCard();        // take a card face up (human player)
 * Card hidden = deck.dealCard(false); // take a card face down (machine player)
 * deck.discard(card);                 // send it to the discard pile
 * Card top = deck.peekDiscard();      // see the top of the discard pile
 * }</pre>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @version 1.0
 * @see     Card
 */
public class Deck {

    /**
     * The four standard suits of a playing card deck.
     */
    private static final String[] SUITS = {"spades", "hearts", "diamonds", "clubs"};

    /**
     * The thirteen standard card values using the long-form naming convention
     * that matches the card image filenames (e.g. {@code ace_of_spades.png}).
     */
    private static final String[] NUMBERS = {
            "ace","2","3","4","5","6","7","8","9","10","jack","queen","king"
    };

    /**
     * The main draw pile. Players take cards from this stack.
     * Cards are stored face down until dealt.
     */
    private final Stack<Card> mainDeck;

    /**
     * The discard pile. Cards played by players are pushed here face up.
     * When the main deck is exhausted, all cards except the top one are
     * recycled back into the main deck.
     */
    private final Stack<Card> discardPile;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs a fully built and shuffled 104-card double-deck.
     *
     * <p>Internally calls {@link #build()} to populate the main deck
     * with all 104 cards, then {@link #shuffle(Stack)} to randomize
     * their order. The discard pile starts empty.</p>
     */
    public Deck() {
        mainDeck    = new Stack<>();
        discardPile = new Stack<>();
        build();
        shuffle(mainDeck);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Populates the main deck with 104 cards by iterating over all
     * combinations of suits and numbers, pushing two copies of each card:
     * one with a blue back and one with a red back.
     *
     * <p>Card count breakdown:</p>
     * <pre>
     *   4 suits × 13 numbers × 2 back colors = 104 cards
     * </pre>
     */
    private void build() {
        for (String suit : SUITS) {
            for (String number : NUMBERS) {
                mainDeck.push(new Card(number, suit, "blue"));
                mainDeck.push(new Card(number, suit, "red"));
            }
        }
    }

    /**
     * Shuffles the given stack in place.
     *
     * <p>Since {@link Collections#shuffle} does not operate directly on
     * {@link Stack}, the stack is temporarily converted to an
     * {@link ArrayList}, shuffled, cleared, and repopulated.</p>
     *
     * @param stack the {@link Stack} of {@link Card} objects to shuffle;
     *              must not be {@code null}
     */
    private void shuffle(Stack<Card> stack) {
        ArrayList<Card> temp = new ArrayList<>(stack);
        Collections.shuffle(temp);
        stack.clear();
        stack.addAll(temp);
    }

    /**
     * Recycles the discard pile into the main deck.
     *
     * <p>The top card of the discard pile (the last card played) is preserved
     * and returned to the discard pile after recycling, as required by the
     * game rules. All other cards are flipped face down, transferred to the
     * main deck, and shuffled.</p>
     *
     * <p>This method does nothing if the discard pile has one card or fewer,
     * since there would be nothing to recycle beyond the protected last card.</p>
     *
     * @see #dealCard(boolean)
     * @see #shuffle(Stack)
     */
    private void recycleDiscard() {
        if (discardPile.size() <= 1) return;

        Card lastPlayed = discardPile.pop(); // preserve the last played card

        while (!discardPile.isEmpty()) {
            Card card = discardPile.pop();
            card.setFaceUp(false);
            mainDeck.push(card);
        }
        shuffle(mainDeck);

        discardPile.push(lastPlayed); // restore it on top of the discard pile
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Deals the top card from the main deck with the specified face direction.
     *
     * <p>If the main deck is empty, {@link #recycleDiscard()} is called
     * automatically to refill it from the discard pile before dealing.</p>
     *
     * @param faceUp {@code true} to deal the card face up (human player);
     *               {@code false} to deal it face down (machine player)
     * @return the top {@link Card} from the main deck
     * @throws IllegalStateException if both the main deck and the
     *                               discard pile are empty
     * @see #recycleDiscard()
     */
    public Card dealCard(boolean faceUp) {
        if (mainDeck.isEmpty()) {
            if (discardPile.isEmpty()) {
                throw new IllegalStateException("No cards available in either deck.");
            }
            recycleDiscard();
        }
        Card card = mainDeck.pop();
        card.setFaceUp(faceUp);
        return card;
    }

    /**
     * Deals the top card from the main deck face up.
     *
     * <p>Convenience overload of {@link #dealCard(boolean)} for the human player.
     * Equivalent to calling {@code dealCard(true)}.</p>
     *
     * @return the top {@link Card} from the main deck, face up
     * @throws IllegalStateException if both the main deck and the
     *                               discard pile are empty
     */
    public Card dealCard() {
        return dealCard(true);
    }

    /**
     * Places a card onto the top of the discard pile, face up.
     *
     * <p>This method should be called whenever a player plays a card
     * during their turn. The card's {@code faceUp} state is set to
     * {@code true} regardless of its previous state.</p>
     *
     * @param card the {@link Card} to discard; must not be {@code null}
     */
    public void discard(Card card) {
        card.setFaceUp(true);
        discardPile.push(card);
    }

    /**
     * Returns the top card of the discard pile without removing it.
     *
     * <p>Useful for displaying the current top of the discard pile
     * in the UI without affecting the game state.</p>
     *
     * @return the top {@link Card} of the discard pile,
     *         or {@code null} if the discard pile is empty
     */
    public Card peekDiscard() {
        if (discardPile.isEmpty()) return null;
        return discardPile.peek();
    }

    /**
     * Returns the number of cards remaining in the main deck.
     *
     * @return the current size of the main deck
     */
    public int mainDeckSize() { return mainDeck.size(); }

    /**
     * Returns the number of cards currently in the discard pile.
     *
     * @return the current size of the discard pile
     */
    public int discardPileSize() { return discardPile.size(); }

    /**
     * Returns {@code true} if the main deck contains no cards.
     *
     * @return {@code true} if the main deck is empty, {@code false} otherwise
     */
    public boolean isMainDeckEmpty() { return mainDeck.isEmpty(); }

    /**
     * Returns {@code true} if the discard pile contains no cards.
     *
     * @return {@code true} if the discard pile is empty, {@code false} otherwise
     */
    public boolean isDiscardPileEmpty() { return discardPile.isEmpty(); }
}