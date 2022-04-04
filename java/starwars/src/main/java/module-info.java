module com.games.tanks_2d {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires javafx.media;

    opens com.games.starwars to javafx.fxml;
    exports com.games.starwars;
    exports com.games.starwars.factory;
    opens com.games.starwars.factory to javafx.fxml;
    exports com.games.starwars.FXML;
    opens com.games.starwars.FXML to javafx.fxml;
}