package restaurant.reservations;

public class PostalNotification {
    private final String postalAddress;
    private final String message;

    public PostalNotification(String postalAddress, String message) {
        this.postalAddress = postalAddress;
        this.message = message;
    }

    public String getPostalAddress() {
        return postalAddress;
    }

    public String getMessage() {
        return message;
    }
}
