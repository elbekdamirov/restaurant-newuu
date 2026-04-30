package restaurant.billing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bill {
    private final int id;
    private final int orderId;
    private final String tableCode;
    private final BigDecimal subtotal;
    private final BigDecimal tax;
    private final BigDecimal serviceCharge;
    private final BigDecimal total;
    private final PaymentStatus paymentStatus;
    private final LocalDateTime createdAt;

    public Bill(int id, int orderId, String tableCode, BigDecimal subtotal, BigDecimal tax,
                BigDecimal serviceCharge, BigDecimal total, PaymentStatus paymentStatus, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.tableCode = tableCode;
        this.subtotal = subtotal;
        this.tax = tax;
        this.serviceCharge = serviceCharge;
        this.total = total;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getTableCode() {
        return tableCode;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getServiceCharge() {
        return serviceCharge;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Bill #" + id + " - Order #" + orderId + " - $" + total + " - " + paymentStatus;
    }
}
