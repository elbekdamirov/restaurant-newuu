package restaurant.orders;

import restaurant.db.Database;
import restaurant.structure.TableStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    public int createOrder(int branchId, int tableId) throws SQLException {
        String insertOrderSql = "INSERT INTO orders (branch_id, table_id, status) VALUES (?, ?, 'RECEIVED')";
        String updateTableSql = "UPDATE restaurant_tables SET status = ? WHERE id = ?";
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int orderId;
                try (PreparedStatement statement = connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, branchId);
                    statement.setInt(2, tableId);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Could not create order.");
                        }
                        orderId = keys.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(updateTableSql)) {
                    statement.setString(1, TableStatus.OCCUPIED.name());
                    statement.setInt(2, tableId);
                    statement.executeUpdate();
                }
                connection.commit();
                return orderId;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void addItem(int orderId, int seatNumber, int menuItemId, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        String priceSql = "SELECT price FROM menu_items WHERE id = ? AND available = TRUE";
        String insertSql = """
                INSERT INTO order_items (order_id, seat_number, menu_item_id, quantity, unit_price)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = Database.getConnection()) {
            BigDecimal price;
            try (PreparedStatement statement = connection.prepareStatement(priceSql)) {
                statement.setInt(1, menuItemId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("Menu item is not available.");
                    }
                    price = resultSet.getBigDecimal("price");
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                statement.setInt(1, orderId);
                statement.setInt(2, seatNumber);
                statement.setInt(3, menuItemId);
                statement.setInt(4, quantity);
                statement.setBigDecimal(5, price);
                statement.executeUpdate();
            }
        }
    }

    public List<Order> findActiveOrders() throws SQLException {
        String sql = """
                SELECT o.id, o.branch_id, b.name AS branch_name, o.table_id, t.table_code, o.status, o.created_at,
                       COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total
                FROM orders o
                JOIN branches b ON b.id = o.branch_id
                JOIN restaurant_tables t ON t.id = o.table_id
                LEFT JOIN order_items oi ON oi.order_id = o.id
                WHERE o.status NOT IN ('CANCELED','CLOSED')
                GROUP BY o.id, o.branch_id, b.name, o.table_id, t.table_code, o.status, o.created_at
                ORDER BY o.created_at DESC
                """;
        return findOrdersBySql(sql);
    }

    public List<Order> findBillableOrders() throws SQLException {
        String sql = """
                SELECT o.id, o.branch_id, b.name AS branch_name, o.table_id, t.table_code, o.status, o.created_at,
                       COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total
                FROM orders o
                JOIN branches b ON b.id = o.branch_id
                JOIN restaurant_tables t ON t.id = o.table_id
                LEFT JOIN order_items oi ON oi.order_id = o.id
                LEFT JOIN bills bill ON bill.order_id = o.id
                WHERE o.status NOT IN ('CANCELED','CLOSED') AND bill.id IS NULL
                GROUP BY o.id, o.branch_id, b.name, o.table_id, t.table_code, o.status, o.created_at
                HAVING total > 0
                ORDER BY o.created_at DESC
                """;
        return findOrdersBySql(sql);
    }

    public List<MealItem> findItemsByOrder(int orderId) throws SQLException {
        String sql = """
                SELECT oi.id, oi.order_id, oi.seat_number, oi.menu_item_id, mi.name,
                       oi.quantity, oi.unit_price, (oi.quantity * oi.unit_price) AS line_total
                FROM order_items oi
                JOIN menu_items mi ON mi.id = oi.menu_item_id
                WHERE oi.order_id = ?
                ORDER BY oi.seat_number, mi.name
                """;
        List<MealItem> items = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(new MealItem(
                            resultSet.getInt("id"),
                            resultSet.getInt("order_id"),
                            resultSet.getInt("seat_number"),
                            resultSet.getInt("menu_item_id"),
                            resultSet.getString("name"),
                            resultSet.getInt("quantity"),
                            resultSet.getBigDecimal("unit_price"),
                            resultSet.getBigDecimal("line_total")
                    ));
                }
            }
        }
        return items;
    }

    public void updateStatus(int orderId, OrderStatus status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    public int countOpenOrders() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE status NOT IN ('CANCELED','CLOSED')";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private List<Order> findOrdersBySql(String sql) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                LocalDateTime created = createdAt == null ? null : createdAt.toLocalDateTime();
                orders.add(new Order(
                        resultSet.getInt("id"),
                        resultSet.getInt("branch_id"),
                        resultSet.getString("branch_name"),
                        resultSet.getInt("table_id"),
                        resultSet.getString("table_code"),
                        OrderStatus.valueOf(resultSet.getString("status")),
                        created,
                        resultSet.getBigDecimal("total")
                ));
            }
        }
        return orders;
    }
}
