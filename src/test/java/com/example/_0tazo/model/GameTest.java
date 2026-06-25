package com.example._0tazo.model;

import com.example._0tazo.model.exception.GameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Game}.
 *
 * <p>Covers game setup, turn flow, player elimination,
 * win condition, and exception throwing for illegal moves.</p>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @version 1.0
 */
@DisplayName("Game")
class GameTest {

    private Game game1; // human + 1 machine
    private Game game3; // human + 3 machines

    @BeforeEach
    void setUp() throws GameException {
        game1 = new Game(1);
        game1.setup();

        game3 = new Game(3);
        game3.setup();
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Game with 1 machine has 2 players total")
    void testGameOneHasTwoPlayers() {
        assertEquals(2, game1.getPlayers().size());
    }

    @Test
    @DisplayName("Game with 3 machines has 4 players total")
    void testGameThreeHasFourPlayers() {
        assertEquals(4, game3.getPlayers().size());
    }

    @Test
    @DisplayName("First player is always the human")
    void testFirstPlayerIsHuman() {
        assertTrue(game1.getPlayers().getFirst() instanceof HumanPlayer);
    }

    @Test
    @DisplayName("Remaining players are computer players")
    void testRemainingPlayersAreComputers() {
        LinkedList<IPlayer> players = game1.getPlayers();
        players.removeFirst(); // remove human
        players.forEach(p -> assertTrue(p instanceof ComputerPlayer));
    }

    @Test
    @DisplayName("Invalid machine count throws IllegalArgumentException")
    void testInvalidMachineCountThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Game(0));
        assertThrows(IllegalArgumentException.class, () -> new Game(4));
    }

    // ── setup() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setup() deals 4 cards to every player")
    void testSetupDealsForCardsToEachPlayer() {
        game1.getPlayers().forEach(p ->
            assertEquals(AbstractPlayer.HAND_SIZE, p.handSize())
        );
    }

    @Test
    @DisplayName("setup() places initial card on the table")
    void testSetupPlacesInitialCard() {
        assertNotNull(game1.getTableCard());
    }

    @Test
    @DisplayName("setup() initializes table sum from initial card")
    void testSetupInitializesTableSum() {
        // Sum must be between -10 and 10 after first card
        int sum = game1.getTableSum();
        assertTrue(sum >= -10 && sum <= 10,
            "Initial sum should be between -10 and 10, got: " + sum);
    }

    @Test
    @DisplayName("setup() reduces deck size by dealt cards + 1 initial")
    void testSetupReducesDeckSize() {
        // 2 players × 4 cards + 1 initial = 9 dealt → 104 - 9 = 95
        assertEquals(95, game1.getDeckSize());
    }

    // ── Turn flow ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("First turn belongs to the human player")
    void testFirstTurnIsHuman() {
        assertTrue(game1.getCurrentPlayer() instanceof HumanPlayer);
    }

    @Test
    @DisplayName("nextTurn() advances to the next player")
    void testNextTurnAdvances() {
        IPlayer first = game1.getCurrentPlayer();
        game1.nextTurn();
        assertNotEquals(first, game1.getCurrentPlayer());
    }

    @Test
    @DisplayName("nextTurn() wraps around to first player after last")
    void testNextTurnWrapsAround() throws GameException {
        // With 2 players: 0 → 1 → 0
        IPlayer human = game1.getCurrentPlayer();
        game1.nextTurn(); // → machine
        game1.nextTurn(); // → human again
        assertEquals(human, game1.getCurrentPlayer());
    }

    // ── humanPlayCard() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("humanPlayCard() throws SumExceededException for illegal card")
    void testHumanPlayCardThrowsSumExceeded() {
        // Force table sum to 49 so most cards would exceed 50
        forceTableSum(game1, 49);

        // Find a card that would exceed 50 (any numeric card >= 2)
        IPlayer human = game1.getCurrentPlayer();
        int illegalIndex = findIllegalCardIndex(human, 49);

        if (illegalIndex >= 0) {
            assertThrows(GameException.SumExceededException.class,
                () -> game1.humanPlayCard(illegalIndex));
        }
    }

    @Test
    @DisplayName("humanPlayCard() throws InvalidTurnException when it is machine's turn")
    void testHumanPlayCardThrowsInvalidTurn() throws GameException {
        game1.nextTurn(); // advance to machine's turn
        assertThrows(GameException.InvalidTurnException.class,
            () -> game1.humanPlayCard(0));
    }

    @Test
    @DisplayName("humanPlayCard() reduces human hand size by 1")
    void testHumanPlayCardReducesHandSize() throws GameException {
        IPlayer human = game1.getCurrentPlayer();
        int legalIndex = findLegalCardIndex(human, game1.getTableSum());

        if (legalIndex >= 0) {
            game1.humanPlayCard(legalIndex);
            assertEquals(AbstractPlayer.HAND_SIZE - 1, human.handSize());
        }
    }

    // ── computerTakeTurn() ────────────────────────────────────────────────────

    @Test
    @DisplayName("computerTakeTurn() throws InvalidTurnException on human's turn")
    void testComputerTurnThrowsOnHumanTurn() {
        assertThrows(GameException.InvalidTurnException.class,
            () -> game1.computerTakeTurn());
    }

    @Test
    @DisplayName("computerTakeTurn() keeps machine hand at 4 cards after play and draw")
    void testComputerTurnKeepsHandSize() throws GameException {
        game1.nextTurn(); // advance to machine
        IPlayer machine = game1.getCurrentPlayer();
        game1.computerTakeTurn();
        assertEquals(AbstractPlayer.HAND_SIZE, machine.handSize());
    }

    // ── Elimination ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("checkCurrentPlayerElimination() eliminates player with no legal move")
    void testEliminationWhenNoLegalMove() throws GameException {
        // Force sum to 50 so only 9 or face cards are playable
        forceTableSum(game1, 50);

        // Give the human only cards that cannot be played at sum=50
        // (any card with value > 0, like numeric cards 2-8, 10)
        // By forcing sum to 50, only 9 (value=0) and J/Q/K (value=-10) are playable
        // If the human has none of those, they get eliminated
        IPlayer human = game1.getCurrentPlayer();
        if (!human.hasLegalMove(50)) {
            int playersBefore = game1.getPlayers().size();
            game1.checkCurrentPlayerElimination();
            assertTrue(game1.getPlayers().size() < playersBefore
                || game1.isGameOver());
        }
    }

    @Test
    @DisplayName("eliminateCurrentPlayer() removes player from the list")
    void testEliminateRemovesPlayer() throws GameException {
        int before = game3.getPlayers().size();
        game3.eliminateCurrentPlayer();
        assertEquals(before - 1, game3.getPlayers().size());
    }

    @Test
    @DisplayName("eliminateCurrentPlayer() sends cards to discard pile")
    void testEliminateCardsGoToDeck() throws GameException {
        int discardBefore = game3.getDeck().discardPileSize();
        game3.eliminateCurrentPlayer();
        assertEquals(discardBefore + AbstractPlayer.HAND_SIZE, game3.getDeck().discardPileSize());
    }
    // ── Game over ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Game is not over at the start")
    void testGameNotOverAtStart() {
        assertFalse(game1.isGameOver());
    }

    @Test
    @DisplayName("Game ends when only one player remains")
    void testGameEndsWithOnePlayer() throws GameException {
        // Eliminate all but one player in game3 (4 players)
        while (game3.getPlayers().size() > 1) {
            game3.eliminateCurrentPlayer();
        }
        assertTrue(game3.isGameOver());
    }

    @Test
    @DisplayName("getWinner() returns null while game is in progress")
    void testGetWinnerNullDuringGame() {
        assertNull(game1.getWinner());
    }

    @Test
    @DisplayName("getWinner() returns last remaining player after game ends")
    void testGetWinnerAfterGameEnds() throws GameException {
        while (game3.getPlayers().size() > 1) {
            game3.eliminateCurrentPlayer();
        }
        assertNotNull(game3.getWinner());
        assertEquals(1, game3.getPlayers().size());
    }

    @Test
    @DisplayName("nextTurn() marks game over when only one player remains")
    void testNextTurnMarksGameOver() throws GameException {
        // Eliminate all but human in game1
        game1.nextTurn(); // go to machine
        game1.eliminateCurrentPlayer(); // eliminate machine
        assertTrue(game1.isGameOver());
    }

    // ── Table sum ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTableSum() returns updated sum after playing a card")
    void testTableSumUpdatesAfterPlay() throws GameException {
        int sumBefore = game1.getTableSum();
        IPlayer human = game1.getCurrentPlayer();
        int legalIndex = findLegalCardIndex(human, sumBefore);

        if (legalIndex >= 0) {
            Card card = human.getCard(legalIndex);
            int expectedSum = sumBefore + card.getValue(sumBefore);
            game1.humanPlayCard(legalIndex);
            assertEquals(expectedSum, game1.getTableSum());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Forces the table sum to a specific value by directly manipulating
     * the Table's addToSum method. Used to set up edge-case scenarios.
     */
    private void forceTableSum(Game game, int targetSum) {
        int current = game.getTableSum();
        game.getTable().addToSum(targetSum - current);
    }

    /**
     * Returns the index of the first legally playable card in the hand,
     * or -1 if none exists.
     */
    private int findLegalCardIndex(IPlayer player, int currentSum) {
        for (int i = 0; i < player.handSize(); i++) {
            Card card = player.getCard(i);
            if (card != null && card.isPlayable(currentSum)) return i;
        }
        return -1;
    }

    /**
     * Returns the index of the first card that would be illegal to play,
     * or -1 if all cards are legal.
     */
    private int findIllegalCardIndex(IPlayer player, int currentSum) {
        for (int i = 0; i < player.handSize(); i++) {
            Card card = player.getCard(i);
            if (card != null && !card.isPlayable(currentSum)) return i;
        }
        return -1;
    }
}
