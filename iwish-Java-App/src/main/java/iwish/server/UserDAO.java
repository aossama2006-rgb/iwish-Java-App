package iwish.server;

import iwish.common.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    // Creates the account, the email must already be trimmed and lower-cased
    public User register(String name, String email, String password)
            throws SQLException, BusinessException {
        String salt = PasswordUtil.newSalt();
        String sql = "INSERT INTO users (name, email, password_hash, salt) VALUES (?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hash(password, salt));
            ps.setString(4, salt);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new User(keys.getInt(1), name, email);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState())) {
                throw new BusinessException("That email is already registered. Try signing in instead.");
            }
            throw e;
        }
    }

    public User authenticate(String email, String password) throws SQLException, BusinessException {
        String sql = "SELECT id, name, email, password_hash, salt FROM users WHERE email = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.matches(password, rs.getString("salt"),
                        rs.getString("password_hash"))) {
                    return new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
                }
            }
        }
        // Same message for unknown email and wrong password on purpose.
        throw new BusinessException("Incorrect email or password.");
    }

    // return the user, or null if nobody has that email 
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id, name, email FROM users WHERE email = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"))
                        : null;
            }
        }
    }
}
