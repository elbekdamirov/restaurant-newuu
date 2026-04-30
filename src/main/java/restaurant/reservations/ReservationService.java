package restaurant.reservations;

import restaurant.db.Database;
import restaurant.structure.TableService;
import restaurant.structure.TableStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {
    private final NotificationService notificationService = new NotificationService();
    private final TableService tableService = new TableService();

    public int createReservation(int branchId, int tableId, String customerName, String phone, String email,
                                 LocalDateTime reservationTime, int peopleCount, String notes) throws SQLException {
        validateReservationInput(customerName, phone, peopleCount);
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int customerId = findOrCreateCustomer(connection, customerName, phone, email);
                int reservationId = insertReservation(connection, branchId, tableId, customerId, reservationTime, peopleCount, notes);
                connection.commit();

                String message = "Reservation confirmed for " + customerName + " at " + reservationTime + ".";
                notificationService.createNotification(reservationId, customerId, "EMAIL", message, true);
                return reservationId;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Reservation> findAllReservations() throws SQLException {
        String sql = """
                SELECT r.id, r.branch_id, b.name AS branch_name, r.table_id, t.table_code,
                       r.customer_id, c.full_name, c.phone, c.email,
                       r.reservation_time, r.people_count, r.status, r.notes
                FROM reservations r
                JOIN branches b ON b.id = r.branch_id
                JOIN restaurant_tables t ON t.id = r.table_id
                JOIN customers c ON c.id = r.customer_id
                ORDER BY r.reservation_time DESC
                """;
        List<Reservation> reservations = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                reservations.add(mapReservation(resultSet));
            }
        }
        return reservations;
    }

    public void cancelReservation(Reservation reservation) throws SQLException {
        String sql = "UPDATE reservations SET status = 'CANCELED' WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservation.getId());
            statement.executeUpdate();
        }
        String message = "Reservation canceled for " + reservation.getCustomerName() + ".";
        notificationService.createNotification(reservation.getId(), reservation.getCustomerId(), "EMAIL", message, true);
    }

    public void checkInReservation(Reservation reservation) throws SQLException {
        String sql = "UPDATE reservations SET status = 'CHECKED_IN' WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservation.getId());
            statement.executeUpdate();
        }
        tableService.updateTableStatus(reservation.getTableId(), TableStatus.OCCUPIED);
    }

    public int countTodayReservations() throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM reservations
                WHERE DATE(reservation_time) = CURRENT_DATE()
                  AND status IN ('PENDING','CONFIRMED','CHECKED_IN')
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private int findOrCreateCustomer(Connection connection, String fullName, String phone, String email) throws SQLException {
        String findSql = "SELECT id FROM customers WHERE phone = ?";
        try (PreparedStatement statement = connection.prepareStatement(findSql)) {
            statement.setString(1, phone);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    updateCustomer(connection, id, fullName, email);
                    return id;
                }
            }
        }

        String insertSql = "INSERT INTO customers (full_name, phone, email) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, fullName);
            statement.setString(2, phone);
            statement.setString(3, email);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not create customer.");
    }

    private void updateCustomer(Connection connection, int customerId, String fullName, String email) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, email = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setInt(3, customerId);
            statement.executeUpdate();
        }
    }

    private int insertReservation(Connection connection, int branchId, int tableId, int customerId,
                                  LocalDateTime reservationTime, int peopleCount, String notes) throws SQLException {
        String sql = """
                INSERT INTO reservations (branch_id, table_id, customer_id, reservation_time, people_count, status, notes)
                VALUES (?, ?, ?, ?, ?, 'CONFIRMED', ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, branchId);
            statement.setInt(2, tableId);
            statement.setInt(3, customerId);
            statement.setTimestamp(4, Timestamp.valueOf(reservationTime));
            statement.setInt(5, peopleCount);
            statement.setString(6, notes);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Could not create reservation.");
    }

    private void validateReservationInput(String customerName, String phone, int peopleCount) {
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Customer name is required.");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone is required.");
        }
        if (peopleCount <= 0) {
            throw new IllegalArgumentException("People count must be greater than zero.");
        }
    }

    private Reservation mapReservation(ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("reservation_time");
        return new Reservation(
                resultSet.getInt("id"),
                resultSet.getInt("branch_id"),
                resultSet.getString("branch_name"),
                resultSet.getInt("table_id"),
                resultSet.getString("table_code"),
                resultSet.getInt("customer_id"),
                resultSet.getString("full_name"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                timestamp.toLocalDateTime(),
                resultSet.getInt("people_count"),
                ReservationStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("notes")
        );
    }
}
