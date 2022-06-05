package com.aiwannafly.gui_torrent;

import com.aiwannafly.gui_torrent.controller.MainMenuController;
import com.aiwannafly.gui_torrent.torrent.client.BitTorrentClient;
import com.aiwannafly.gui_torrent.torrent.client.TorrentClient;
import com.aiwannafly.gui_torrent.view.Renderer;
import com.aiwannafly.gui_torrent.view.TexturePack;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ApplicationStarter extends Application {
    private static final TorrentClient torrentClient = new BitTorrentClient();

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = Renderer.instance.getScene();
        stage.setTitle("aiTorrent");
        stage.setScene(scene);
        stage.getIcons().add(TexturePack.iconImage);
        stage.setOnCloseRequest(event -> MainMenuController.exit());
        stage.show();
    }

    public static void main(String[] args) {
//        int a = 240;
//        byte[] bytes = ByteBuffer.allocate(4).putInt(a).array();
//        for (int i = 0; i < 4; i++) {
//            System.out.println(Integer.toBinaryString(bytes[i]));
//        }
//        Platform.exit();
//        int b = ByteBuffer.wrap(bytes).getInt();
//        System.out.println(b);
        launch();
    }

    public static TorrentClient getTorrentClient() {
        return torrentClient;
    }

}