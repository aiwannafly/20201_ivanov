package com.aiwannafly.gui_torrent;

import com.aiwannafly.gui_torrent.torrent.client.BitTorrentClient;
import com.aiwannafly.gui_torrent.torrent.client.TorrentClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ApplicationStarter extends Application {
    private static TorrentClient torrentClient;
    private static final String STYLESHEET = Objects.requireNonNull(ApplicationStarter.class.getResource(
            "styles.css")).toExternalForm();
    private static Pane buttonsPane;

    @Override
    public void start(Stage stage) throws IOException {
        torrentClient = new BitTorrentClient();
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationStarter.class.getResource("fxml/main-menu.fxml"));
        buttonsPane = new Pane();
        buttonsPane.getChildren().add(fxmlLoader.load());
        Scene scene = new Scene(buttonsPane, 640, 480);
        stage.setTitle("aiTorrent");
        scene.getStylesheets().add(STYLESHEET);
        stage.setScene(scene);
        stage.show();
    }

    public static TorrentClient getTorrentClient() {
        return torrentClient;
    }

    public static Pane getButtonsPane() {
        return buttonsPane;
    }

    public static void main(String[] args) {
        launch();
    }

}