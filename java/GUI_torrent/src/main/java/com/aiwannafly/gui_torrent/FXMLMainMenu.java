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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FXMLMainMenu {
    @FXML
    public Button distributeButton;

    @FXML
    public Button createButton;

    @FXML
    private Button downloadButton;

    private int filesCount = 0;
    private final Map<String, FileSection> fileSections = new HashMap<>();
    private static final double STATUS_BAR_LENGTH = 300;

    @FXML
    protected void onDownloadButtonClick() {
        TorrentClient torrentClient = ApplicationStarter.getTorrentClient();
        assert torrentClient != null;
        File file = chooseFile("Choose a file to download");
        if (file == null) {
            return;
        }
        String fileName = file.getName();
        String postfix = Constants.POSTFIX;
        int originalFileLength = fileName.length() - postfix.length();
        String originalFileName;
        if (originalFileLength <= 0) {
            originalFileName = "[torrent]";
        } else {
            originalFileName = Constants.PREFIX + fileName.substring(0, originalFileLength);
        }
        try {
            torrentClient.download(fileName);
            if (fileName.length() <= postfix.length()) {
                System.err.println("Bad file name, it should end with " + postfix);
            }
        } catch (BadTorrentFileException | BadServerReplyException |
                NoSeedsException | ServerNotCorrespondsException e) {
            System.err.println("Could not download " + originalFileName
                    + ": " + e.getMessage());
            return;
        }
        FileSection fileSection = makeNewFileSection(fileName, Status.DISTRIBUTED);
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
                addNewSegmentToBar(fileSection);
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
        String torrentFileName = file.getName();
        try {
            torrentClient.distribute(torrentFileName);
            System.out.println("Torrent file " + torrentFileName + " is distributed");
        } catch (BadTorrentFileException e) {
            System.err.println("Could not upload " + torrentFileName + ": " + e.getMessage());
        }
        FileSection fileSection = makeNewFileSection(torrentFileName, Status.DISTRIBUTED);
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
        String fileName = file.getName();
        System.out.println("Filename: " + fileName);
        try {
            torrentClient.createTorrent(fileName);
            System.out.println("Torrent file " + fileName + ".torrent was created successfully. " +
                    "It is distributed for others clients.");
        } catch (BadTorrentFileException | TorrentCreateFailureException e) {
            System.err.println("Could not create a torrent for " + fileName +
                    ": " + e.getMessage());
        }
        String torrentFileName = fileName + Constants.POSTFIX;
        FileSection fileSection = makeNewFileSection(torrentFileName, Status.DISTRIBUTED);
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

    private FileSection makeNewFileSection(String fileName, Status status) {
        FileSection fileSection = new FileSection();
        double width = 400;
        double height = 50;
        fileSection.status = status;
        fileSection.x = 200;
        fileSection.y = height * (++filesCount);
        fileSection.square = new Rectangle(fileSection.x, fileSection.y, width, height);
        fileSection.square.setFill(Color.BLACK);
        fileSection.nameLabel = new Label(fileName);
        fileSection.nameLabel.setLayoutX(fileSection.x + 10);
        fileSection.nameLabel.setLayoutY(fileSection.y + 5);
        fileSection.nameLabel.setMaxWidth(100);
        String statusName = null;
        switch (fileSection.status) {
            case DISTRIBUTED -> statusName = "DISTRIBUTED";
            case DOWNLOADING -> statusName = "DOWNLOADING";
        }
        fileSection.statusLabel = new Label(statusName);
        fileSection.statusLabel.setLayoutX(fileSection.x + 10 + 100);
        fileSection.statusLabel.setLayoutY(fileSection.y + 5);
        fileSection.statusLabel.setMaxWidth(100);
        Torrent torrentFile;
        try {
            torrentFile = TorrentParser.parseTorrent(Constants.PATH + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return fileSection;
        }
        fileSection.torrent = torrentFile;
        return fileSection;
    }

    private void showFileSection(FileSection fileSection) {
        Pane pane = ApplicationStarter.getButtonsPane();
        pane.getChildren().add(fileSection.square);
        pane.getChildren().add(fileSection.statusLabel);
        pane.getChildren().add(fileSection.nameLabel);
    }

    private void addNewSegmentToBar(FileSection fileSection) {
        double width = STATUS_BAR_LENGTH / fileSection.torrent.getPieces().size();
        double height = 20;
        double y = fileSection.y + 30;
        double x = fileSection.x + ++fileSection.sectionsCount * width;
        Rectangle segment = new Rectangle(x, y, width, height);
        segment.setFill(Color.LIMEGREEN);
        Pane pane = ApplicationStarter.getButtonsPane();
        pane.getChildren().add(segment);
    }

    enum Status {
        DISTRIBUTED, DOWNLOADING
    }

    private static class FileSection {
        public String fileName;
        public Status status;
        public Label nameLabel;
        public Label statusLabel;
        public Rectangle square;
        public double x;
        public double y;
        private Torrent torrent;
        private int sectionsCount = 0;
    }
}