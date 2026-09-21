package iwish.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection settings. Override with JVM flags if needed, e.g.
 * -Diwish.db.user=root -Diwish.db.password=secret
 */
public final class Database {
    private static final String URL = System.getProperty("iwish.db.url",
            "jdbc:mariadb://localhost:3306/iwish");
    private static final String USER = System.getProperty("iwish.db.user", "root");
    private static final String PASSWORD = System.getProperty("iwish.db.password", "");

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
