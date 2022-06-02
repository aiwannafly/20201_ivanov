package com.aiwannafly.gui_torrent;

import com.aiwannafly.gui_torrent.torrent.client.BitTorrentClient;
import com.aiwannafly.gui_torrent.torrent.client.TorrentClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ApplicationStarter extends Application {
    private static TorrentClient torrentClient;
    private static final String STYLESHEET = Objects.requireNonNull(ApplicationStarter.class.getResource(
            "styles.css")).toExternalForm();
    private static Pane rootPane;

    @Override
    public void start(Stage stage) throws IOException {
        torrentClient = new BitTorrentClient();
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationStarter.class.getResource("fxml/main-menu.fxml"));
        rootPane = new Pane();
        Background b = new Background(new BackgroundImage(TexturePack.backgroundImage, BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT, null, null));
        rootPane.setBackground(b);
        rootPane.getChildren().add(fxmlLoader.load());
        Scene scene = new Scene(rootPane, 1080, 480);
        stage.setTitle("aiTorrent");
        scene.getStylesheets().add(STYLESHEET);
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        showHeaders();
        stage.show();
    }

    public static TorrentClient getTorrentClient() {
        return torrentClient;
    }

    public static Pane getRootPane() {
        return rootPane;
    }

    public static void main(String[] args) {
        launch();
    }

    private static void showHeaders() {
        ArrayList<Label> allLabels = new ArrayList<>();
        Label numLabel = new Label("№");
        numLabel.setPrefWidth(NUM_FIELD_LENGTH);
        Label nameLabel = new Label("File name");
        nameLabel.setPrefWidth(NAME_FIELD_LENGTH);
        Label sizeLabel = new Label("Size");
        sizeLabel.setPrefWidth(SIZE_FIELD_LENGTH);
        Label barLabel = new Label("Status bar");
        barLabel.setPrefWidth(BAR_FIELD_LENGTH);
        allLabels.add(numLabel);
        allLabels.add(nameLabel);
        allLabels.add(sizeLabel);
        allLabels.add(barLabel);
        double y = 10;
        double x = 160 + 20;
        for (Label label: allLabels) {
            label.setPrefHeight(LABEL_HEIGHT);
            label.setLayoutY(y);
            label.setLayoutX(x);
            label.setAlignment(Pos.CENTER);
            x += label.getPrefWidth();
            rootPane.getChildren().add(label);
        }
    }
    public static final double NUM_FIELD_LENGTH = 30;
    public static final double NAME_FIELD_LENGTH = 120;
    public static final double SIZE_FIELD_LENGTH = 70;
    public static final double BAR_FIELD_LENGTH = 600;
    public static final double LABEL_HEIGHT = 30;
}