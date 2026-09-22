package iwish.server;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Friends Pay for wish-list items, and the notifications sent when a gift is fully funded
public class ContributionDAO {
    private final FriendDAO friends = new FriendDAO();
    private final NotificationDAO notifications = new NotificationDAO();

    
     // Adds a contribution from contributorId to the wish-list entry wishId
   
     //return a message to show the contributor
    public String contribute(int contributorId, int wishId, String amountText)
            throws SQLException, BusinessException {
        BigDecimal amount = parseAmount(amountText);
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                String message = addContribution(c, contributorId, wishId, amount);
                c.commit();
                return message;
            } catch (SQLException | BusinessException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private String addContribution(Connection c, int contributorId, int wishId, BigDecimal amount)
            throws SQLException, BusinessException {
        // Lock the entry so two friends can't overfund it at the same moment
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id FROM wishlist_items WHERE id = ? FOR UPDATE")) {
            ps.setInt(1, wishId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new BusinessException("That item is no longer on the wish list.");
                }
            }
        }

        int ownerId;
        String ownerName;
        String itemName;
        BigDecimal price;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT w.user_id, u.name, i.name, i.price FROM wishlist_items w "
                        + "JOIN users u ON u.id = w.user_id JOIN items i ON i.id = w.item_id "
                        + "WHERE w.id = ?")) {
            ps.setInt(1, wishId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                ownerId = rs.getInt(1);
                ownerName = rs.getString(2);
                itemName = rs.getString(3);
                price = rs.getBigDecimal(4);
            }
        }

        if (ownerId == contributorId) {
            throw new BusinessException("You can't contribute to your own wish list.");
        }
        if (!friends.areFriends(contributorId, ownerId)) {
            throw new BusinessException("You can only contribute to a friend's wish list.");
        }

        BigDecimal remaining = price.subtract(sumFunded(c, wishId));
        if (remaining.signum() <= 0) {
            throw new BusinessException("This gift is already fully funded.");
        }
        if (amount.compareTo(remaining) > 0) {
            throw new BusinessException("That's more than needed. Only " + money(remaining)
                    + " is still missing.");
        }

        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO contributions (wishlist_item_id, contributor_id, amount) VALUES (?, ?, ?)")) {
            ps.setInt(1, wishId);
            ps.setInt(2, contributorId);
            ps.setBigDecimal(3, amount);
            ps.executeUpdate();
        }

        BigDecimal stillNeeded = remaining.subtract(amount);
        if (stillNeeded.signum() == 0) {
            notifyGiftComplete(c, wishId, ownerId, ownerName, itemName);
            return "Thank you! Your contribution completed \"" + itemName + "\" for " + ownerName + ".";
        }
        return "Contribution added. " + money(stillNeeded) + " is still needed for \"" + itemName + "\".";
    }

    // Tells every contributor (as buyers) and the owner (as receiver) that the gift is complete
    private void notifyGiftComplete(Connection c, int wishId, int ownerId, String ownerName, String itemName)
            throws SQLException {
        List<Integer> buyerIds = new ArrayList<>();
        List<String> buyerNames = new ArrayList<>();
        List<BigDecimal> shares = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT u.id, u.name, SUM(c.amount) FROM contributions c "
                        + "JOIN users u ON u.id = c.contributor_id WHERE c.wishlist_item_id = ? "
                        + "GROUP BY u.id, u.name ORDER BY MIN(c.id)")) {
            ps.setInt(1, wishId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    buyerIds.add(rs.getInt(1));
                    buyerNames.add(rs.getString(2));
                    shares.add(rs.getBigDecimal(3));
                }
            }
        }

        //  each buyer hears that the price is complete
        for (int i = 0; i < buyerIds.size(); i++) {
            notifications.add(c, buyerIds.get(i), "The gift \"" + itemName + "\" for " + ownerName
                    + " is now fully funded. Your share: " + money(shares.get(i)) + ". Thank you!");
        }
        //  the receiver hears who bought it
        notifications.add(c, ownerId, "Good news! \"" + itemName + "\" from your wish list has been bought by "
                + joinNames(buyerNames) + ".");
    }

    private BigDecimal sumFunded(Connection c, int wishId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COALESCE(SUM(amount), 0) FROM contributions WHERE wishlist_item_id = ?")) {
            ps.setInt(1, wishId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

   

    static BigDecimal parseAmount(String text) throws BusinessException {
        if (text == null || text.isBlank()) {
            throw new BusinessException("Enter the amount you want to contribute.");
        }
        String trimmedText = text.trim();
        if (!trimmedText.matches("[0-9]+([.][0-9]+)?|[-][0-9.]+")) {
            throw new BusinessException("Enter a valid amount, for example 250 or 99.50.");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(trimmedText);
        } catch (NumberFormatException e) {
            throw new BusinessException("Enter a valid amount, for example 250 or 99.50.");
        }
        if (amount.signum() <= 0) {
            throw new BusinessException("The amount must be greater than zero.");
        }
        BigDecimal trimmed = amount.stripTrailingZeros();
        if (trimmed.scale() > 2) {
            throw new BusinessException("Use at most 2 decimal places.");
        }
        if (trimmed.precision() - trimmed.scale() > 8) {
            throw new BusinessException("That amount is too large.");
        }
        return amount.setScale(2);
    }

    static String joinNames(List<String> names) {
        if (names.size() == 1) {
            return names.get(0);
        }
        String last = names.get(names.size() - 1);
        return String.join(", ", names.subList(0, names.size() - 1)) + " and " + last;
    }

    private static String money(BigDecimal value) {
        return String.format(Locale.US, "EGP %,.2f", value);
    }
}
