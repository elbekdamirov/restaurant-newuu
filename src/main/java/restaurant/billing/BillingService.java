package restaurant.billing;

import restaurant.db.Database;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BillingService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    private static final BigDecimal SERVICE_RATE = new BigDecimal("0.10");

    public int createBill(int orderId) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Integer existingBillId = findExistingBillId(connection, orderId);
                if (existingBillId != null) {
                    connection.commit();
                    return existingBillId;
                }

                BigDecimal subtotal = calculateSubtotal(connection, orderId);
                if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Order has no items. Add items before creating a bill.");
                }
                BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
                BigDecimal serviceCharge = subtotal.multiply(SERVICE_RATE).setScale(2, RoundingMode.HALF_UP);
                BigDecimal total = subtotal.add(tax).add(serviceCharge).setScale(2, RoundingMode.HALF_UP);

                int billId = insertBill(connection, orderId, subtotal, tax, serviceCharge, total);
                insertBillItems(connection, orderId, billId);
                connection.commit();
                return billId;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Bill> findAllBills() throws SQLException {
        String sql = """
                SELECT b.id, b.order_id, t.table_code, b.subtotal, b.tax, b.service_charge, b.total, b.payment_status, b.created_at
                FROM bills b
                JOIN orders o ON o.id = b.order_id
                JOIN restaurant_tables t ON t.id = o.table_id
                ORDER BY b.created_at DESC
                """;
        return findBills(sql);
    }

    public List<Bill> findUnpaidBills() throws SQLException {
        String sql = """
                SELECT b.id, b.order_id, t.table_code, b.subtotal, b.tax, b.service_charge, b.total, b.payment_status, b.created_at
                FROM bills b
                JOIN orders o ON o.id = b.order_id
                JOIN restaurant_tables t ON t.id = o.table_id
                WHERE b.payment_status = 'PENDING'
                ORDER BY b.created_at DESC
                """;
        return findBills(sql);
    }

    public List<BillItem> findBillItems(int billId) throws SQLException {
        String sql = """
                SELECT id, bill_id, item_name, quantity, unit_price, line_total
                FROM bill_items
                WHERE bill_id = ?
                ORDER BY item_name
                """;
        List<BillItem> items = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, billId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(new BillItem(
                            resultSet.getInt("id"),
                            resultSet.getInt("bill_id"),
                            resultSet.getString("item_name"),
                            resultSet.getInt("quantity"),
                            resultSet.getBigDecimal("unit_price"),
                            resultSet.getBigDecimal("line_total")
                    ));
                }
            }
        }
        return items;
    }

    public void payBill(Bill bill, PaymentMethod method, BigDecimal amount, String details) throws SQLException {
        if (amount == null || amount.compareTo(bill.getTotal()) < 0) {
            throw new IllegalArgumentException("Payment amount must be at least the bill total.");
        }
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String insertPayment = """
                        INSERT INTO payments (bill_id, method, amount, status, details)
                        VALUES (?, ?, ?, 'PAID', ?)
                        """;
                try (PreparedStatement statement = connection.prepareStatement(insertPayment)) {
                    statement.setInt(1, bill.getId());
                    statement.setString(2, method.name());
                    statement.setBigDecimal(3, amount);
                    statement.setString(4, details);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement("UPDATE bills SET payment_status = 'PAID' WHERE id = ?")) {
                    statement.setInt(1, bill.getId());
                    statement.executeUpdate();
                }

                int tableId = findTableIdForOrder(connection, bill.getOrderId());
                try (PreparedStatement statement = connection.prepareStatement("UPDATE orders SET status = 'CLOSED' WHERE id = ?")) {
                    statement.setInt(1, bill.getOrderId());
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("UPDATE restaurant_tables SET status = 'FREE' WHERE id = ?")) {
                    statement.setInt(1, tableId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public BigDecimal sumPaidToday() throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM payments
                WHERE DATE(paid_at) = CURRENT_DATE() AND status = 'PAID'
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getBigDecimal(1);
        }
    }

    private Integer findExistingBillId(Connection connection, int orderId) throws SQLException {
        String sql = "SELECT id FROM bills WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }
        return null;
    }

    private BigDecimal calculateSubtotal(Connection connection, int orderId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * unit_price), 0) AS subtotal FROM order_items WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBigDecimal("subtotal").setScale(2, RoundingMode.HALF_UP);
            }
        }
    }

    private int insertBill(Connection connection, int orderId, BigDecimal subtotal, BigDecimal tax,
                           BigDecimal serviceCharge, BigDecimal total) throws SQLException {
        String sql = """
                INSERT INTO bills (order_id, subtotal, tax, service_charge, total, payment_status)
                VALUES (?, ?, ?, ?, ?, 'PENDING')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, orderId);
            statement.setBigDecimal(2, subtotal);
            statement.setBigDecimal(3, tax);
            statement.setBigDecimal(4, serviceCharge);
            statement.setBigDecimal(5, total);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not create bill.");
    }

    private void insertBillItems(Connection connection, int orderId, int billId) throws SQLException {
        String sql = """
                INSERT INTO bill_items (bill_id, item_name, quantity, unit_price, line_total)
                SELECT ?, mi.name, oi.quantity, oi.unit_price, oi.quantity * oi.unit_price
                FROM order_items oi
                JOIN menu_items mi ON mi.id = oi.menu_item_id
                WHERE oi.order_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, billId);
            statement.setInt(2, orderId);
            statement.executeUpdate();
        }
    }

    private List<Bill> findBills(String sql) throws SQLException {
        List<Bill> bills = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Timestamp timestamp = resultSet.getTimestamp("created_at");
                LocalDateTime createdAt = timestamp == null ? null : timestamp.toLocalDateTime();
                bills.add(new Bill(
                        resultSet.getInt("id"),
                        resultSet.getInt("order_id"),
                        resultSet.getString("table_code"),
                        resultSet.getBigDecimal("subtotal"),
                        resultSet.getBigDecimal("tax"),
                        resultSet.getBigDecimal("service_charge"),
                        resultSet.getBigDecimal("total"),
                        PaymentStatus.valueOf(resultSet.getString("payment_status")),
                        createdAt
                ));
            }
        }
        return bills;
    }

    private int findTableIdForOrder(Connection connection, int orderId) throws SQLException {
        String sql = "SELECT table_id FROM orders WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("table_id");
                }
            }
        }
        throw new SQLException("Order not found.");
    }
}
