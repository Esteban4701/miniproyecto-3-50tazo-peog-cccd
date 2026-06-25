package com.example._0tazo.utilities;

import com.example._0tazo.utilities.ITimerListener;
import java.util.Random;

/**
 * Manages the two timer threads used during a Cincuentazo game:
 *
 * <ol>
 *   <li><b>General timer</b> — runs for the entire game, firing
 *       {@link ITimerListener#onTick(int)} every second so the controller
 *       can update the elapsed-time label in the UI.</li>
 *   <li><b>Machine turn timer</b> — fires once after a random delay of
 *       2–4 seconds ({@link ITimerListener#onMachineTurnReady()}) to
 *       simulate the computer "thinking", then fires again after 1–2 seconds
 *       ({@link ITimerListener#onMachineDrawReady()}) to simulate drawing.</li>
 * </ol>
 *
 * <p>Both threads are daemon threads so they stop automatically when the
 * JavaFX application exits. The general timer can be paused and resumed
 * (e.g. while a win/lose overlay is shown), and both timers can be stopped
 * cleanly when the game ends.</p>
 *
 * <p>This class has no JavaFX dependency — the listener implementation in
 * the controller is responsible for calling {@code Platform.runLater()}
 * before touching any UI node.</p>
 *
 * <p>Typical usage from the controller:</p>
 * <pre>{@code
 * GameTimer timer = new GameTimer(this); // controller implements ITimerListener
 * timer.startGeneralTimer();             // begin counting elapsed time
 *
 * // When a machine turn starts:
 * timer.startMachineTurnTimer();
 * // → onMachineTurnReady() fires after 2–4 s  (controller plays the card)
 * // → onMachineDrawReady() fires after 1–2 s more (controller draws)
 *
 * // When the game ends:
 * timer.stopAll();
 * }</pre>
 *
 * @author  Paulo Esteban Ordoñez Gutiérrez
 * @author Cristian Camilo Criollo Diaz
 * @version 1.0
 * @see     ITimerListener
 */
public class GameTimer {

    // ── Constants ─────────────────────────────────────────────────────────────

    /** Minimum delay in seconds before the machine plays a card. */
    private static final int MACHINE_PLAY_MIN = 2;

    /** Maximum delay in seconds before the machine plays a card. */
    private static final int MACHINE_PLAY_MAX = 4;

    /** Minimum delay in seconds before the machine draws a card. */
    private static final int MACHINE_DRAW_MIN = 1;

    /** Maximum delay in seconds before the machine draws a card. */
    private static final int MACHINE_DRAW_MAX = 2;

    // ── State ─────────────────────────────────────────────────────────────────

    /** Listener that receives all timer callbacks. */
    private final ITimerListener listener;

    /** Random instance for generating machine delays. */
    private final Random random;

    /** The general game timer thread. */
    private Thread generalTimerThread;

    /** The machine turn timer thread. */
    private Thread machineTurnThread;

    /** Total seconds elapsed since the general timer started. */
    private int elapsedSeconds;

    /** Controls whether the general timer is running. */
    private volatile boolean generalRunning;

    /** Controls whether the general timer is paused. */
    private volatile boolean generalPaused;

    /** Controls whether the machine turn timer is running. */
    private volatile boolean machineRunning;

    private volatile boolean stopped = false;

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Constructs a {@code GameTimer} with the given event listener.
     *
     * @param listener the {@link ITimerListener} that will receive timer callbacks;
     *                 typically the game controller
     */
    public GameTimer(ITimerListener listener) {
        this.listener       = listener;
        this.random         = new Random();
        this.elapsedSeconds = 0;
        this.generalRunning = false;
        this.generalPaused  = false;
        this.machineRunning = false;
    }

    // ── General timer ─────────────────────────────────────────────────────────

    /**
     * Starts the general game timer, firing {@link ITimerListener#onTick(int)}
     * every second until {@link #stopAll()} or {@link #stopGeneralTimer()} is called.
     *
     * <p>If the timer is already running this method does nothing.</p>
     */
    public void startGeneralTimer() {
        stopped = false;
        if (generalRunning) return;

        generalRunning = true;
        generalPaused  = false;
        elapsedSeconds = 0;

        generalTimerThread = new Thread(() -> {
            while (generalRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                if (!generalPaused && generalRunning) {
                    elapsedSeconds++;
                    listener.onTick(elapsedSeconds);
                }
            }
        });

        generalTimerThread.setDaemon(true);
        generalTimerThread.setName("general-timer");
        generalTimerThread.start();
    }

