package com.example._0tazo.utilities
;

/**
 * Callback interface that receives timer events from {@link GameTimer}.
 *
 * <p>The controller implements this interface and uses
 * {@code Platform.runLater()} inside each method to safely update
 * the JavaFX UI from the timer threads.</p>
 *
 * <p>Example implementation in the controller:</p>
 * <pre>{@code
 * public class GameController implements ITimerListener {
 *
 *     @Override
 *     public void onTick(int elapsedSeconds) {
 *         Platform.runLater(() ->
 *             timerLabel.setText(formatTime(elapsedSeconds))
 *         );
 *     }
 *
 *     @Override
 *     public void onMachineTurnReady() {
 *         Platform.runLater(() -> executeMachineTurn());
 *     }
 *
 *     @Override
 *     public void onMachineDrawReady() {
 *         Platform.runLater(() -> executeMachineDraw());
 *     }
 * }
 * }</pre>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @version 1.0
 * @see     GameTimer
 */
public interface ITimerListener {

    /**
     * Called every second by the general game timer thread.
     *
     * <p>Use this to update the elapsed-time label in the UI.</p>
     *
     * @param elapsedSeconds total seconds elapsed since the game started
     */
    void onTick(int elapsedSeconds);

    /**
     * Called when the machine turn delay (2–4 s) has elapsed and the
     * computer player is ready to play a card.
     *
     * <p>The controller should respond by calling
     *  on the JavaFX thread.</p>
     */
    void onMachineTurnReady();

    /**
     * Called when the machine draw delay (1–2 s) has elapsed and the
     * computer player is ready to draw a replacement card.
     *
     * <p>The controller should respond by calling
     * on the JavaFX thread.</p>
     */
    void onMachineDrawReady();
}