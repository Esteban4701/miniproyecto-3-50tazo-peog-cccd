package com.example._0tazo.model;

/**
 * Represents a single playing card with a number, suit, face direction,
 * and back color. Cards belong to a standard 52-card set extended to a
 * 104-card double deck by using two back colors (red and blue).
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @author Cristian Camilo Criollo Diaz
 * @version 1.0
 * @see     Deck
 */
public class Card {

    private String number;
    private String suit;
    private boolean faceUp;
    private String backColor; // "red" or "blue"

    /**
     * Constructs a new card face down with the given number, suit, and back color.
     *
     * @param number    the card's rank (e.g. "ace", "2", "jack", "king")
     * @param suit      the card's suit (e.g. "spades", "hearts")
     * @param backColor the back color of the card ("red" or "blue")
     */
    public Card(String number, String suit, String backColor) {
        this.number = number;
        this.suit = suit;
        this.faceUp = false;
        this.backColor = backColor;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getNumber()    { return number; }
    public String getSuit()      { return suit; }
    public boolean isFaceUp()    { return faceUp; }
    public String getBackColor() { return backColor; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setNumber(String number)       { this.number = number; }
    public void setSuit(String suit)           { this.suit = suit; }
    public void setFaceUp(boolean faceUp)      { this.faceUp = faceUp; }
    public void setBackColor(String backColor) { this.backColor = backColor; }

    // ── Game logic ───────────────────────────────────────────────────────────

    /**
     * Returns the filename of the image that represents this card's current state.
     *
     * <p>If the card is face down, returns the back image based on {@code backColor}.
     * If face up, returns the front image using the naming convention
     * {@code number_of_suit.png} (e.g. {@code ace_of_spades.png}).</p>
     *
     * @return the image filename for this card's current state
     */
    public String getCardImage() {
        if (!faceUp) return backColor + "_back.png";
        return number + "_of_" + suit + ".png";
    }

    /**
     * Returns the integer value this card contributes to the table sum,
     * following the Cincuentazo rules:
     *
     * <ul>
     *   <li>2–8 and 10: add their face value.</li>
     *   <li>9: adds 0 (neither adds nor subtracts).</li>
     *   <li>Jack, Queen, King: subtract 10.</li>
     *   <li>Ace: adds 10 if the result would not exceed 50; otherwise adds 1.</li>
     * </ul>
     *
     * @param currentSum the current table sum, used to determine the Ace's value
     * @return the integer value to add to the table sum (may be negative)
     * @throws IllegalStateException if the card's number is not a recognized rank
     */
    public int getValue(int currentSum) {
        return switch (number.toLowerCase()) {
            case "ace"                                    -> (currentSum + 10 <= 50) ? 10 : 1;
            case "2","3","4","5","6","7","8","10"         -> Integer.parseInt(number);
            case "9"                                      -> 0;
            case "jack", "queen", "king"                  -> -10;
            default -> throw new IllegalStateException("Unknown card number: " + number);
        };
    }

    /**
     * Returns {@code true} if playing this card would keep the table sum
     * at or below 50, given the current sum.
     *
     * <p>This is a convenience method the AI and the controller can use
     * to quickly filter which cards in a hand are legally playable.</p>
     *
     * @param currentSum the current table sum
     * @return {@code true} if playing this card is legal, {@code false} otherwise
     */
    public boolean isPlayable(int currentSum) {
        return currentSum + getValue(currentSum) <= 50;
    }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public String toString() {
        return number + " of " + suit;
    }
}
