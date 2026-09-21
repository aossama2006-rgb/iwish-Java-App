package iwish.server;

import javafx.application.Application;

/** Entry point for the server window (not an Application subclass, so it also runs from the classpath). */
public final class ServerMain {
    private ServerMain() {
    }

    public static void main(String[] args) {
        Application.launch(ServerApp.class, args);
    }
}
