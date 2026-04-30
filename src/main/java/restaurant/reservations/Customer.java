package restaurant.reservations;

public class Customer {
    private final int id;
    private final String fullName;
    private final String phone;
    private final String email;

    public Customer(int id, String fullName, String phone, String email) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
