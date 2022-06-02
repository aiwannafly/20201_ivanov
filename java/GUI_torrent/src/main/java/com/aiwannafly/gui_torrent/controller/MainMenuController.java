package com.aiwannafly.gui_torrent.controller;

import com.aiwannafly.gui_torrent.ApplicationStarter;
import com.aiwannafly.gui_torrent.torrent.Constants;
import com.aiwannafly.gui_torrent.torrent.client.TorrentClient;
import com.aiwannafly.gui_torrent.torrent.client.exceptions.*;
import com.aiwannafly.gui_torrent.torrent.client.util.torrent.Torrent;
import com.aiwannafly.gui_torrent.torrent.client.util.torrent.TorrentParser;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.aiwannafly.gui_torrent.view.Renderer.*;

public class MainMenuController {
    @FXML
    public Button distributeButton;

    @FXML
    public Button createButton;
    public VBox menu;

    @FXML
    private Button downloadButton;
    private final static Map<String, FileSection> fileSections = new HashMap<>();
    private final static Set<String> downloadedTorrents = new HashSet<>();

    @FXML
    protected void onDownloadButtonClick() {
        TorrentClient torrentClient = ApplicationStarter.getTorrentClient();
        assert torrentClient != null;
        File file = chooseFile("Choose a file to download");
        if (file == null) {
            return;
        }
        String torrentFilePath = file.getAbsolutePath();
        String postfix = Constants.POSTFIX;
        String torrentFileName = torrentFilePath.substring(torrentFilePath.lastIndexOf(Constants.PATH_DIVIDER) + 1);
        int originalFileLength = torrentFileName.length() - postfix.length();
        String originalFileName;
        if (originalFileLength <= 0) {
            originalFileName = Constants.PREFIX;
        } else {
            originalFileName = Constants.PREFIX + torrentFilePath.substring(0, originalFileLength);
        }
        if (torrentFileName.length() <= postfix.length()) {
            showErrorAlert("Bad file name, it should end with " + postfix);
            return;
        }
        try {
            torrentClient.download(torrentFilePath);
        } catch (BadTorrentFileException | BadServerReplyException |
                NoSeedsException | ServerNotCorrespondsException e) {
            showErrorAlert("Could not download " + originalFileName
                    + ": " + e.getMessage());
            return;
        }
        FileSection fileSection = makeNewFileSection(torrentFilePath, Status.DOWNLOADING);
        showFileSection(fileSection);
        fileSections.put(torrentFileName, fileSection);
        ObservableList<Integer> collectedPieces;
        try {
            collectedPieces = torrentClient.getCollectedPieces(torrentFileName);
        } catch (BadTorrentFileException e) {
            e.printStackTrace();
            return;
        }
        collectedPieces.addListener((ListChangeListener<? super Integer>) change -> {
            Platform.runLater(() -> {
                while (fileSection.sectionsCount < collectedPieces.size()) {
                    addNewSegmentToBar(fileSection);
                }
                if (collectedPieces.size() == fileSection.torrent.getPieces().size()) {
                    downloadedTorrents.add(torrentFileName);
                }
            });
        });
    }

    @FXML
    private void onDistributeButtonClick() {
        TorrentClient torrentClient = ApplicationStarter.getTorrentClient();
        assert torrentClient != null;
        File file = chooseFile("Choose a file to distribute");
        if (file == null) {
            return;
        }
        String torrentFilePath = file.getAbsolutePath();
        String torrentFileName = torrentFilePath.substring(torrentFilePath.lastIndexOf(Constants.PATH_DIVIDER) + 1);
        try {
            torrentClient.distribute(torrentFilePath);
        } catch (BadTorrentFileException e) {
            showErrorAlert("Could not upload " + torrentFileName + ": " + e.getMessage());
            return;
        }
        FileSection fileSection = makeNewFileSection(torrentFilePath, Status.DISTRIBUTED);
        showFileSection(fileSection);
        fileSections.put(torrentFileName, fileSection);
    }

    @FXML
    private void onCreateButtonClick() {
        TorrentClient torrentClient = ApplicationStarter.getTorrentClient();
        assert torrentClient != null;
        File file = chooseFile("Choose a file to make its torrent");
        if (file == null) {
            return;
        }
        String filePath = file.getAbsolutePath();
        String fileName = filePath.substring(filePath.lastIndexOf(Constants.PATH_DIVIDER) + 1);
        try {
            torrentClient.createTorrent(filePath);
        } catch (BadTorrentFileException | TorrentCreateFailureException e) {
            showErrorAlert("Could not create a torrent for " + fileName +
                    ": " + e.getMessage());
            return;
        }
        String torrentFileName = fileName + Constants.POSTFIX;
        String torrentFilePath = Constants.TORRENT_PATH + torrentFileName;
        FileSection fileSection = makeNewFileSection(torrentFilePath, Status.DISTRIBUTED);
        showFileSection(fileSection);
        fileSections.put(torrentFileName, fileSection);
    }

    @FXML
    protected void onExitButtonClick() {
        exit();
    }

    public static void exit() {
        TorrentClient torrentClient = ApplicationStarter.getTorrentClient();
        assert torrentClient != null;
        try {
            torrentClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.exit();
    }

    private File chooseFile(String message) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(message);
        Stage stage = (Stage) downloadButton.getScene().getWindow();
        return fileChooser.showOpenDialog(stage);
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void stopResumeHandler(String torrentFileName) {
        if (downloadedTorrents.contains(torrentFileName)) {
            return;
        }
        TorrentClient torrentClient = ApplicationStarter.getTorrentClient();
        ButtonStatus status = fileSections.get(torrentFileName).buttonStatus;
        if (status == ButtonStatus.STOP) {
            try {
                torrentClient.stopDownloading(torrentFileName);
            } catch (BadTorrentFileException e) {
                e.printStackTrace();
                return;
            }
            fileSections.get(torrentFileName).buttonStatus = ButtonStatus.RESUME;
            return;
        }
        try {
            torrentClient.resumeDownloading(torrentFileName);
        } catch (BadTorrentFileException e) {
            e.printStackTrace();
            return;
        }
        fileSections.get(torrentFileName).buttonStatus = ButtonStatus.STOP;
    }
}