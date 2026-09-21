package iwish.client;

import iwish.common.User;
import java.net.URL;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** The JavaFX application: owns the window and switches between the login and main screens. */
public class IWishApp extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        showLogin();
        stage.show();
    }

    @Override
    public void stop() {
        ServerConnection.get().close();
    }

    static void showLogin() {
        stage.setTitle("i-Wish");
        stage.setScene(createScene(new LoginView().getRoot(), 460, 640));
        stage.setMinWidth(0);
        stage.setMinHeight(0);
        stage.centerOnScreen();
    }

    static void showMain(User me) {
        stage.setTitle("i-Wish - " + me.getName());
        stage.setScene(createScene(new MainView(me).getRoot(), 580, 680));
        stage.setMinWidth(500);
        stage.setMinHeight(520);
        stage.centerOnScreen();
    }

    /** A scene with the app stylesheet applied. */
    static Scene createScene(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        URL css = IWishApp.class.getResource("style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        return scene;
    }
}
