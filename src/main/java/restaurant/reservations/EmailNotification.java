package restaurant.reservations;

public class EmailNotification implements NotificationSender {
    @Override
    public void send(String destination, String message) {
        // In a real application, this would integrate with an SMTP server.
        System.out.println("Sending EMAIL to " + destination + ": " + message);
    }

    @Override
    public String getType() {
        return "EMAIL";
    }
}
