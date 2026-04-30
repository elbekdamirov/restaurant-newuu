package restaurant.structure;

public class Address {
    private final String city;
    private final String addressLine;

    public Address(String city, String addressLine) {
        this.city = city;
        this.addressLine = addressLine;
    }

    public String getCity() {
        return city;
    }

    public String getAddressLine() {
        return addressLine;
    }

    @Override
    public String toString() {
        return addressLine + ", " + city;
    }
}
