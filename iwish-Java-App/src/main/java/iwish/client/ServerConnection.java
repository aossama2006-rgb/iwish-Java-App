package iwish.client;

import iwish.common.Request;
import iwish.common.Response;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javafx.application.Platform;


 // Requests run one at a time on a background thread, callbacks run on the JavaFX thread
public final class ServerConnection {
    private static final ServerConnection INSTANCE = new ServerConnection();

    private final String host = System.getProperty("iwish.host", "localhost");
    private final int port = Integer.getInteger("iwish.port", 5555);
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "iwish-network");
        t.setDaemon(true);
        return t;
    });

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private ServerConnection() {
    }

    public static ServerConnection get() {
        return INSTANCE;
    }

    private void connect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            return;
        }
        Socket s = new Socket();
        s.connect(new InetSocketAddress(host, port), 3000);
        out = new ObjectOutputStream(s.getOutputStream());
        out.flush();
        in = new ObjectInputStream(s.getInputStream());
        socket = s;
    }

    public synchronized void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
            // nothing to do
        }
        socket = null;
    }

    private synchronized Response send(Request request) throws IOException, ClassNotFoundException {
        connect();
        try {
            out.writeObject(request);
            out.flush();
            out.reset();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            close();
            throw e;
        }
    }

    // Sends the request in the background and calls onDone on the thread
    public void sendAsync(Request request, Consumer<Response> onDone) {
        worker.execute(() -> {
            Response response;
            try {
                response = send(request);
            } catch (IOException | ClassNotFoundException e) {
                response = Response.error("Can't reach the server. Is it running?");
            } catch (RuntimeException e) {
                response = Response.error("Unexpected error. Please try again.");
            }
            Response result = response;
            Platform.runLater(() -> onDone.accept(result));
        });
    }
}
