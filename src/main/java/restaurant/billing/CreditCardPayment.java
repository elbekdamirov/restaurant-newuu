package restaurant.billing;

public class CreditCardPayment {
    private final String cardholderName;
    private final String lastFourDigits;

    public CreditCardPayment(String cardholderName, String lastFourDigits) {
        this.cardholderName = cardholderName;
        this.lastFourDigits = lastFourDigits;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }
}
