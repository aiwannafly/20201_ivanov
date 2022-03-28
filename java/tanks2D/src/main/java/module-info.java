module com.games.tanks_2d {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires javafx.media;

    opens com.games.tanks2d to javafx.fxml;
    exports com.games.tanks2d;
}