package restaurant.billing;

import java.math.BigDecimal;

public class CashPayment {
    private final BigDecimal amountTendered;

    public CashPayment(BigDecimal amountTendered) {
        this.amountTendered = amountTendered;
    }

    public BigDecimal getAmountTendered() {
        return amountTendered;
    }
}
