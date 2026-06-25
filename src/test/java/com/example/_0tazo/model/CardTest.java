package com.example._0tazo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Card}.
 *
 * <p>Covers {@link Card#getValue(int)} for every rank category,
 * Ace value selection logic, and {@link Card#isPlayable(int)}
 * near the 50-point limit.</p>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @version 1.0
 */
@DisplayName("Card")
class CardTest {

    private Card twoOfSpades;
    private Card fiveOfHearts;
    private Card eightOfClubs;
    private Card nineOfDiamonds;
    private Card tenOfSpades;
    private Card jackOfHearts;
    private Card queenOfClubs;
    private Card kingOfDiamonds;
    private Card aceOfSpades;

    @BeforeEach
    void setUp() {
        twoOfSpades    = new Card("2",     "spades",   "blue");
        fiveOfHearts   = new Card("5",     "hearts",   "red");
        eightOfClubs   = new Card("8",     "clubs",    "blue");
        nineOfDiamonds = new Card("9",     "diamonds", "red");
        tenOfSpades    = new Card("10",    "spades",   "blue");
        jackOfHearts   = new Card("jack",  "hearts",   "red");
        queenOfClubs   = new Card("queen", "clubs",    "blue");
        kingOfDiamonds = new Card("king",  "diamonds", "red");
        aceOfSpades    = new Card("ace",   "spades",   "blue");
    }

    // ── getValue() — numeric cards ────────────────────────────────────────────

    @Test
    @DisplayName("2 adds 2 to the sum")
    void testTwoAddsTwo() {
        assertEquals(2, twoOfSpades.getValue(20));
    }

    @Test
    @DisplayName("5 adds 5 to the sum")
    void testFiveAddsFive() {
        assertEquals(5, fiveOfHearts.getValue(10));
    }

    @Test
    @DisplayName("8 adds 8 to the sum")
    void testEightAddsEight() {
        assertEquals(8, eightOfClubs.getValue(30));
    }

    @Test
    @DisplayName("10 adds 10 to the sum")
    void testTenAddsTen() {
        assertEquals(10, tenOfSpades.getValue(15));
    }

    // ── getValue() — 9 ───────────────────────────────────────────────────────

    @Test
    @DisplayName("9 adds 0 — sum stays unchanged")
    void testNineAddsZero() {
        assertEquals(0, nineOfDiamonds.getValue(35));
    }

    @Test
    @DisplayName("9 adds 0 even when sum is 0")
    void testNineAddsZeroAtStart() {
        assertEquals(0, nineOfDiamonds.getValue(0));
    }

    // ── getValue() — face cards ───────────────────────────────────────────────

    @Test
    @DisplayName("Jack subtracts 10")
    void testJackSubtractsTen() {
        assertEquals(-10, jackOfHearts.getValue(30));
    }

    @Test
    @DisplayName("Queen subtracts 10")
    void testQueenSubtractsTen() {
        assertEquals(-10, queenOfClubs.getValue(20));
    }

    @Test
    @DisplayName("King subtracts 10")
    void testKingSubtractsTen() {
        assertEquals(-10, kingOfDiamonds.getValue(50));
    }

    // ── getValue() — Ace ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Ace returns 10 when currentSum + 10 <= 50")
    void testAceReturnsTenWhenSafe() {
        // 30 + 10 = 40 <= 50 → should return 10
        assertEquals(10, aceOfSpades.getValue(30));
    }

    @Test
    @DisplayName("Ace returns 10 when currentSum + 10 == 50 exactly")
    void testAceReturnsTenAtExactLimit() {
        // 40 + 10 = 50 <= 50 → should return 10
        assertEquals(10, aceOfSpades.getValue(40));
    }

    @Test
    @DisplayName("Ace returns 1 when currentSum + 10 > 50")
    void testAceReturnsOneWhenTenWouldExceed() {
        // 41 + 10 = 51 > 50 → should return 1
        assertEquals(1, aceOfSpades.getValue(41));
    }

    @Test
    @DisplayName("Ace returns 1 when sum is already 50")
    void testAceReturnsOneWhenSumIsFifty() {
        assertEquals(1, aceOfSpades.getValue(50));
    }

    // ── getValue() — unknown rank ─────────────────────────────────────────────

    @Test
    @DisplayName("Unknown rank throws IllegalStateException")
    void testUnknownRankThrows() {
        Card invalid = new Card("joker", "spades", "red");
        assertThrows(IllegalStateException.class, () -> invalid.getValue(10));
    }

    // ── isPlayable() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Card is playable when result is exactly 50")
    void testIsPlayableAtExactLimit() {
        // 5 + 45 = 50 → playable
        assertTrue(fiveOfHearts.isPlayable(45));
    }

    @Test
    @DisplayName("Card is not playable when result exceeds 50")
    void testIsNotPlayableWhenExceeds() {
        // 5 + 46 = 51 > 50 → not playable
        assertFalse(fiveOfHearts.isPlayable(46));
    }

    @Test
    @DisplayName("Face card is playable even when sum is 50 (subtracts 10)")
    void testFaceCardPlayableAtFifty() {
        // 50 + (-10) = 40 <= 50 → playable
        assertTrue(jackOfHearts.isPlayable(50));
    }

    @Test
    @DisplayName("9 is always playable regardless of sum")
    void testNineAlwaysPlayable() {
        // 50 + 0 = 50 <= 50 → playable
        assertTrue(nineOfDiamonds.isPlayable(50));
    }

    @Test
    @DisplayName("Card is playable when sum is 0")
    void testIsPlayableAtZeroSum() {
        assertTrue(eightOfClubs.isPlayable(0));
    }

    // ── getCardImage() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Face down card returns back image")
    void testFaceDownReturnsBackImage() {
        twoOfSpades.setFaceUp(false);
        assertEquals("blue_back.png", twoOfSpades.getCardImage());
    }

    @Test
    @DisplayName("Face up card returns front image")
    void testFaceUpReturnsFrontImage() {
        twoOfSpades.setFaceUp(true);
        assertEquals("2_of_spades.png", twoOfSpades.getCardImage());
    }

    @Test
    @DisplayName("Ace face up returns correct image filename")
     void testAceFaceUpImage() {
        aceOfSpades.setFaceUp(true);
        assertEquals("ace_of_spades.png", aceOfSpades.getCardImage());
    }
}
