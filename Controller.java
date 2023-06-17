package com.example.mp3player;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Controller implements Initializable {
    public Button songs;
    public HBox mainButtons;
    public Button playlists;
    @FXML
    private ImageView iconShuffle;
    @FXML
    private ImageView iconRepeat;
    @FXML
    private ImageView iconShuffleOn;
    @FXML
    private ImageView iconRepeatOn;
    @FXML
    private Button mediaLibrary;
    @FXML
    private TextField searchField;
    @FXML
    private Label playButton;
    @FXML
    private Label pauseButton;
    @FXML
    private Label repeatButton;
    @FXML
    private AnchorPane bottomMenu;
    @FXML
    private Label songLabel;
    @FXML
    private Label labelCurrentTime;
    @FXML
    private Label labelTotalTime;
    @FXML
    private Label previousButton;
    @FXML
    private Label shuffleButton;
    @FXML
    private Label nextButton;
    @FXML
    private Label backBeforeSearch;
    @FXML
    private Button addPlaylistButton;
    @FXML
    private Button createPlaylistButton;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Slider songSlider;
    @FXML
    private ListView<String> listViewSongs;

    private Media media;
    private MediaPlayer mediaPlayer;

    private boolean running;
    private boolean isRepeating = false;
    private int flag1 = 0;
    private int songNumber = 0;
    private boolean shuffle_on = false;
    private static String currentPlaylist = "allSongs";
    private static String workPlaylist;
    private static String savedPlaylist;
    private static List<Integer> playedSongs = new ArrayList<>();

    @FXML
    void onMediaLibraryAsClick() throws IOException {
        stopCurrentSong();
        listViewSongs.getItems().clear();
        File file = new File("C:\\Playlists\\allSongs.txt");
        if (!file.exists() || file.length() == 0) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            System.out.println("Медиатека пуста");
            songLabel.setText("");
            return;
        }
        Scanner s = new Scanner(file);
        ArrayList<String> allSongs = new ArrayList<>();
        while (s.hasNextLine()) {
            String[] G = s.nextLine().split("/");
            int gLength = G.length - 1;
            allSongs.add(G[gLength].replaceAll(".mp3", ""));
        }
        s.close();
        listViewSongs.getItems().addAll(allSongs);
        listViewSongs.getSelectionModel().selectFirst();
        currentPlaylist = "allSongs";
        songNumber = 0;
        playPlaylist();
    }

    @FXML
    void onSongsAsClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter filter = new FileChooser
                    .ExtensionFilter("Выбрать файл (.mp3)", "*.mp3");
            fileChooser.getExtensionFilters().add(filter);
            fileChooser.setInitialDirectory(new File("C:\\Users\\Виктор\\Desktop\\учеба\\музыка"));
            File file = fileChooser.showOpenDialog(null);
            String filePath = file.toURI().toString();
            currentPlaylist = "allSongs";
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
            songNumber = listViewSongs.getSelectionModel().getSelectedIndex();
        } catch (RuntimeException | IOException e) {
            System.out.println("Incorrect input");
        }
    }

    @FXML
    void onPlaylistsAsClick() {
        cleanFiles();
        try {
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
            currentPlaylist = import_dir;
            System.out.println(currentPlaylist);
            updateSongs();
            if (listViewSongs != null) {
                bottomMenu.setVisible(true);
                setIcons();
                listViewSongs.getSelectionModel().selectFirst();
                stopCurrentSong();
                songNumber = 0;
                playPlaylist();
            }
        } catch (RuntimeException | IOException e) {
            System.out.println("Incorrect input");
        }
    }

    @FXML
    void addPlaylistToMedia() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Playlists\\" + currentPlaylist + ".txt"));
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

    @FXML
    void createPlaylist() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory == null) {
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Создание плейлиста");
        dialog.setHeaderText("Введите название плейлиста");
        dialog.setContentText("Название:");
        dialog.setGraphic(null);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isEmpty()) {
            return;
        }
        String playlistName = result.get();
        File playlistFolder = new File(selectedDirectory.getAbsolutePath(), playlistName);
        if (!playlistFolder.exists()) {
            if (!playlistFolder.mkdir()) {
                System.out.println("Failed to create playlist folder");
                return;
            }
        }
        File allSongsFile = new File("C:\\Playlists\\allSongs.txt");
        if (!allSongsFile.exists()) {
            System.out.println("allSongs.txt file not found");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(allSongsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] songData = line.split("/");
                String songFileName = songData[songData.length - 1];
                Path sourceFile = Paths.get(line);
                Path targetFile = Paths.get(playlistFolder.getAbsolutePath(), songFileName);

                Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("Плейлист успешно создан!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void playMedia() {
        mediaPlayer.play();
        running = true;
        volumeSlider.setValue(mediaPlayer.getVolume() * 100);
        volumeSlider.valueProperty().addListener(observable -> mediaPlayer.setVolume(volumeSlider.getValue() / 100));
        mediaPlayer.currentTimeProperty().addListener((observableValue, oldValue, newValue) -> songSlider.setValue(newValue.toSeconds()));
        mediaPlayer.currentTimeProperty().addListener((observableValue, oldTime, newTime) -> {
            bindCurrentTimeLabel();
            if (!songSlider.isValueChanging()) {
                songSlider.setValue(newTime.toSeconds());
            }
            labelCurrentTime.getText();
            labelTotalTime.getText();
        });
        mediaPlayer.totalDurationProperty().addListener((observableValue, oldDuration, newDuration) -> {
            songSlider.setMax(newDuration.toSeconds());
            labelTotalTime.setText(getTime(newDuration));

        });
        songSlider.setOnMousePressed(mouseEvent -> mediaPlayer.seek(Duration.seconds(songSlider.getValue())));
        songSlider.setOnMouseDragged(mouseEvent -> mediaPlayer.seek(Duration.seconds(songSlider.getValue())));
        mediaPlayer.setOnReady(() -> {
            Duration total = mediaPlayer.getMedia().getDuration();
            songSlider.setMax(total.toSeconds());
        });
        playButton.setOnMouseClicked(mouseEvent -> playMedia());
        pauseButton.setOnMouseClicked(mouseEvent -> pauseMedia());

        addPlaylistButton.setOnMouseClicked(mouseEvent -> {
            try {
                addPlaylistToMedia();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        listViewSongs.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                if (running) {
                    mediaPlayer.stop();
                }
                songNumber = listViewSongs.getSelectionModel().getSelectedIndex();
                try {
                    playPlaylist();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            try {
                forwardMedia();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void pauseMedia() {
        mediaPlayer.pause();
    }

    public void setIcons() {
        Image imageNext = new Image(new File("src/main/resources/com/example/mp3player/asserts/next-button.png").toURI().toString());
        ImageView iconNext = new ImageView(imageNext);
        iconNext.setFitWidth(40);
        iconNext.setFitHeight(40);

        Image imagePause = new Image(new File("src/main/resources/com/example/mp3player/asserts/pause-button.png").toURI().toString());
        ImageView iconPause = new ImageView(imagePause);
        iconPause.setFitWidth(60);
        iconPause.setFitHeight(60);

        Image imagePlay = new Image(new File("src/main/resources/com/example/mp3player/asserts/play-button.png").toURI().toString());
        ImageView iconPlay = new ImageView(imagePlay);
        iconPlay.setFitWidth(55);
        iconPlay.setFitHeight(55);

        Image imagePrevious = new Image(new File("src/main/resources/com/example/mp3player/asserts/previous-button.png").toURI().toString());
        ImageView iconPrevious = new ImageView(imagePrevious);
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

        Image imageBack = new Image(new File("src/main/resources/com/example/mp3player/asserts/back.png").toURI().toString());
        ImageView iconBack = new ImageView(imageBack);
        iconBack.setFitWidth(30);
        iconBack.setFitHeight(30);

        nextButton.setGraphic(iconNext);
        playButton.setGraphic(iconPlay);
        pauseButton.setGraphic(iconPause);
        previousButton.setGraphic(iconPrevious);
        shuffleButton.setGraphic(iconShuffle);
        repeatButton.setGraphic(iconRepeat);
        backBeforeSearch.setGraphic(iconBack);
    }

    public void changeShuffleIconOnClick() throws IOException {
        Image imageShuffleOn = new Image(new File("src/main/resources/com/example/mp3player/asserts/shuffle-button-on.png").toURI().toString());
        iconShuffleOn = new ImageView(imageShuffleOn);
        iconShuffleOn.setFitWidth(45);
        iconShuffleOn.setFitHeight(45);
    }

    public void changeRepeatIconOnClick() throws IOException {
        Image imageRepeatOn = new Image(new File("src/main/resources/com/example/mp3player/asserts/repeat-button-on.png").toURI().toString());
        iconRepeatOn = new ImageView(imageRepeatOn);
        iconRepeatOn.setFitWidth(45);
        iconRepeatOn.setFitHeight(45);
    }

    private void stopCurrentSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

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

    public void bindCurrentTimeLabel() {
        if (mediaPlayer != null)
            labelCurrentTime.textProperty().bind(Bindings.createStringBinding(() -> getTime(mediaPlayer.getCurrentTime()), mediaPlayer.currentTimeProperty()));
    }

    private void playPlaylist() throws IOException {
        List<String> lines = FileUtils.readLines(new File("C:\\Playlists\\" + currentPlaylist + ".txt"), "utf-8");
        String filePath = lines.get(songNumber);
        File f = new File(filePath);
        URI filepath = URI.create(("file:/" + filePath).replaceAll(" ", "%20"));
        media = new Media(filepath.toString());
        mediaPlayer = new MediaPlayer(media);
        bottomMenu.setVisible(true);
        songLabel.setText(FilenameUtils.removeExtension(f.getName()));
        playMedia();
        updateSongs();
    }

    private void updateSongs() throws IOException {
        Scanner s = new Scanner(new File("C:\\Playlists\\" + currentPlaylist + ".txt"));
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

    public void repeatSong() throws IOException {
        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.seek(Duration.millis(0));
            mediaPlayer.play();
        });
    }

    private void previousMedia() throws IOException {
        savedPlaylist = currentPlaylist;
        if (Objects.equals(workPlaylist, "filteredSongs")) {
            currentPlaylist = workPlaylist;
        }
        List<String> lines;
        lines = FileUtils.readLines(new File("C:\\Playlists\\" + currentPlaylist + ".txt"), "utf-8");
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
            songNumber = getRandom(0, lines.size() - 1);
            listViewSongs.getSelectionModel().select(songNumber);
            playPlaylist();
        }
        currentPlaylist = savedPlaylist;
    }

    private void forwardMedia() throws IOException {
        List<String> lines;
        savedPlaylist = currentPlaylist;
        if (Objects.equals(workPlaylist, "filteredSongs")) {
            currentPlaylist = workPlaylist;
        }
        lines = FileUtils.readLines(new File("C:\\Playlists\\" + currentPlaylist + ".txt"), "utf-8");
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
            songNumber = getRandom(0, lines.size() - 1);
            listViewSongs.getSelectionModel().select(songNumber);
            playPlaylist();
        }
        currentPlaylist = savedPlaylist;
    }

    private void searchSongs(String searchText) throws IOException {
        workPlaylist = currentPlaylist;
        List<String> songPaths = FileUtils.readLines(new File("C:\\Playlists\\" + workPlaylist + ".txt"), "utf-8");
        List<String> songNames = new ArrayList<>();

        for (String path : songPaths) {
            String fileName = new File(path).getName();
            String nameWithoutExtension = removeFileExtension(fileName);
            songNames.add(nameWithoutExtension);
        }

        List<String> filteredSongs = new ArrayList<>();
        for (String song : songNames) {
            if (song.toLowerCase().contains(searchText.toLowerCase())) {
                clearFilteredSongsFile();
                filteredSongs.add(song);
            }
        }

        listViewSongs.setItems(FXCollections.observableArrayList(filteredSongs));
        File filteredSongsFile = new File("C:\\Playlists\\filteredSongs.txt");
        List<String> lines = new ArrayList<>();
        for (String song : filteredSongs) {
            String filePath;
            if (!Objects.equals(currentPlaylist, "allSongs")) {
                filePath = "C:/Users/Виктор/Desktop/учеба/музыка/плейлисты/" + currentPlaylist + "/" + song + ".mp3";
            } else {
                filePath = "C:/Users/Виктор/Desktop/учеба/музыка/" + song + ".mp3";
            }
            lines.add(filePath);
        }
        Path path = filteredSongsFile.toPath();
        Files.write(path, lines, StandardCharsets.UTF_8);

        listViewSongs.setOnMouseClicked(event -> {
            workPlaylist = "filteredSongs";
            if (event.getClickCount() == 2) {
                String selectedSong = listViewSongs.getSelectionModel().getSelectedItem();
                String songPath = findSongPath(selectedSong, songPaths);
                if (songPath != null) {
                    Media media = new Media(new File(songPath).toURI().toString());
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.dispose();
                    }
                    mediaPlayer = new MediaPlayer(media);
                    playMedia();
                    bottomMenu.setVisible(true);
                    songLabel.setText(selectedSong);
                }
            }
        });

        backBeforeSearch.setOnMouseClicked((mouseEvent -> {
            searchField.clear();
            listViewSongs.setItems(FXCollections.observableArrayList(songNames));
            clearFilteredSongsFile();
        }));
    }

    private void cleanFiles() {
        File folder = new File("C:\\Playlists\\");
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && !file.getName().equals("filteredSongs.txt") && !file.getName().equals("allSongs.txt")) {
                    clearFileContent(file);
                }
            }
        }
    }

    private static void clearFileContent(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void clearFilteredSongsFile() {
        try {
            File file = new File("C:\\Playlists\\filteredSongs.txt");
            if (file.exists()) {
                FileWriter writer = new FileWriter(file);
                writer.write(""); // Очищаем содержимое файла
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String findSongPath(String songName, List<String> songPaths) {
        for (String path : songPaths) {
            String fileName = new File(path).getName();
            String nameWithoutExtension = removeFileExtension(fileName);
            if (nameWithoutExtension.equalsIgnoreCase(songName)) {
                return path;
            }
        }
        return null;
    }

    private String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    public static int getRandom(int min, int max) {
        if (playedSongs.size() >= (max - min + 1)) {
            playedSongs.clear();
        }
        int random;
        do {
            random = (int) ((Math.random() * ((max - min) + 1)) + min);
        } while (playedSongs.contains(random));
        playedSongs.add(random);
        return random;
    }

    @FXML
    public void initialize(URL arg0, ResourceBundle arg1) {
        nextButton.setOnMouseClicked(mouseEvent -> {
            try {
                pauseMedia();
                forwardMedia();
                listViewSongs.getSelectionModel().select(songNumber);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        previousButton.setOnMouseClicked(mouseEvent -> {
            try {
                pauseMedia();
                previousMedia();
                listViewSongs.getSelectionModel().select(songNumber);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        shuffleButton.setOnMouseClicked(mouseEvent -> {
            try {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        repeatButton.setOnMouseClicked((mouseEvent -> {
            try {
                if (!isRepeating) {
                    isRepeating = true;
                    System.out.println("Повтор включен");
                    changeRepeatIconOnClick();
                    repeatButton.setGraphic(iconRepeatOn);
                    repeatSong();
                } else {
                    isRepeating = false;
                    mediaPlayer.setOnEndOfMedia(null);
                    System.out.println("Повтор выключен");
                    repeatButton.setGraphic(iconRepeat);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        createPlaylistButton.setOnMouseClicked(((mouseEvent -> createPlaylist())));
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
        cleanFiles();
    }
}
