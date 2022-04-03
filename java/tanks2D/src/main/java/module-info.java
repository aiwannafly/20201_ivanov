module com.games.tanks_2d {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires javafx.media;

    opens com.games.starwars to javafx.fxml;
    exports com.games.starwars;
    exports com.games.starwars.model.factory;
    opens com.games.starwars.model.factory to javafx.fxml;
}