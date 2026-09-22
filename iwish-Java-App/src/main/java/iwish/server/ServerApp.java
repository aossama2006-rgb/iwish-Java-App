package iwish.server;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// The server window: Start / Stop
public class ServerApp extends Application {
    private final IWishServer server = new IWishServer(IWishServer.DEFAULT_PORT);

    private final Label state = new Label();
    private final Label message = new Label();
    private final Button startButton = new Button("Start");
    private final Button stopButton = new Button("Stop");

    @Override
    public void start(Stage stage) {
        Label title = new Label("i-Wish Server");
        title.getStyleClass().add("app-title");
        state.getStyleClass().add("server-state");

        message.setWrapText(true);
        message.managedProperty().bind(message.textProperty().isNotEmpty());
        message.visibleProperty().bind(message.textProperty().isNotEmpty());

        startButton.getStyleClass().add("primary-button");
        stopButton.getStyleClass().add("danger-button");
        startButton.setOnAction(e -> startServer());
        stopButton.setOnAction(e -> stopServer());
        HBox buttons = new HBox(10, startButton, stopButton);
        buttons.setAlignment(Pos.CENTER);
        showStopped();

        VBox card = new VBox(14, title, state, buttons, message);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(360, Region.USE_PREF_SIZE);
        StackPane root = new StackPane(card);
        root.getStyleClass().add("login-root");
        root.setPadding(new Insets(24));

        Scene scene = new Scene(root, 440, 340);
        URL css = ServerApp.class.getResource("/iwish/client/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setTitle("i-Wish Server");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        server.stop();
    }

    // Checks the database, then opens the port, Done off the UI thread because either can be slow
    private void startServer() {
        startButton.setDisable(true);
        setMessage("Connecting to the database...", false);
        Thread worker = new Thread(() -> {
            String error = tryStart();
            Platform.runLater(() -> {
                if (error == null) {
                    showRunning();
                } else {
                    showStopped();
                    setMessage(error, true);
                }
            });
        }, "iwish-start");
        worker.setDaemon(true);
        worker.start();
    }

    private String tryStart() {
        try (Connection ignored = Database.getConnection()) {
            
        } catch (SQLException e) {
            return "Can't connect to the database. Is MySQL/MariaDB running? (" + e.getMessage() + ")";
        }
        try {
            server.start();
        } catch (IOException e) {
            return "Can't open port " + IWishServer.DEFAULT_PORT
                    + ". Is another server already running? (" + e.getMessage() + ")";
        }
        return null;
    }

    private void stopServer() {
        server.stop();
        showStopped();
    }

    private void showRunning() {
        state.setText("Running on port " + IWishServer.DEFAULT_PORT);
        state.getStyleClass().setAll("server-state", "success-label");
        startButton.setDisable(true);
        stopButton.setDisable(false);
        message.setText("");
    }

    private void showStopped() {
        state.setText("Stopped");
        state.getStyleClass().setAll("server-state", "error-label");
        startButton.setDisable(false);
        stopButton.setDisable(true);
        message.setText("");
    }

    private void setMessage(String text, boolean error) {
        message.getStyleClass().setAll(error ? "error-label" : "hint-label");
        message.setText(text);
    }
}
