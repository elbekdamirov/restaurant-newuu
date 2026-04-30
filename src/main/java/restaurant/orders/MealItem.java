package restaurant.orders;

import java.math.BigDecimal;

public class MealItem {
    private final int id;
    private final int orderId;
    private final int seatNumber;
    private final int menuItemId;
    private final String itemName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;

    public MealItem(int id, int orderId, int seatNumber, int menuItemId, String itemName,
                    int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        this.id = id;
        this.orderId = orderId;
        this.seatNumber = seatNumber;
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public int getId() {
        return id;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
