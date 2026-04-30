package restaurant.orders;

import restaurant.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KitchenService {
    public List<KitchenTicket> findKitchenTickets() throws SQLException {
        String sql = """
                SELECT o.id AS order_id, t.table_code, o.status,
                       GROUP_CONCAT(CONCAT(oi.quantity, 'x ', mi.name, ' (Seat ', oi.seat_number, ')') ORDER BY oi.seat_number SEPARATOR ', ') AS items
                FROM orders o
                JOIN restaurant_tables t ON t.id = o.table_id
                JOIN order_items oi ON oi.order_id = o.id
                JOIN menu_items mi ON mi.id = oi.menu_item_id
                WHERE o.status IN ('RECEIVED','PREPARING','READY')
                GROUP BY o.id, t.table_code, o.status
                ORDER BY o.created_at
                """;
        List<KitchenTicket> tickets = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tickets.add(new KitchenTicket(
                        resultSet.getInt("order_id"),
                        resultSet.getString("table_code"),
                        OrderStatus.valueOf(resultSet.getString("status")),
                        resultSet.getString("items")
                ));
            }
        }
        return tickets;
    }
}
