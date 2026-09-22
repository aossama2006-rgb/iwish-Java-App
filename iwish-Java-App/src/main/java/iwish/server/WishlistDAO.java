package iwish.server;

import iwish.common.Item;
import iwish.common.WishItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;

public class WishlistDAO {
    private static final int MAX_NOTE_LENGTH = 200;

    private final FriendDAO friends = new FriendDAO();

  
    public ArrayList<WishItem> getWishlist(int userId, boolean hidePartialProgress) throws SQLException {
        String sql = "SELECT w.id, w.note, i.id, i.name, i.category, i.price, "
                + "COALESCE((SELECT SUM(c.amount) FROM contributions c WHERE c.wishlist_item_id = w.id), 0) "
                + "FROM wishlist_items w JOIN items i ON i.id = w.item_id "
                + "WHERE w.user_id = ? ORDER BY w.id DESC";
        ArrayList<WishItem> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Item item = new Item(rs.getInt(3), rs.getString(4), rs.getString(5), rs.getBigDecimal(6));
                    BigDecimal funded = rs.getBigDecimal(7).setScale(2, RoundingMode.HALF_UP);
                    if (hidePartialProgress && funded.compareTo(item.getPrice()) < 0) {
                        funded = BigDecimal.ZERO.setScale(2);
                    }
                    result.add(new WishItem(rs.getInt(1), item, rs.getString(2), funded));
                }
            }
        }
        return result;
    }

    // A user may only look at the wish lists of their friends
    public ArrayList<WishItem> getFriendWishlist(int viewerId, int friendId)
            throws SQLException, BusinessException {
        if (!friends.areFriends(viewerId, friendId)) {
            throw new BusinessException("You can only view the wish lists of your friends.");
        }
        return getWishlist(friendId, false);
    }

    // Adds a catalog item to the user's wish list, Returns the item that was added
    public Item add(int userId, int itemId, String note) throws SQLException, BusinessException {
        String cleanNote = cleanNote(note);
        try (Connection c = Database.getConnection()) {
            Item item = findItem(c, itemId);
            if (item == null) {
                throw new BusinessException("That item is no longer available.");
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO wishlist_items (user_id, item_id, note) VALUES (?, ?, ?)")) {
                ps.setInt(1, userId);
                ps.setInt(2, itemId);
                ps.setString(3, cleanNote.isEmpty() ? null : cleanNote);
                ps.executeUpdate();
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState())) {
                    throw new BusinessException(item.getName() + " is already on your wish list.");
                }
                throw e;
            }
            return item;
        }
    }

    public void updateNote(int userId, int wishId, String note) throws SQLException, BusinessException {
        String cleanNote = cleanNote(note);
        try (Connection c = Database.getConnection()) {
            if (!ownsEntry(c, userId, wishId)) {
                throw new BusinessException("That item is no longer on your wish list.");
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE wishlist_items SET note = ? WHERE id = ? AND user_id = ?")) {
                ps.setString(1, cleanNote.isEmpty() ? null : cleanNote);
                ps.setInt(2, wishId);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }
        }
    }

    public void remove(int userId, int wishId) throws SQLException, BusinessException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT id FROM wishlist_items WHERE id = ? AND user_id = ? FOR UPDATE")) {
                    ps.setInt(1, wishId);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new BusinessException("That item is no longer on your wish list.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT 1 FROM contributions WHERE wishlist_item_id = ? LIMIT 1")) {
                    ps.setInt(1, wishId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new BusinessException(
                                    "Some of your friends are already contributing to this gift, "
                                            + "so it can't be removed.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM wishlist_items WHERE id = ?")) {
                    ps.setInt(1, wishId);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException | BusinessException e) {
                c.rollback();
                throw e;
            }
        }
    }

   

    private static String cleanNote(String note) throws BusinessException {
        String n = note == null ? "" : note.trim();
        if (n.length() > MAX_NOTE_LENGTH) {
            throw new BusinessException("Notes can be at most " + MAX_NOTE_LENGTH + " characters.");
        }
        return n;
    }

    private Item findItem(Connection c, int itemId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, name, category, price FROM items WHERE id = ?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? new Item(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4))
                        : null;
            }
        }
    }

    private boolean ownsEntry(Connection c, int userId, int wishId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM wishlist_items WHERE id = ? AND user_id = ?")) {
            ps.setInt(1, wishId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
