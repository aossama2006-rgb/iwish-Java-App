package iwish.client;

import javafx.application.Application;

/**
 * Entry point. It deliberately does NOT extend Application: launching an Application
 * subclass directly from the classpath fails with "JavaFX runtime components are missing".
 */
public final class ClientMain {
    private ClientMain() {
    }

    public static void main(String[] args) {
        Application.launch(IWishApp.class, args);
    }
}
