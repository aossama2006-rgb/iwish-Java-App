package iwish.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Accepts client connections. start()/stop() are kept separate so a GUI can drive them later. */
public class IWishServer {
    public static final int DEFAULT_PORT = 5555;
    private static final Logger LOG = Logger.getLogger(IWishServer.class.getName());

    private final int port;
    private final Set<Socket> clients = ConcurrentHashMap.newKeySet();
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private volatile boolean running;

    public IWishServer(int port) {
        this.port = port;
    }

    public synchronized void start() throws IOException {
        if (running) {
            return;
        }
        ServerSocket listening = new ServerSocket(port);
        ExecutorService workers = Executors.newCachedThreadPool();
        serverSocket = listening;
        pool = workers;
        running = true;
        Thread acceptor = new Thread(() -> acceptLoop(listening, workers), "iwish-acceptor");
        acceptor.start();
        LOG.info("i-Wish server listening on port " + port);
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        try {
            serverSocket.close();
        } catch (IOException ignored) {
            // already closing
        }
        for (Socket s : clients) {
            try {
                s.close();
            } catch (IOException ignored) {
                // already closed
            }
        }
        pool.shutdownNow();
        LOG.info("i-Wish server stopped");
    }

    public boolean isRunning() {
        return running;
    }

    private void acceptLoop(ServerSocket listening, ExecutorService workers) {
        while (!listening.isClosed()) {
            try {
                Socket socket = listening.accept();
                clients.add(socket);
                workers.execute(new ClientHandler(socket, () -> clients.remove(socket)));
            } catch (IOException e) {
                if (!listening.isClosed()) {
                    LOG.log(Level.WARNING, "Accept failed", e);
                }
            }
        }
    }
}