    /**
     * Pauses the general timer. The elapsed time stops incrementing and
     * {@link ITimerListener#onTick(int)} stops firing until {@link #resumeGeneralTimer()}
     * is called.
     */
    public void pauseGeneralTimer() {
        generalPaused = true;
    }

    /**
     * Resumes the general timer after a {@link #pauseGeneralTimer()} call.
     */
    public void resumeGeneralTimer() {
        generalPaused = false;
    }

    /**
     * Stops the general timer permanently. The thread is interrupted and
     * the elapsed time is reset to zero.
     */
    public void stopGeneralTimer() {
        generalRunning = false;
        if (generalTimerThread != null) {
            generalTimerThread.interrupt();
        }
        elapsedSeconds = 0;
    }

    // ── Machine turn timer ────────────────────────────────────────────────────

    /**
     * Starts the machine turn timer sequence:
     *
     * <ol>
     *   <li>Waits a random delay between {@value #MACHINE_PLAY_MIN} and
     *       {@value #MACHINE_PLAY_MAX} seconds, then fires
     *       {@link ITimerListener#onMachineTurnReady()}.</li>
     *   <li>Waits a further random delay between {@value #MACHINE_DRAW_MIN} and
     *       {@value #MACHINE_DRAW_MAX} seconds, then fires
     *       {@link ITimerListener#onMachineDrawReady()}.</li>
     * </ol>
     *
     * <p>If a machine turn timer is already running it is stopped before
     * starting the new one.</p>
     */
    public void startMachineTurnTimer() {
        stopMachineTurnTimer();

        machineRunning     = true;
        machineTurnThread  = new Thread(() -> {
            try {
                // Phase 1 — thinking delay before playing
                int playDelay = randomDelay(MACHINE_PLAY_MIN, MACHINE_PLAY_MAX);
                Thread.sleep(playDelay * 1000L);

                if (!machineRunning) return;
                listener.onMachineTurnReady();

                // Phase 2 — pause before drawing
                int drawDelay = randomDelay(MACHINE_DRAW_MIN, MACHINE_DRAW_MAX);
                Thread.sleep(drawDelay * 1000L);

                if (!machineRunning) return;
                listener.onMachineDrawReady();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                machineRunning = false;
            }
        });

        machineTurnThread.setDaemon(true);
        machineTurnThread.setName("machine-turn-timer");
        machineTurnThread.start();
    }

    /**
     * Stops the machine turn timer if it is currently running.
     * Any pending {@code onMachineTurnReady} or {@code onMachineDrawReady}
     * callbacks will not fire after this call.
     */
    public void stopMachineTurnTimer() {
        machineRunning = false;
        if (machineTurnThread != null) {
            machineTurnThread.interrupt();
        }
    }

    // ── Stop all ──────────────────────────────────────────────────────────────

    /**
     * Stops both timers. Should be called when the game ends or the
     * application closes.
     */
    public void stopAll() {
        stopped = true;
        generalRunning = false;
        machineRunning = false;

        if (generalTimerThread != null) {
            generalTimerThread.interrupt();
            try {
                generalTimerThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (machineTurnThread != null) {
            machineTurnThread.interrupt();
            try {
                machineTurnThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        elapsedSeconds = 0;
        generalTimerThread = null;
        machineTurnThread  = null;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns the total seconds elapsed since the general timer started.
     *
     * @return elapsed seconds
     */
    public int getElapsedSeconds() { return elapsedSeconds; }

    /**
     * Returns {@code true} if the general timer is currently running and not paused.
     *
     * @return {@code true} if ticking
     */
    public boolean isGeneralRunning() { return generalRunning && !generalPaused; }

    /**
     * Returns {@code true} if the machine turn timer is currently active.
     *
     * @return {@code true} if a machine delay is in progress
     */
    public boolean isMachineTimerRunning() { return machineRunning; }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Returns a random integer between {@code min} and {@code max} inclusive.
     *
     * @param min minimum value
     * @param max maximum value
     * @return a random int in [{@code min}, {@code max}]
     */
    private int randomDelay(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}