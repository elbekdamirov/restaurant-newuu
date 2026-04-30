package restaurant.billing;

public class CheckPayment {
    private final String bankName;
    private final String checkNumber;

    public CheckPayment(String bankName, String checkNumber) {
        this.bankName = bankName;
        this.checkNumber = checkNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public String getCheckNumber() {
        return checkNumber;
    }
}
