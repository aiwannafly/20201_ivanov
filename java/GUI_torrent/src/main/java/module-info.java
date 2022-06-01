module com.aiwannafly.gui_torrent {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.io;

    opens com.aiwannafly.gui_torrent to javafx.fxml;
    exports com.aiwannafly.gui_torrent;
}