package com.aiwannafly.gui_torrent;

import com.aiwannafly.gui_torrent.torrent.Constants;
import com.aiwannafly.gui_torrent.torrent.client.TorrentClient;
import com.aiwannafly.gui_torrent.torrent.client.exceptions.*;
import com.aiwannafly.gui_torrent.torrent.client.util.torrent.Torrent;
import com.aiwannafly.gui_torrent.torrent.client.util.torrent.TorrentParser;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.aiwannafly.gui_torrent.ApplicationStarter.*;

public class FXMLMainMenu {
    @FXML
    public Button distributeButton;

    @FXML
    public Button createButton;

    @FXML
    private Button downloadButton;

    private int filesCount = 0;
    private final Map<String, FileSection> fileSections = new HashMap<>();

    @FXML
    protected void onDownloadButtonClick() {
        TorrentClient torrentClient = ApplicationStarter.getTorrentClient();
        assert torrentClient != null;
        File file = chooseFile("Choose a file to download");
        if (file == null) {
            return;
        }
        String filePath = file.getAbsolutePath();
        String postfix = Constants.POSTFIX;
        String fileName = filePath.substring(filePath.lastIndexOf(Constants.PATH_DIVIDER) + 1);
        int originalFileLength = fileName.length() - postfix.length();
        String originalFileName;
        if (originalFileLength <= 0) {
            originalFileName = Constants.PREFIX;
        } else {
            originalFileName = Constants.PREFIX + filePath.substring(0, originalFileLength);
        }
        if (fileName.length() <= postfix.length()) {
            showErrorAlert("Bad file name, it should end with " + postfix);
            return;
        }
        try {
            torrentClient.download(filePath);
        } catch (BadTorrentFileException | BadServerReplyException |
                NoSeedsException | ServerNotCorrespondsException e) {
            showErrorAlert("Could not download " + originalFileName
                    + ": " + e.getMessage());
            return;
        }
        FileSection fileSection = makeNewFileSection(filePath, Status.DOWNLOADING);
        showFileSection(fileSection);
        fileSections.put(fileName, fileSection);
        ObservableList<Integer> collectedPieces;
        try {
            collectedPieces = torrentClient.getCollectedPieces(fileName);
        } catch (BadTorrentFileException e) {
            e.printStackTrace();
            return;
        }
        collectedPieces.addListener((ListChangeListener<? super Integer>) change -> {
            Platform.runLater(() -> {
                while (fileSection.sectionsCount < collectedPieces.size()) {
                    addNewSegmentToBar(fileSection);
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

    private FileSection makeNewFileSection(String torrentFilePath, Status status) {
        FileSection fileSection = new FileSection();
        String torrentFileName = torrentFilePath.substring(torrentFilePath.lastIndexOf(Constants.PATH_DIVIDER) + 1);
        Torrent torrentFile;
        try {
            torrentFile = TorrentParser.parseTorrent(torrentFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            return fileSection;
        }
        fileSection.status = status;
        fileSection.x = 160 + 20;
        fileSection.y = 10 + ApplicationStarter.LABEL_HEIGHT * (++filesCount);
        fileSection.labels = new ArrayList<>();
        Label numLabel = new Label(String.valueOf(filesCount));
        numLabel.setPrefWidth(NUM_FIELD_LENGTH);
        Label nameLabel = new Label(torrentFileName);
        nameLabel.setPrefWidth(NAME_FIELD_LENGTH);
        Label sizeLabel = new Label(String.valueOf(torrentFile.getTotalSize() / 1024));
        sizeLabel.setPrefWidth(SIZE_FIELD_LENGTH);
        String statusStr = null;
        switch (status) {
            case DOWNLOADING -> statusStr = "downloading";
            case DISTRIBUTED -> statusStr = "distributed";
        }
        Label barLabel = new Label(statusStr);
        barLabel.setPrefWidth(BAR_FIELD_LENGTH);
        fileSection.labels.add(numLabel);
        fileSection.labels.add(nameLabel);
        fileSection.labels.add(sizeLabel);
        fileSection.labels.add(barLabel);
        fileSection.torrent = torrentFile;
        for (Label label: fileSection.labels) {
            label.setPrefHeight(LABEL_HEIGHT);
            label.setLayoutY(fileSection.y);
            label.setLayoutX(fileSection.x);
            label.setAlignment(Pos.CENTER);
            fileSection.x += label.getPrefWidth();
        }
        return fileSection;
    }

    private void showFileSection(FileSection fileSection) {
        Pane pane = ApplicationStarter.getRootPane();
        for (Label label: fileSection.labels) {
            pane.getChildren().add(label);
        }
    }

    private void addNewSegmentToBar(FileSection fileSection) {
        double width = BAR_FIELD_LENGTH / fileSection.torrent.getPieces().size();
        double height = 10;
        double y = fileSection.y + 10;
        double offset = 160 + 20 + NUM_FIELD_LENGTH + NAME_FIELD_LENGTH + SIZE_FIELD_LENGTH;
        double x = offset + ++fileSection.sectionsCount * width;
        Rectangle segment = new Rectangle(x, y, width, height);
        Color limeGreen = new Color(36.0 / 255, 1, 0, 1);
        segment.setFill(limeGreen);
        Pane pane = ApplicationStarter.getRootPane();
        pane.getChildren().add(segment);
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    enum Status {
        DISTRIBUTED, DOWNLOADING
    }

    private static class FileSection {
        public String fileName;
        public Status status;
        public Label numberLabel;
        public Label nameLabel;
        public Label sizeLabel;
        public Label statusLabel;
        public ArrayList<Label> labels;
        public double x;
        public double y;
        private Torrent torrent;
        private int sectionsCount = 0;
    }
}