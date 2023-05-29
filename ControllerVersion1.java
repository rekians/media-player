package com.example.mp3player;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerVersion1 implements Initializable {
    private ImageView iconPlay;
    private ImageView iconPause;
    private ImageView iconNext;
    private ImageView iconPrevious;
    private ImageView iconShuffle;

    private Button mediaLibrary;

    @FXML
    private Label playButton;
    @FXML
    private Label pauseButton;
    @FXML
    private Label previousButton;
    @FXML
    private Label shuffleButton;
    @FXML
    private Label nextButton;

    @FXML
    private AnchorPane bottomMenu;

    @FXML
    private Label songLabel;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Slider songSlider;
    private Media media;
    private MediaPlayer mediaPlayer;

    @FXML
    private ListView<String> listViewSongs;
    private ListView<String> listViewPlaylists;

    private int songNumber;
    private boolean running;

    private void playMedia() {
        mediaPlayer.play();
        running = true;
        songSlider.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediaPlayer.seek(Duration.seconds(songSlider.getValue()));

            }
        });
        songSlider.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                mediaPlayer.seek(Duration.seconds(songSlider.getValue()));
            }
        });
        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                Duration total = media.getDuration();
                songSlider.setMax(total.toSeconds());
            }
        });
        playButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                playMedia();
            }
        });
        pauseButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                pauseMedia();
            }
        });

        listViewSongs.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() > 1) {
                    if (running) {
                        mediaPlayer.stop();
                    }
                    songNumber = listViewSongs.getSelectionModel().getSelectedIndex();
                }
            }
        });

    }

    @FXML
    private void pauseMedia() {
        mediaPlayer.pause();
    }

    public void setIcons() {
        Image imageNext = new Image(new File("src/main/resources/com/example/mp3player/asserts/next-button.png").toURI().toString());
        iconNext = new ImageView(imageNext);
        iconNext.setFitWidth(40);
        iconNext.setFitHeight(40);

        Image imagePause = new Image(new File("src/main/resources/com/example/mp3player/asserts/pause-button.png").toURI().toString());
        iconPause = new ImageView(imagePause);
        iconPause.setFitWidth(60);
        iconPause.setFitHeight(60);

        Image imagePlay = new Image(new File("src/main/resources/com/example/mp3player/asserts/play-button.png").toURI().toString());
        iconPlay = new ImageView(imagePlay);
        iconPlay.setFitWidth(55);
        iconPlay.setFitHeight(55);

        Image imagePrevious = new Image(new File("src/main/resources/com/example/mp3player/asserts/previous-button.png").toURI().toString());
        iconPrevious = new ImageView(imagePrevious);
        iconPrevious.setFitWidth(40);
        iconPrevious.setFitHeight(40);

        Image imageShuffle= new Image(new File("src/main/resources/com/example/mp3player/asserts/shuffle-button.png").toURI().toString());
        iconShuffle = new ImageView(imageShuffle);
        iconShuffle.setFitWidth(45);
        iconShuffle.setFitHeight(45);

        nextButton.setGraphic(iconNext);
        playButton.setGraphic(iconPlay);
        pauseButton.setGraphic(iconPause);
        previousButton.setGraphic(iconPrevious);
        shuffleButton.setGraphic(iconShuffle);
    }

    @FXML
    public void initialize(URL arg0, ResourceBundle arg1) {
        nextButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                listViewSongs.getSelectionModel().select(songNumber);
            }
        });
        previousButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                listViewSongs.getSelectionModel().select(songNumber);
            }
        });

    }
}

