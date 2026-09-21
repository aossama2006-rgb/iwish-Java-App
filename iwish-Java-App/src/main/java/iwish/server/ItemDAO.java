package iwish.server;

import iwish.common.Item;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ItemDAO {

    public ArrayList<Item> getCatalog() throws SQLException {
        String sql = "SELECT id, name, category, price FROM items ORDER BY category, name";
        ArrayList<Item> result = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Item(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4)));
            }
        }
        return result;
    }
}
