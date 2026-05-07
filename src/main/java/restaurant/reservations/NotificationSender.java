package restaurant.reservations;

public interface NotificationSender {
    void send(String destination, String message);
    String getType();
}
