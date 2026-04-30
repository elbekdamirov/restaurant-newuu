package restaurant.orders;

public class Meal {
    private final int seatNumber;
    private final int orderId;

    public Meal(int orderId, int seatNumber) {
        this.orderId = orderId;
        this.seatNumber = seatNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public int getOrderId() {
        return orderId;
    }
}
