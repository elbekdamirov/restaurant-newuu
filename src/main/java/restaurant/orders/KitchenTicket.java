package restaurant.orders;

public class KitchenTicket {
    private final int orderId;
    private final String tableCode;
    private final OrderStatus status;
    private final String itemSummary;

    public KitchenTicket(int orderId, String tableCode, OrderStatus status, String itemSummary) {
        this.orderId = orderId;
        this.tableCode = tableCode;
        this.status = status;
        this.itemSummary = itemSummary;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getTableCode() {
        return tableCode;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getItemSummary() {
        return itemSummary;
    }
}
