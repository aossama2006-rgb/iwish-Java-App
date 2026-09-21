package iwish.server;

import iwish.common.Notification;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class NotificationDAO {
    private static final int MAX_LENGTH = 400;
    private static final int RECENT_LIMIT = 50;

    /** Adds a notification using the caller's connection, so it commits (or rolls back) with the caller. */
    public void add(Connection c, int userId, String message) throws SQLException {
        String text = message.length() > MAX_LENGTH ? message.substring(0, MAX_LENGTH - 3) + "..." : message;
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO notifications (user_id, message) VALUES (?, ?)")) {
            ps.setInt(1, userId);
            ps.setString(2, text);
            ps.executeUpdate();
        }
    }

    /** The user's most recent notifications, newest first. */
    public ArrayList<Notification> getRecent(int userId) throws SQLException {
        String sql = "SELECT id, message, is_read, created_at FROM notifications "
                + "WHERE user_id = ? ORDER BY id DESC LIMIT " + RECENT_LIMIT;
        ArrayList<Notification> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Notification(rs.getInt(1), rs.getString(2), rs.getBoolean(3),
                            rs.getTimestamp(4).getTime()));
                }
            }
        }
        return result;
    }

    public void markAllRead(int userId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
