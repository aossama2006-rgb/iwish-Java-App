package iwish.server;

import iwish.common.FriendRequest;
import iwish.common.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class FriendDAO {
    private final UserDAO users = new UserDAO();

    public ArrayList<User> getFriends(int userId) throws SQLException {
        String sql = "SELECT u.id, u.name, u.email FROM friendships f "
                + "JOIN users u ON u.id = CASE WHEN f.user_a = ? THEN f.user_b ELSE f.user_a END "
                + "WHERE f.user_a = ? OR f.user_b = ? ORDER BY u.name";
        ArrayList<User> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new User(rs.getInt(1), rs.getString(2), rs.getString(3)));
                }
            }
        }
        return result;
    }

    public ArrayList<FriendRequest> getIncomingRequests(int userId) throws SQLException {
        String sql = "SELECT r.id, u.id, u.name, u.email FROM friend_requests r "
                + "JOIN users u ON u.id = r.sender_id "
                + "WHERE r.receiver_id = ? ORDER BY r.created_at";
        ArrayList<FriendRequest> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User sender = new User(rs.getInt(2), rs.getString(3), rs.getString(4));
                    result.add(new FriendRequest(rs.getInt(1), sender));
                }
            }
        }
        return result;
    }

    //Sends a friend request to the user with the given email,Returns the target user
    public User sendRequest(int senderId, String targetEmail) throws SQLException, BusinessException {
        User target = users.findByEmail(targetEmail);
        if (target == null) {
            throw new BusinessException("No user found with that email.");
        }
        if (target.getId() == senderId) {
            throw new BusinessException("You can't add yourself as a friend.");
        }
        try (Connection c = Database.getConnection()) {
            if (areFriends(c, senderId, target.getId())) {
                throw new BusinessException("You and " + target.getName() + " are already friends.");
            }
            if (requestExists(c, senderId, target.getId())) {
                throw new BusinessException("You already sent a request to " + target.getName() + ".");
            }
            if (requestExists(c, target.getId(), senderId)) {
                throw new BusinessException(target.getName()
                        + " already sent you a request. Accept it from the Requests tab.");
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO friend_requests (sender_id, receiver_id) VALUES (?, ?)")) {
                ps.setInt(1, senderId);
                ps.setInt(2, target.getId());
                ps.executeUpdate();
            }
        }
        return target;
    }

    // Accepts a request addressed to userId, Returns the new friend
    public User acceptRequest(int userId, int requestId) throws SQLException, BusinessException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                int senderId;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT sender_id FROM friend_requests WHERE id = ? AND receiver_id = ? FOR UPDATE")) {
                    ps.setInt(1, requestId);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new BusinessException("That request no longer exists.");
                        }
                        senderId = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT IGNORE INTO friendships (user_a, user_b) VALUES (LEAST(?, ?), GREATEST(?, ?))")) {
                    ps.setInt(1, userId);
                    ps.setInt(2, senderId);
                    ps.setInt(3, userId);
                    ps.setInt(4, senderId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM friend_requests WHERE id = ?")) {
                    ps.setInt(1, requestId);
                    ps.executeUpdate();
                }
                User friend = loadUser(c, senderId);
                c.commit();
                return friend;
            } catch (SQLException | BusinessException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void declineRequest(int userId, int requestId) throws SQLException, BusinessException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM friend_requests WHERE id = ? AND receiver_id = ?")) {
            ps.setInt(1, requestId);
            ps.setInt(2, userId);
            if (ps.executeUpdate() == 0) {
                throw new BusinessException("That request no longer exists.");
            }
        }
    }

    public void removeFriend(int userId, int friendId) throws SQLException, BusinessException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM friendships WHERE user_a = LEAST(?, ?) AND user_b = GREATEST(?, ?)")) {
            ps.setInt(1, userId);
            ps.setInt(2, friendId);
            ps.setInt(3, userId);
            ps.setInt(4, friendId);
            if (ps.executeUpdate() == 0) {
                throw new BusinessException("You are not friends with that user.");
            }
        }
    }

    public boolean areFriends(int a, int b) throws SQLException {
        try (Connection c = Database.getConnection()) {
            return areFriends(c, a, b);
        }
    }

    

    private boolean areFriends(Connection c, int a, int b) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM friendships WHERE user_a = LEAST(?, ?) AND user_b = GREATEST(?, ?)")) {
            ps.setInt(1, a);
            ps.setInt(2, b);
            ps.setInt(3, a);
            ps.setInt(4, b);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean requestExists(Connection c, int senderId, int receiverId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM friend_requests WHERE sender_id = ? AND receiver_id = ?")) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private User loadUser(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id, name, email FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new User(rs.getInt(1), rs.getString(2), rs.getString(3));
            }
        }
    }
}
