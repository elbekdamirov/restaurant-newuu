package restaurant.reservations;

public class EmailNotification {
    private final String email;
    private final String message;

    public EmailNotification(String email, String message) {
        this.email = email;
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }
}
