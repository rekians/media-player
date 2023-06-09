package com.example.mp3player;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Controller implements Initializable {
    private ImageView iconPlay;
    private ImageView iconPause;
    private ImageView iconNext;
    private ImageView iconPrevious;
    private ImageView iconShuffle;
    private ImageView iconRepeat;
    private ImageView iconShuffleOn;
    private ImageView iconRepeatOn;
    @FXML
    private Button mediaLibrary;

    /**
     * текстовое поле для поиска
     */
    @FXML
    private TextField searchField;
    /**
     * кнопка "Добавить песню"
     */
    @FXML
    private Button songs;
    @FXML
    private Label playButton;

    @FXML
    private Label pauseButton;
    @FXML
    private Label repeatButton;

    private boolean isRepeating = false;

    @FXML
    private AnchorPane bottomMenu;

    @FXML
    private Pane pane;
    @FXML
    private Label songLabel;
    @FXML
    private Label labelCurrentTime;
    @FXML
    private Label labelTotalTime;
    @FXML
    private HBox mainButtons;
    @FXML
    private Label previousButton;
    @FXML
    private Label shuffleButton;
    @FXML
    private Label nextButton;
    @FXML
    private Button importDirectory;
    @FXML
    private Button addPlaylistButton;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Slider songSlider;
    private Media media;
    private MediaPlayer mediaPlayer;

    @FXML
    private ListView<String> listViewSongs;

    private ListView<String> listViewPlaylists;

    private Timer timer;
    private TimerTask task;
    private boolean running;
    private int flag1 = 0;
    private int flag2 = 0;
    private int songNumber;
    private int songMediaNumber;
    private boolean shuffle_on = false;
    private boolean prevShuffle = false;
    private boolean wasPlaying = false;
    private int savedVol;
    private ArrayList<String> playlist = new ArrayList<>();
    private ArrayList<String> playlist_names = new ArrayList<>();
    private static String current_playlist = "allSongs";
    private ArrayList<String> current_song_names = new ArrayList<>();
    private File main_directory = new File("C:\\Playlists");

    private int trackNumber;
    private String trackName;
    private String artistName;
    private Duration trackDuration;

    @FXML
    private Label chosenSongFromListView;

    /**
     * активация кнопки и наложение эффекта
     */
    @FXML
    void onMediaLibraryAsClick() throws IOException {
        stopCurrentSong();
        listViewSongs.getItems().clear();
        Scanner s = new Scanner(new File("C:\\Playlists\\allSongs.txt"));
        ArrayList<String> allSongs = new ArrayList<>();
        while (s.hasNextLine()) {
            String[] G = s.nextLine().split("/");
            int gLength = G.length - 1;
            allSongs.add(G[gLength].replaceAll(".mp3", ""));
        }
        s.close();
        listViewSongs.getItems().addAll(allSongs);
        listViewSongs.getSelectionModel().selectFirst();
        current_playlist = "allSongs";
        playPlaylist();
    }

    /**
     * активация кнопки "Плейлисты". загрузка плейлистов
     */
    @FXML
    void onPlaylistsAsClick() throws IOException {
        clearPlaylistTwoFile();
        clearPlaylistOneFile();
        try {
            stopCurrentSong();
            DirectoryChooser directoryChooser = new
                    DirectoryChooser();
            directoryChooser.setInitialDirectory(new File("C:\\Users\\Виктор\\Desktop\\учеба\\музыка\\плейлисты"));
            File playlist_import_dir = directoryChooser.showDialog(null);
            while (Objects.requireNonNull(playlist_import_dir.listFiles()).length == 0) {
                playlist_import_dir = directoryChooser.showDialog(null);
            }
            File dir = new File(playlist_import_dir.toURI());
            List<File> lst;
            lst = new ArrayList<>();
            flag1 = 0;
            String import_dir = null;
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if ((file.isFile()) & (file.toString().endsWith("mp3"))) {
                    lst.add(file);
                    String trackFromPlaylist = file.toString();
                    trackFromPlaylist = trackFromPlaylist.replaceAll("%20", " ");
                    trackFromPlaylist = trackFromPlaylist.replace("\\", "/");
                    String directory = playlist_import_dir.toString().replace("\\", "/");
                    ArrayList<String> array = new ArrayList<>(List.of(directory.split("/")));
                    import_dir = array.get(array.size() - 1);
                    FileWriter writer2 = new FileWriter("C:\\Playlists\\" + import_dir + ".txt", true);
                    BufferedWriter bufferWriter1 = new BufferedWriter(writer2);
                    bufferWriter1.write(trackFromPlaylist + "\n");
                    bufferWriter1.close();
                    File createFile = new File("C:\\Playlists\\" + import_dir + ".txt");
                    if (!createFile.exists())
                        try {
                            createFile.createNewFile();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                }
            }
            current_playlist = import_dir;
            System.out.println(current_playlist);
            updateSongs();
        } catch (RuntimeException | IOException e) {
            System.out.println("incorrect input");
        }
        if (listViewSongs != null) {
            bottomMenu.setVisible(true);
            setIcons();
            listViewSongs.getSelectionModel().selectFirst();
            playPlaylist();
        }
    }

    @FXML
    void addPlaylistToMedia() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Playlists\\" + current_playlist + ".txt"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Playlists\\allSongs.txt", true));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                writer.write(line);
                writer.newLine();
            }
        }
        reader.close();
        writer.close();
        System.out.println("Плейлист добавлен в медиатеку.");
    }


    /**
     * При нажатии кнопки "Добавить песню" появляется диалоговое окно, в котором предлагается выбрать песню для проигрывания.
     * Далее трек проигрывается
     *
     * @throws Exception при возникновении ошибки
     * @see FileChooser.ExtensionFilter при выборе песни в диалоговом окне есть фильтр на выбор файла в формате mp3
     */
    @FXML
    void onSongsAsClick() throws Exception {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser
                .ExtensionFilter("Выбрать файл (.mp3)", "*.mp3");
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialDirectory(new File("C:\\Users\\Виктор\\Desktop\\учеба\\музыка"));
        File file = fileChooser.showOpenDialog(null);
        String filePath = file.toURI().toString();
        current_playlist = "allSongs";
        String song = filePath.replaceAll("file:/", "");
        song = song.replaceAll("%20", " ");
        flag1 = 0;
        List<String> lines = FileUtils.readLines(new File("C:\\Playlists\\allSongs.txt"), "utf-8");
        if (lines.size() != 0) {
            for (String line : lines) {
                if (line.equals(song)) {
                    flag1++;
                }
            }
            if (flag1 == 0) {
                FileWriter writer1 = new FileWriter("C:\\Playlists\\allSongs.txt", true);
                BufferedWriter bufferWriter = new BufferedWriter(writer1);
                bufferWriter.write(song + "\n");
                bufferWriter.close();
            }
        } else {
            FileWriter writer1 = new FileWriter("C:\\Playlists\\allSongs.txt", true);
            BufferedWriter bufferWriter = new BufferedWriter(writer1);
            bufferWriter.write(song + "\n");
            bufferWriter.close();
        }
        stopCurrentSong();
        songLabel.setText(FilenameUtils.removeExtension(file.getName()));
        Media media = new Media(filePath);
        mediaPlayer = new MediaPlayer(media);
        bottomMenu.setVisible(true);
        setIcons();
        playMedia();
        listViewSongs.setVisible(true);
        updateSongs();
    }

    private void stopCurrentSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /**
     * @param time установка продолжительности трека
     * @return возращается формат продолжительности трека —:--:— или —:—
     */
    public String getTime(Duration time) {

        int hours = (int) time.toHours();
        int minutes = (int) time.toMinutes();
        int seconds = (int) time.toSeconds();

        if (seconds > 59) seconds = seconds % 60;
        if (minutes > 59) minutes = minutes % 60;
        if (hours > 59) hours = hours % 60;

        if (hours > 0) return String.format("%d:%02d:%02d",
                hours,
                minutes,
                seconds);
        else return String.format("%02d:%02d",
                minutes,
                seconds);
    }

    /**
     * работа с таймером
     */
    public void bindCurrentTimeLabel() {
        labelCurrentTime.textProperty().bind(Bindings.createStringBinding(() -> getTime(mediaPlayer.getCurrentTime()), mediaPlayer.currentTimeProperty()));
    }

    /**
     * начало работы таймлайна. если песня проигрывается, то видно сколько времени с начала песни прошли и сколько всего длится трек
     */
    public void beginTimer() {
        timer = new Timer();
        task = new TimerTask() {
            public void run() {
                running = true;
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double end = mediaPlayer.getTotalDuration().toSeconds();
                songSlider.setValue(current / end);
                if (current / end == 1) {
                    cancelTimer();
                }
            }
        };
    }

    /**
     * сохранение параметра предыдущего трека
     */
    private void savePar() {
        if (wasPlaying) {
            shuffle_on = prevShuffle;
            volumeSlider.setValue(savedVol);
            mediaPlayer.setVolume(savedVol * 0.01);
        } else {
            shuffle_on = false;
            volumeSlider.setValue(10.0);
            mediaPlayer.setVolume(10.0 * 0.01);
            savedVol = 10;

        }
    }


    /**
     * окончание работы таймлайна. если песня не проигрывается, то таймлайн перестает работать
     */
    public void cancelTimer() {
        running = false;
        timer.cancel();
    }


    /**
     * воспроизведение песни
     */
    @FXML
    private void playMedia() {
        mediaPlayer.play();
        running = true;
        volumeSlider.setValue(mediaPlayer.getVolume() * 100);
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                mediaPlayer.setVolume(volumeSlider.getValue() / 100);
            }
        });
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
                songSlider.setValue(newValue.toSeconds());
            }
        });
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldTime, Duration newTime) {
                bindCurrentTimeLabel();
                if (!songSlider.isValueChanging()) {
                    songSlider.setValue(newTime.toSeconds());
                }
                labelCurrentTime.getText();
                labelTotalTime.getText();
            }
        });
        mediaPlayer.totalDurationProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldDuration, Duration newDuration) {
                songSlider.setMax(newDuration.toSeconds());
                labelTotalTime.setText(getTime(newDuration));

            }
        });
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
                Duration total = mediaPlayer.getMedia().getDuration();
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

        addPlaylistButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    addPlaylistToMedia();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                    try {
                        playPlaylist();
                        songNumber = 0;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Обработка события завершения воспроизведения текущей песни
        mediaPlayer.setOnEndOfMedia(() -> {
            // Вызов метода для воспроизведения следующей песни
            try {
                forwardMedia();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * поставить на паузу
     */
    @FXML
    private void pauseMedia() {
        mediaPlayer.pause();
    }


    /**
     * проигрывание плейлиста
     */
    private void playPlaylist() throws IOException {
        List<String> lines = FileUtils.readLines(new File("C:\\Playlists\\" + current_playlist + ".txt"), "utf-8");
        String filePath = lines.get(songNumber);
        File f = new File(filePath);
        URI filepath = URI.create(("file:/" + filePath).replaceAll(" ", "%20"));
        media = new Media(filepath.toString());
        mediaPlayer = new MediaPlayer(media);
        bottomMenu.setVisible(true);
        songLabel.setText(FilenameUtils.removeExtension(f.getName()));
        playMedia();
        updateSongs();
//        updatePlaylist();
    }

    /**
     * обновление загруженных песен
     */
    private void updateSongs() throws IOException {
        Scanner s = new Scanner(new File("C:\\Playlists\\" + current_playlist + ".txt"));
        ArrayList<String> downloadedSongs = new ArrayList<>();
        while (s.hasNextLine()) {
            String[] G = s.nextLine().split("/");
            int gLength = G.length - 1;
            downloadedSongs.add(G[gLength].replaceAll(".mp3", ""));
        }
        s.close();
        listViewSongs.getItems().clear();
        listViewSongs.getItems().addAll(downloadedSongs);
    }

    /**
     * обновление плейлистов
     */
    private void updatePlaylist() {
        int playlist_amount = Objects.requireNonNull(main_directory.listFiles()).length;
        if (playlist_amount > 0) {
            playlist.clear();
            playlist_names.clear();
            listViewPlaylists.getItems().clear();
            for (File f : Objects.requireNonNull(main_directory.listFiles())) {
                playlist.add(f.getName());
                playlist_names.add(f.getName().replaceAll(".txt", ""));
            }
            listViewPlaylists.getItems().clear();
            listViewPlaylists.getItems().addAll(playlist_names);
        }
    }

    /**
     * установка иконок для кнопок "play", "pause", "next", "previous"
     */
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

        Image imageShuffle = new Image(new File("src/main/resources/com/example/mp3player/asserts/shuffle-button.png").toURI().toString());
        iconShuffle = new ImageView(imageShuffle);
        iconShuffle.setFitWidth(45);
        iconShuffle.setFitHeight(45);

        Image imageRepeat = new Image(new File("src/main/resources/com/example/mp3player/asserts/repeat-button.png").toURI().toString());
        iconRepeat = new ImageView(imageRepeat);
        iconRepeat.setFitWidth(45);
        iconRepeat.setFitHeight(45);

        nextButton.setGraphic(iconNext);
        playButton.setGraphic(iconPlay);
        pauseButton.setGraphic(iconPause);
        previousButton.setGraphic(iconPrevious);
        shuffleButton.setGraphic(iconShuffle);
        repeatButton.setGraphic(iconRepeat);
    }

    public void changeShuffleIconOnClick() {
        Image imageShuffleOn = new Image(new File("src/main/resources/com/example/mp3player/asserts/shuffle-button-on.png").toURI().toString());
        iconShuffleOn = new ImageView(imageShuffleOn);
        iconShuffleOn.setFitWidth(45);
        iconShuffleOn.setFitHeight(45);
    }

    public void changeRepeatIconOnClick() {
        Image imageRepeatOn = new Image(new File("src/main/resources/com/example/mp3player/asserts/repeat-button-on.png").toURI().toString());
        iconRepeatOn = new ImageView(imageRepeatOn);
        iconRepeatOn.setFitWidth(45);
        iconRepeatOn.setFitHeight(45);
    }

    public void repeatSong() {
        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.seek(Duration.millis(0));
            mediaPlayer.play();
        });
    }

    @FXML
    public void initialize(URL arg0, ResourceBundle arg1) {
        nextButton.setOnMouseClicked(mouseEvent -> {
            try {
                forwardMedia();
                listViewSongs.getSelectionModel().select(songNumber);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        previousButton.setOnMouseClicked(mouseEvent -> {
            try {
                previousMedia();
                listViewSongs.getSelectionModel().select(songNumber);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        shuffleButton.setOnMouseClicked(mouseEvent -> {
            if (shuffle_on) {
                shuffle_on = false;
                System.out.println("Шафл выключен");
                shuffleButton.setGraphic(iconShuffle);
            } else {
                shuffle_on = true;
                System.out.println("Шафл включен");
                changeShuffleIconOnClick();
                shuffleButton.setGraphic(iconShuffleOn);
            }
            prevShuffle = shuffle_on;
        });
        repeatButton.setOnMouseClicked((mouseEvent -> {
            if (!isRepeating) {
                isRepeating = true;
                System.out.println("Повтор включен");
                changeRepeatIconOnClick();
                repeatButton.setGraphic(iconRepeatOn);
                repeatSong(); // Вызываем метод повтора текущей песни
            } else {
                isRepeating = false;
                mediaPlayer.setOnEndOfMedia(null);
                System.out.println("Повтор выключен");
                repeatButton.setGraphic(iconRepeat);

            }
        }));
        searchField.setPromptText("Track or artist");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                searchSongs(newValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listViewSongs.setFocusTraversable(false);
        mediaLibrary.setOnMouseClicked(mouseEvent -> {
        });
        clearAllSongsFile();
        clearPlaylistOneFile();
        clearPlaylistTwoFile();
    }


    private void clearAllSongsFile() {
        try {
            File file = new File("C:\\Playlists\\allSongs.txt");
            if (file.exists()) {
                FileWriter writer = new FileWriter(file);
                writer.write(""); // Очищаем содержимое файла
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearPlaylistOneFile() {
        try {
            File file = new File("C:\\Playlists\\плейлист 1.txt");
            if (file.exists()) {
                FileWriter writer = new FileWriter(file);
                writer.write(""); // Очищаем содержимое файла
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearPlaylistTwoFile() {
        try {
            File file = new File("C:\\Playlists\\плейлист 2.txt");
            if (file.exists()) {
                FileWriter writer = new FileWriter(file);
                writer.write(""); // Очищаем содержимое файла
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchSongs(String searchText) throws IOException {
        List<String> songPaths = FileUtils.readLines(new File("C:\\Playlists\\" + current_playlist + ".txt"), "utf-8");
        List<String> songNames = new ArrayList<>();

        for (String path : songPaths) {
            String fileName = new File(path).getName();
            String nameWithoutExtension = removeFileExtension(fileName);
            songNames.add(nameWithoutExtension);
        }

        // Фильтровать список песен на основе введенного текста
        List<String> filteredSongs = new ArrayList<>();
        for (String song : songNames) {
            if (song.toLowerCase().contains(searchText.toLowerCase())) {
                filteredSongs.add(song);
            }
        }
        // Обновить ListView с отфильтрованными песнями
        listViewSongs.setItems(FXCollections.observableArrayList(filteredSongs));
    }

    private String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    /**
     * перемотка трека вперед
     */
    private void forwardMedia() throws IOException {
        List<String> lines = FileUtils.readLines(new File("C:\\Playlists\\" + current_playlist + ".txt"), "utf-8");
        if (!shuffle_on) {
            if (songNumber < lines.size() - 1) {
                mediaPlayer.stop();
                mediaPlayer.seek(Duration.millis(0));
                songNumber++;
                playPlaylist();
                listViewSongs.getSelectionModel().select(songNumber);
            } else {
                mediaPlayer.stop();
                mediaPlayer.seek(Duration.millis(0));
                songNumber = 0;
                playPlaylist();
                listViewSongs.getSelectionModel().select(songNumber);
            }
        } else {
            mediaPlayer.stop();
            mediaPlayer.seek(Duration.millis(0));
            songNumber = getRandom(1, lines.size() - 1);
            listViewSongs.getSelectionModel().select(songNumber);
            playPlaylist();
        }
    }

    /**
     * Перемотка трека назад
     */
    private void previousMedia() throws IOException {
        List<String> lines = FileUtils.readLines(new File("C:\\Playlists\\" + current_playlist + ".txt"), "utf-8");
        if (!shuffle_on) {
            if (songNumber > 0) {
                mediaPlayer.stop();
                mediaPlayer.seek(Duration.millis(0));
                songNumber--;
                playPlaylist();
            } else {
                mediaPlayer.stop();
                mediaPlayer.seek(Duration.millis(0));
                songNumber = lines.size() - 1;
                playPlaylist();
            }
        } else {
            mediaPlayer.stop();
            mediaPlayer.seek(Duration.millis(0));
            songNumber = getRandom(1, lines.size() - 1);
            listViewSongs.getSelectionModel().select(songNumber);
            playPlaylist();
        }
    }

    /**
     * Возврашение случайного целого числа из заданного диапазоны
     */
    public static int getRandom(int min, int max) {
        return (int) ((Math.random() * ((max - min) + 1)) + min);
    }
}
