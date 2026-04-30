package restaurant.reservations;

import java.time.LocalDateTime;

public class Notification {
    private final int id;
    private final int reservationId;
    private final int customerId;
    private final String type;
    private final String message;
    private final boolean sent;
    private final LocalDateTime createdAt;

    public Notification(int id, int reservationId, int customerId, String type, String message, boolean sent, LocalDateTime createdAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.type = type;
        this.message = message;
        this.sent = sent;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getReservationId() {
        return reservationId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSent() {
        return sent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
