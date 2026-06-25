package com.example._0tazo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Deck}.
 *
 * <p>Covers initial deck size, dealing, discarding, recycling behavior
 * (last card preservation), and the empty-deck error condition.</p>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @version 1.0
 */
@DisplayName("Deck")
class DeckTest {

    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = new Deck();
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("New deck contains 104 cards")
    void testInitialSize() {
        assertEquals(104, deck.mainDeckSize());
    }

    @Test
    @DisplayName("Discard pile starts empty")
    void testDiscardPileStartsEmpty() {
        assertTrue(deck.isDiscardPileEmpty());
    }

    @Test
    @DisplayName("peekDiscard returns null when discard pile is empty")
    void testPeekDiscardEmptyReturnsNull() {
        assertNull(deck.peekDiscard());
    }

    // ── dealCard() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("dealCard() reduces main deck size by 1")
    void testDealReducesDeckSize() {
        deck.dealCard();
        assertEquals(103, deck.mainDeckSize());
    }

    @Test
    @DisplayName("dealCard() returns card face up by default")
    void testDealCardFaceUpByDefault() {
        Card card = deck.dealCard();
        assertTrue(card.isFaceUp());
    }

    @Test
    @DisplayName("dealCard(false) returns card face down")
    void testDealCardFaceDown() {
        Card card = deck.dealCard(false);
        assertFalse(card.isFaceUp());
    }

    @Test
    @DisplayName("dealCard(true) returns card face up")
    void testDealCardFaceUp() {
        Card card = deck.dealCard(true);
        assertTrue(card.isFaceUp());
    }

    @Test
    @DisplayName("Dealing all 104 cards empties the main deck")
    void testDealAllCards() {
        for (int i = 0; i < 104; i++) {
            deck.dealCard();
        }
        assertTrue(deck.isMainDeckEmpty());
    }

    // ── discard() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("discard() increases discard pile size by 1")
    void testDiscardIncreasesSize() {
        Card card = deck.dealCard();
        deck.discard(card);
        assertEquals(1, deck.discardPileSize());
    }

    @Test
    @DisplayName("discard() sets card face up")
    void testDiscardSetsFaceUp() {
        Card card = deck.dealCard(false);
        deck.discard(card);
        assertTrue(card.isFaceUp());
    }

    @Test
    @DisplayName("peekDiscard returns last discarded card")
    void testPeekDiscardReturnsTopCard() {
        Card card1 = deck.dealCard();
        Card card2 = deck.dealCard();
        deck.discard(card1);
        deck.discard(card2);
        assertEquals(card2, deck.peekDiscard());
    }

    // ── recycleDiscard() — via dealCard() when main deck is empty ─────────────

    @Test
    @DisplayName("Recycling preserves the last played card on the discard pile")
    void testRecyclePreservesLastCard() {
        // Empty the main deck
        for (int i = 0; i < 104; i++) {
            deck.discard(deck.dealCard());
        }
        assertTrue(deck.isMainDeckEmpty());

        Card lastPlayed = deck.peekDiscard();

        // Trigger recycle by dealing — main deck was empty
        deck.dealCard();

        // Last played card should still be on top of discard pile
        assertEquals(lastPlayed, deck.peekDiscard());
    }

    @Test
    @DisplayName("Recycling refills the main deck from the discard pile")
    void testRecycleRefillsMainDeck() {
        // Deal all cards and discard them
        for (int i = 0; i < 104; i++) {
            deck.discard(deck.dealCard());
        }
        assertTrue(deck.isMainDeckEmpty());

        // Trigger recycle
        deck.dealCard();

        // Main deck should have 103 cards (104 - 1 last played - 1 dealt)
        assertEquals(102, deck.mainDeckSize());
    }

    @Test
    @DisplayName("Recycled cards are face down in the main deck")
    void testRecycledCardsAreFaceDown() {
        for (int i = 0; i < 104; i++) {
            deck.discard(deck.dealCard());
        }

        // Trigger recycle and deal several cards
        for (int i = 0; i < 10; i++) {
            Card card = deck.dealCard(false);
            assertFalse(card.isFaceUp());
        }
    }

    // ── Empty deck error ──────────────────────────────────────────────────────

    @Test
    @DisplayName("dealCard() throws IllegalStateException when both piles are empty")
    void testDealFromEmptyDeckThrows() {
        // Empty main deck without discarding
        for (int i = 0; i < 104; i++) {
            deck.dealCard();
        }
        assertTrue(deck.isMainDeckEmpty());
        assertTrue(deck.isDiscardPileEmpty());

        assertThrows(IllegalStateException.class, () -> deck.dealCard());
    }

    // ── Size consistency ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Total cards across both piles equals 104 after several operations")
    void testTotalCardCountConsistency() {
        // Deal 20 cards and discard them
        for (int i = 0; i < 20; i++) {
            deck.discard(deck.dealCard());
        }
        assertEquals(104, deck.mainDeckSize() + deck.discardPileSize());
    }
}
