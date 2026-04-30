package restaurant.reservations;

import java.time.LocalDateTime;

public class Reservation {
    private final int id;
    private final int branchId;
    private final String branchName;
    private final int tableId;
    private final String tableCode;
    private final int customerId;
    private final String customerName;
    private final String customerPhone;
    private final String customerEmail;
    private final LocalDateTime reservationTime;
    private final int peopleCount;
    private final ReservationStatus status;
    private final String notes;

    public Reservation(int id, int branchId, String branchName, int tableId, String tableCode, int customerId,
                       String customerName, String customerPhone, String customerEmail, LocalDateTime reservationTime,
                       int peopleCount, ReservationStatus status, String notes) {
        this.id = id;
        this.branchId = branchId;
        this.branchName = branchName;
        this.tableId = tableId;
        this.tableCode = tableCode;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.reservationTime = reservationTime;
        this.peopleCount = peopleCount;
        this.status = status;
        this.notes = notes;
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

    public int getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public LocalDateTime getReservationTime() {
        return reservationTime;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }
}
