package restaurant.structure;

import java.time.LocalTime;

public class Branch {
    private final int id;
    private final String name;
    private final String phone;
    private final String city;
    private final String addressLine;
    private final LocalTime openTime;
    private final LocalTime closeTime;

    public Branch(int id, String name, String phone, String city, String addressLine, LocalTime openTime, LocalTime closeTime) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.city = city;
        this.addressLine = addressLine;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getCity() {
        return city;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public Address getAddress() {
        return new Address(city, addressLine);
    }

    @Override
    public String toString() {
        return name;
    }
}
