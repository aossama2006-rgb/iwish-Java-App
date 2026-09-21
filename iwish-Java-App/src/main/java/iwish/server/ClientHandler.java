package iwish.server;

import iwish.common.Action;
import iwish.common.Item;
import iwish.common.Request;
import iwish.common.Response;
import iwish.common.User;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/** Serves one connected client. The signed-in user lives here, for the life of the connection. */
class ClientHandler implements Runnable {
    private static final Logger LOG = Logger.getLogger(ClientHandler.class.getName());
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final Socket socket;
    private final Runnable onClose;
    private final UserDAO users = new UserDAO();
    private final FriendDAO friends = new FriendDAO();
    private final ItemDAO items = new ItemDAO();
    private final WishlistDAO wishlists = new WishlistDAO();
    private final ContributionDAO contributions = new ContributionDAO();
    private final NotificationDAO notifications = new NotificationDAO();
    private User currentUser;

    ClientHandler(Socket socket, Runnable onClose) {
        this.socket = socket;
        this.onClose = onClose;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            // Only accept our own protocol classes and basic JDK types from clients.
            in.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
                    "iwish.common.*;java.lang.*;java.util.*;!*"));
            while (true) {
                Request request = (Request) in.readObject();
                out.writeObject(handle(request));
                out.flush();
                out.reset();
            }
        } catch (EOFException | SocketException e) {
            // client disconnected
        } catch (IOException | ClassNotFoundException | RuntimeException e) {
            LOG.log(Level.WARNING, "Connection error", e);
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
                // nothing to do
            }
            onClose.run();
        }
    }

    private Response handle(Request req) {
        try {
            Action action = req.getAction();
            switch (action) {
                case REGISTER:
                    return register(req);
                case LOGIN:
                    return login(req);
                default:
                    break;
            }
            if (currentUser == null) {
                return Response.sessionExpired();
            }
            switch (action) {
                case LOGOUT:
                    currentUser = null;
                    return Response.ok("Signed out.");
                case ADD_FRIEND:
                    return addFriend(req);
                case REMOVE_FRIEND:
                    friends.removeFriend(currentUser.getId(), req.getInt("friendId"));
                    return Response.ok("Friend removed.");
                case GET_FRIENDS:
                    return Response.ok("OK", friends.getFriends(currentUser.getId()));
                case GET_INCOMING_REQUESTS:
                    return Response.ok("OK", friends.getIncomingRequests(currentUser.getId()));
                case ACCEPT_REQUEST:
                    User friend = friends.acceptRequest(currentUser.getId(), req.getInt("requestId"));
                    return Response.ok("You and " + friend.getName() + " are now friends!", friend);
                case DECLINE_REQUEST:
                    friends.declineRequest(currentUser.getId(), req.getInt("requestId"));
                    return Response.ok("Request declined.");
                case GET_CATALOG:
                    return Response.ok("OK", items.getCatalog());
                case GET_MY_WISHLIST:
                    return Response.ok("OK", wishlists.getWishlist(currentUser.getId(), true));
                case ADD_TO_WISHLIST: {
                    Item added = wishlists.add(currentUser.getId(), req.getInt("itemId"),
                            req.getString("note"));
                    return Response.ok(added.getName() + " was added to your wish list.");
                }
                case UPDATE_WISHLIST_ITEM:
                    wishlists.updateNote(currentUser.getId(), req.getInt("wishId"), req.getString("note"));
                    return Response.ok("Note updated.");
                case REMOVE_FROM_WISHLIST:
                    wishlists.remove(currentUser.getId(), req.getInt("wishId"));
                    return Response.ok("Removed from your wish list.");
                case GET_FRIEND_WISHLIST:
                    return Response.ok("OK",
                            wishlists.getFriendWishlist(currentUser.getId(), req.getInt("friendId")));
                case CONTRIBUTE:
                    return Response.ok(contributions.contribute(currentUser.getId(),
                            req.getInt("wishId"), req.getString("amount")));
                case GET_NOTIFICATIONS:
                    return Response.ok("OK", notifications.getRecent(currentUser.getId()));
                case MARK_NOTIFICATIONS_READ:
                    notifications.markAllRead(currentUser.getId());
                    return Response.ok("OK");
                default:
                    return Response.error("Unknown request.");
            }
        } catch (BusinessException e) {
            return Response.error(e.getMessage());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Database error", e);
            return Response.error("Something went wrong on the server. Please try again.");
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Bad request", e);
            return Response.error("Bad request.");
        }
    }

    private Response register(Request req) throws SQLException, BusinessException {
        String name = clean(req.getString("name"));
        String email = clean(req.getString("email")).toLowerCase(Locale.ROOT);
        String password = req.getString("password");

        if (name.isEmpty() || name.length() > 50) {
            throw new BusinessException("Please enter a name (up to 50 characters).");
        }
        if (!EMAIL.matcher(email).matches() || email.length() > 100) {
            throw new BusinessException("Please enter a valid email address.");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("Password must be at least 6 characters.");
        }
        currentUser = users.register(name, email, password);
        return Response.ok("Welcome to i-Wish, " + name + "!", currentUser);
    }

    private Response login(Request req) throws SQLException, BusinessException {
        String email = clean(req.getString("email")).toLowerCase(Locale.ROOT);
        String password = req.getString("password");
        if (email.isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessException("Enter your email and password.");
        }
        currentUser = users.authenticate(email, password);
        return Response.ok("Welcome back, " + currentUser.getName() + "!", currentUser);
    }

    private Response addFriend(Request req) throws SQLException, BusinessException {
        String email = clean(req.getString("email")).toLowerCase(Locale.ROOT);
        if (email.isEmpty()) {
            throw new BusinessException("Enter your friend's email.");
        }
        User target = friends.sendRequest(currentUser.getId(), email);
        return Response.ok("Friend request sent to " + target.getName() + ".");
    }

    private static String clean(String s) {
        return s == null ? "" : s.trim();
    }
}
