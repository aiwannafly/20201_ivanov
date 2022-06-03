package com.aiwannafly.gui_torrent.view;

import com.aiwannafly.gui_torrent.ApplicationStarter;
import com.aiwannafly.gui_torrent.TrackerServer;
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

public class Renderer implements GUITorrentRenderer {
    private static final double NUM_FIELD_LENGTH = 30;
    private static final double NAME_FIELD_LENGTH = 320;
    private static final double SIZE_FIELD_LENGTH = 70;
    private static final double STATUS_FIELD_LENGTH = 100;
    private static final double BAR_FIELD_LENGTH = 400;
    private static final double LABEL_HEIGHT = 30;
    private static final double SECTION_LENGTH = NUM_FIELD_LENGTH +
            NAME_FIELD_LENGTH + SIZE_FIELD_LENGTH + STATUS_FIELD_LENGTH +
            BAR_FIELD_LENGTH;
    private static final String STYLESHEET = Objects.requireNonNull(TrackerServer.class.getResource(
            "styles.css")).toExternalForm();
    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 480;
    private static final double TOP_OFFSET = 10;
    private static final double LEFT_OFFSET = 20;
    private static final double MENU_LENGTH = 160;
    private Pane rootPane;
    private Pane buttonsPane;
    private int filesCount = 0;
    public static final Renderer instance = new Renderer();

    public Scene getScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationStarter.class.getResource("fxml/main-menu.fxml"));
        rootPane = new Pane();
        Background b = new Background(new BackgroundImage(TexturePack.backgroundImage, BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT, null, null));
        rootPane.setBackground(b);
        buttonsPane = fxmlLoader.load();
        rootPane.getChildren().add(buttonsPane);
        Scene scene = new Scene(rootPane, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(STYLESHEET);
        scene.setFill(Color.BLACK);
        showHeaders();
        return scene;
    }

    @Override
    public FileSection createFileSection(String torrentFilePath, Status status) {
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
        fileSection.x = MENU_LENGTH + LEFT_OFFSET;
        fileSection.y = TOP_OFFSET + LABEL_HEIGHT * (++filesCount);
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
        Label statusLabel = new Label(statusStr);
        statusLabel.setPrefWidth(STATUS_FIELD_LENGTH);
        Label barLabel = new Label();
        barLabel.setPrefWidth(BAR_FIELD_LENGTH);
        fileSection.labels.add(numLabel);
        fileSection.labels.add(nameLabel);
        fileSection.labels.add(sizeLabel);
        fileSection.labels.add(statusLabel);
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
        Rectangle image = makeIcon();
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

    @Override
    public void renderFileSection(FileSection fileSection) {
        for (Label label : fileSection.labels) {
            rootPane.getChildren().add(label);
        }
        if (fileSection.stopResumeButton == null) {
            return;
        }
        rootPane.getChildren().add(fileSection.stopResumeButton);
    }

    @Override
    public void renderNewSegmentBar(FileSection fileSection) {
        double width = BAR_FIELD_LENGTH / fileSection.torrent.getPieces().size();
        double y = fileSection.y + TOP_OFFSET;
        double offset = 160 + 20 + NUM_FIELD_LENGTH + NAME_FIELD_LENGTH + SIZE_FIELD_LENGTH +
                STATUS_FIELD_LENGTH;
        double x = offset + fileSection.sectionsCount++ * width;
        Rectangle segment = new Rectangle(x, y, width, TOP_OFFSET);
        Color limeGreen = new Color(36.0 / 255, 1, 0, 1);
        segment.setFill(limeGreen);
        rootPane.getChildren().add(segment);
    }

    private void showHeaders() {
        ArrayList<Label> allLabels = new ArrayList<>();
        Label numLabel = new Label("№");
        numLabel.setPrefWidth(NUM_FIELD_LENGTH);
        Label nameLabel = new Label("File name");
        nameLabel.setPrefWidth(NAME_FIELD_LENGTH);
        Label sizeLabel = new Label("Size");
        sizeLabel.setPrefWidth(SIZE_FIELD_LENGTH);
        Label statusLabel = new Label("Status");
        statusLabel.setPrefWidth(STATUS_FIELD_LENGTH);
        Label barLabel = new Label("Status bar");
        barLabel.setPrefWidth(BAR_FIELD_LENGTH);
        Label buttonLabel = new Label();
        buttonLabel.setPrefWidth(LABEL_HEIGHT);
        allLabels.add(numLabel);
        allLabels.add(nameLabel);
        allLabels.add(sizeLabel);
        allLabels.add(statusLabel);
        allLabels.add(barLabel);
        allLabels.add(buttonLabel);
        double x = MENU_LENGTH + LEFT_OFFSET;
        Label borderLabel = new Label();
        borderLabel.setLayoutX(x);
        borderLabel.setLayoutY(TOP_OFFSET);
        borderLabel.setPrefHeight(SECTION_LENGTH);
        rootPane.getChildren().add(borderLabel);
        for (Label label : allLabels) {
            label.setPrefHeight(LABEL_HEIGHT);
            label.setLayoutY(TOP_OFFSET);
            label.setLayoutX(x);
            label.setAlignment(Pos.CENTER);
            x += label.getPrefWidth();
            rootPane.getChildren().add(label);
        }
        borderLabel.setPrefWidth(x - (MENU_LENGTH + LEFT_OFFSET));
    }

    private static Rectangle makeIcon() {
        Rectangle icon = new Rectangle();
        icon.setWidth(LABEL_HEIGHT / 3.5);
        icon.setHeight(LABEL_HEIGHT / 3.5);
        return icon;
    }
}
