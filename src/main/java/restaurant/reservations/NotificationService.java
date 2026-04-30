package restaurant.reservations;

import restaurant.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NotificationService {
    public void createNotification(Integer reservationId, int customerId, String type, String message, boolean sent) throws SQLException {
        String sql = """
                INSERT INTO notifications (reservation_id, customer_id, type, message, sent)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (reservationId == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, reservationId);
            }
            statement.setInt(2, customerId);
            statement.setString(3, type);
            statement.setString(4, message);
            statement.setBoolean(5, sent);
            statement.executeUpdate();
        }
    }
}
