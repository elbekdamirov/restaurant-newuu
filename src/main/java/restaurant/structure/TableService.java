package restaurant.structure;

import restaurant.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TableService {
    public List<Branch> findAllBranches() throws SQLException {
        String sql = """
                SELECT id, name, phone, city, address_line, open_time, close_time
                FROM branches
                ORDER BY name
                """;
        List<Branch> branches = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                branches.add(mapBranch(resultSet));
            }
        }
        return branches;
    }

    public List<RestaurantTable> findTablesByBranch(int branchId) throws SQLException {
        String sql = """
                SELECT id, branch_id, table_code, capacity, status, location_label
                FROM restaurant_tables
                WHERE branch_id = ?
                ORDER BY table_code
                """;
        List<RestaurantTable> tables = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tables.add(mapTable(resultSet));
                }
            }
        }
        return tables;
    }

    public List<RestaurantTable> findAvailableTables(int branchId, int peopleCount, LocalDateTime dateTime) throws SQLException {
        String sql = """
                SELECT t.id, t.branch_id, t.table_code, t.capacity, t.status, t.location_label
                FROM restaurant_tables t
                WHERE t.branch_id = ?
                  AND t.capacity >= ?
                  AND t.status <> 'UNAVAILABLE'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM reservations r
                      WHERE r.table_id = t.id
                        AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN')
                        AND ABS(TIMESTAMPDIFF(MINUTE, r.reservation_time, ?)) < 120
                  )
                ORDER BY t.capacity, t.table_code
                """;
        List<RestaurantTable> tables = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, peopleCount);
            statement.setTimestamp(3, Timestamp.valueOf(dateTime));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tables.add(mapTable(resultSet));
                }
            }
        }
        return tables;
    }

    public int countTables() throws SQLException {
        String sql = "SELECT COUNT(*) FROM restaurant_tables";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    public void updateTableStatus(int tableId, TableStatus status) throws SQLException {
        String sql = "UPDATE restaurant_tables SET status = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, tableId);
            statement.executeUpdate();
        }
    }

    private Branch mapBranch(ResultSet resultSet) throws SQLException {
        return new Branch(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("phone"),
                resultSet.getString("city"),
                resultSet.getString("address_line"),
                resultSet.getObject("open_time", LocalTime.class),
                resultSet.getObject("close_time", LocalTime.class)
        );
    }

    private RestaurantTable mapTable(ResultSet resultSet) throws SQLException {
        return new RestaurantTable(
                resultSet.getInt("id"),
                resultSet.getInt("branch_id"),
                resultSet.getString("table_code"),
                resultSet.getInt("capacity"),
                TableStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("location_label")
        );
    }
}
