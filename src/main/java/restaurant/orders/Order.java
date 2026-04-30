package restaurant.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private final int id;
    private final int branchId;
    private final String branchName;
    private final int tableId;
    private final String tableCode;
    private final OrderStatus status;
    private final LocalDateTime createdAt;
    private final BigDecimal total;

    public Order(int id, int branchId, String branchName, int tableId, String tableCode,
                 OrderStatus status, LocalDateTime createdAt, BigDecimal total) {
        this.id = id;
        this.branchId = branchId;
        this.branchName = branchName;
        this.tableId = tableId;
        this.tableCode = tableCode;
        this.status = status;
        this.createdAt = createdAt;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public int getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public int getTableId() {
        return tableId;
    }

    public String getTableCode() {
        return tableCode;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "Order #" + id + " - Table " + tableCode + " - " + status;
    }
}
