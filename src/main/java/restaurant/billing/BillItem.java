package restaurant.billing;

import java.math.BigDecimal;

public class BillItem {
    private final int id;
    private final int billId;
    private final String itemName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;

    public BillItem(int id, int billId, String itemName, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        this.id = id;
        this.billId = billId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public int getId() {
        return id;
    }

    public int getBillId() {
        return billId;
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
