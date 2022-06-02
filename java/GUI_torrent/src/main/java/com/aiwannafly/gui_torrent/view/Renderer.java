package com.aiwannafly.gui_torrent.view;

import com.aiwannafly.gui_torrent.ApplicationStarter;
import com.aiwannafly.gui_torrent.controller.MainMenuController;
import com.aiwannafly.gui_torrent.torrent.Constants;
import com.aiwannafly.gui_torrent.torrent.client.util.torrent.Torrent;
import com.aiwannafly.gui_torrent.torrent.client.util.torrent.TorrentParser;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Renderer {
    public static final double NUM_FIELD_LENGTH = 30;
    public static final double NAME_FIELD_LENGTH = 120 + 200;
    public static final double SIZE_FIELD_LENGTH = 70;
    public static final double BAR_FIELD_LENGTH = 600 - 200;
    public static final double LABEL_HEIGHT = 30;
    private static final String STYLESHEET = Objects.requireNonNull(ApplicationStarter.class.getResource(
            "styles.css")).toExternalForm();
    private static Pane rootPane;
    private static int filesCount = 0;

    public enum Status {
        DISTRIBUTED, DOWNLOADING
    }

    public enum ButtonStatus {
        STOP, RESUME
    }

    public static class FileSection {
        public String fileName;
        public Status status;
        public Button stopResumeButton;
        public ButtonStatus buttonStatus;
        public ArrayList<Label> labels;
        public double x;
        public double y;
        public Torrent torrent;
        public int sectionsCount = 0;
    }

    public static Scene getScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationStarter.class.getResource("fxml/main-menu.fxml"));
        rootPane = new Pane();
        Background b = new Background(new BackgroundImage(TexturePack.backgroundImage, BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT, null, null));
        rootPane.setBackground(b);
        rootPane.getChildren().add(fxmlLoader.load());
        Scene scene = new Scene(rootPane, 1080, 480);
        scene.getStylesheets().add(STYLESHEET);
        scene.setFill(Color.BLACK);
        showHeaders();
        return scene;
    }

    public static void showHeaders() {
        ArrayList<Label> allLabels = new ArrayList<>();
        Label numLabel = new Label("№");
        numLabel.setPrefWidth(NUM_FIELD_LENGTH);
        Label nameLabel = new Label("File name");
        nameLabel.setPrefWidth(NAME_FIELD_LENGTH);
        Label sizeLabel = new Label("Size");
        sizeLabel.setPrefWidth(SIZE_FIELD_LENGTH);
        Label barLabel = new Label("Status bar");
        barLabel.setPrefWidth(BAR_FIELD_LENGTH);
        Label buttonLabel = new Label();
        buttonLabel.setPrefWidth(LABEL_HEIGHT);
        allLabels.add(numLabel);
        allLabels.add(nameLabel);
        allLabels.add(sizeLabel);
        allLabels.add(barLabel);
        allLabels.add(buttonLabel);
        double y = 10;
        double x = 160 + 20;
        Label borderLabel = new Label();
        borderLabel.setLayoutX(x);
        borderLabel.setLayoutY(y);
        borderLabel.setPrefHeight(1080);
        rootPane.getChildren().add(borderLabel);
        for (Label label : allLabels) {
            label.setPrefHeight(LABEL_HEIGHT);
            label.setLayoutY(y);
            label.setLayoutX(x);
            label.setAlignment(Pos.CENTER);
            x += label.getPrefWidth();
            rootPane.getChildren().add(label);
        }
        borderLabel.setPrefWidth(x - (160 + 20));
    }

    public static FileSection makeNewFileSection(String torrentFilePath, Status status) {
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
        fileSection.y = 10 + LABEL_HEIGHT * (++filesCount);
        fileSection.labels = new ArrayList<>();
        Label numLabel = new Label(String.valueOf(filesCount));
        numLabel.setPrefWidth(NUM_FIELD_LENGTH);
        Label nameLabel = new Label(torrentFileName);
        nameLabel.setPrefWidth(NAME_FIELD_LENGTH);
        Label sizeLabel = new Label(torrentFile.getTotalSize() / 1024 + " kB");
        sizeLabel.setPrefWidth(SIZE_FIELD_LENGTH);
        String statusStr = null;
        switch (status) {
            case DOWNLOADING -> statusStr = "downloading";
            case DISTRIBUTED -> statusStr = "distributing";
        }
        Label barLabel = new Label(statusStr);
        barLabel.setPrefWidth(BAR_FIELD_LENGTH);
        fileSection.labels.add(numLabel);
        fileSection.labels.add(nameLabel);
        fileSection.labels.add(sizeLabel);
        fileSection.labels.add(barLabel);
        fileSection.torrent = torrentFile;
        for (Label label : fileSection.labels) {
            label.setPrefHeight(LABEL_HEIGHT);
            label.setLayoutY(fileSection.y);
            label.setLayoutX(fileSection.x);
            label.setAlignment(Pos.CENTER);
            fileSection.x += label.getPrefWidth();
        }
        fileSection.buttonStatus = ButtonStatus.STOP;
        fileSection.stopResumeButton = new Button();
        fileSection.stopResumeButton.setLayoutY(fileSection.y);
        fileSection.stopResumeButton.setLayoutX(fileSection.x);
        fileSection.stopResumeButton.setPrefWidth(LABEL_HEIGHT);
        fileSection.stopResumeButton.setPrefHeight(LABEL_HEIGHT);
        if (status == Status.DISTRIBUTED) {
            return fileSection;
        }
        Rectangle image = new Rectangle();
        image.setWidth(LABEL_HEIGHT / 3);
        image.setHeight(LABEL_HEIGHT / 3);
        image.setFill(TexturePack.stopButton);
        fileSection.stopResumeButton.setGraphic(image);
        fileSection.stopResumeButton.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            MainMenuController.stopResumeHandler(torrentFileName);
            Rectangle icon = makeIcon();
            if (fileSection.buttonStatus == ButtonStatus.RESUME) {
                icon.setFill(TexturePack.resumeBlackButton);
            } else {
                icon.setFill(TexturePack.stopBlackButton);
            }
            fileSection.stopResumeButton.setGraphic(icon);
        });
        fileSection.stopResumeButton.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            Rectangle icon = makeIcon();
            if (fileSection.buttonStatus == ButtonStatus.RESUME) {
                icon.setFill(TexturePack.resumeBlackButton);
            } else {
                icon.setFill(TexturePack.stopBlackButton);
            }
            fileSection.stopResumeButton.setGraphic(icon);
        });
        fileSection.stopResumeButton.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            Rectangle icon = makeIcon();
            if (fileSection.buttonStatus == ButtonStatus.RESUME) {
                icon.setFill(TexturePack.resumeButton);
            } else {
                icon.setFill(TexturePack.stopButton);
            }
            fileSection.stopResumeButton.setGraphic(icon);
        });
        return fileSection;
    }

    public static void showFileSection(FileSection fileSection) {
        for (Label label : fileSection.labels) {
            rootPane.getChildren().add(label);
        }
        if (fileSection.stopResumeButton == null) {
            return;
        }
        rootPane.getChildren().add(fileSection.stopResumeButton);
    }

    public static void addNewSegmentToBar(FileSection fileSection) {
        double width = BAR_FIELD_LENGTH / fileSection.torrent.getPieces().size();
        double height = 10;
        double y = fileSection.y + 10;
        double offset = 160 + 20 + NUM_FIELD_LENGTH + NAME_FIELD_LENGTH + SIZE_FIELD_LENGTH;
        double x = offset + fileSection.sectionsCount++ * width;
        Rectangle segment = new Rectangle(x, y, width, height);
        Color limeGreen = new Color(36.0 / 255, 1, 0, 1);
        segment.setFill(limeGreen);
        rootPane.getChildren().add(segment);
    }

    private static Rectangle makeIcon() {
        Rectangle icon = new Rectangle();
        icon.setWidth(LABEL_HEIGHT / 3);
        icon.setHeight(LABEL_HEIGHT / 3);
        return icon;
    }
}
