package restaurant.reservations;

import restaurant.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
            
            if (sent) {
                sendNotification(type, message);
            }
        }
    }
    
    private void sendNotification(String type, String message) {
        NotificationSender sender;
        if ("POSTAL".equalsIgnoreCase(type)) {
            sender = new PostalNotification();
        } else {
            sender = new EmailNotification();
        }
        sender.send("Customer Destination", message);
    }

    public List<Notification> findAllNotifications() throws SQLException {
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
        List<Notification> notifications = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Timestamp timestamp = resultSet.getTimestamp("created_at");
                notifications.add(new Notification(
                        resultSet.getInt("id"),
                        resultSet.getInt("reservation_id"),
                        resultSet.getInt("customer_id"),
                        resultSet.getString("type"),
                        resultSet.getString("message"),
                        resultSet.getBoolean("sent"),
                        timestamp.toLocalDateTime()
                ));
            }
        }
        return notifications;
    }
}
