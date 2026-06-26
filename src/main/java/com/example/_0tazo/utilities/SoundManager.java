package com.example._0tazo.utilities;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.Objects;

public class SoundManager {

    private static SoundManager instance;
    private final AudioClip cardPlaySound;
    private final AudioClip eliminatedSound;
    private final AudioClip winSound;
    private MediaPlayer backgroundMusic;
    private static final String BASE = "/com/example/_0tazo/assets/sounds/";

    private SoundManager() {
        cardPlaySound   = loadClip("card_play.wav");
        eliminatedSound = loadClip("eliminated.wav");
        winSound        = loadClip("win.wav");
        loadBackgroundMusic("background.mp3");
    }

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    public void playCardSound() {
        if (cardPlaySound != null) cardPlaySound.play();
    }

    public void playEliminatedSound() {
        if (eliminatedSound != null) eliminatedSound.play();
    }

    public void playWinSound() {
        if (winSound != null) winSound.play();
    }

    public void playBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusic.setVolume(0.3);
            backgroundMusic.play();
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) backgroundMusic.stop();
    }

    private void loadBackgroundMusic(String filename) {
        try {
            String path = Objects.requireNonNull(
                    getClass().getResource(BASE + filename)
            ).toString();
            backgroundMusic = new MediaPlayer(new Media(path));
        } catch (Exception e) {
            System.err.println("Sound not found: " + filename);
        }
    }

    private AudioClip loadClip(String filename) {
        try {
            return new AudioClip(Objects.requireNonNull(
                    getClass().getResource(BASE + filename)
            ).toString());
        } catch (Exception e) {
            System.err.println("Sound not found: " + filename);
            return null;
        }
    }
}
