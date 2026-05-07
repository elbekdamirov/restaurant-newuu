package restaurant.reservations;

public class PostalNotification implements NotificationSender {
    @Override
    public void send(String destination, String message) {
        // In a real application, this would integrate with a postal service API.
        System.out.println("Sending POSTAL letter to " + destination + ": " + message);
    }

    @Override
    public String getType() {
        return "POSTAL";
    }
}
