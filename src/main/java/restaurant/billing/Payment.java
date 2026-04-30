package restaurant.billing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private final int id;
    private final int billId;
    private final PaymentMethod method;
    private final BigDecimal amount;
    private final PaymentStatus status;
    private final String details;
    private final LocalDateTime paidAt;

    public Payment(int id, int billId, PaymentMethod method, BigDecimal amount, PaymentStatus status, String details, LocalDateTime paidAt) {
        this.id = id;
        this.billId = billId;
        this.method = method;
        this.amount = amount;
        this.status = status;
        this.details = details;
        this.paidAt = paidAt;
    }

    public int getId() {
        return id;
    }

    public int getBillId() {
        return billId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
