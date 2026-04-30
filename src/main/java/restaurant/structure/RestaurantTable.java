package restaurant.structure;

public class RestaurantTable {
    private final int id;
    private final int branchId;
    private final String tableCode;
    private final int capacity;
    private final TableStatus status;
    private final String locationLabel;

    public RestaurantTable(int id, int branchId, String tableCode, int capacity, TableStatus status, String locationLabel) {
        this.id = id;
        this.branchId = branchId;
        this.tableCode = tableCode;
        this.capacity = capacity;
        this.status = status;
        this.locationLabel = locationLabel;
    }

    public int getId() {
        return id;
    }

    public int getBranchId() {
        return branchId;
    }

    public String getTableCode() {
        return tableCode;
    }

    public int getCapacity() {
        return capacity;
    }

    public TableStatus getStatus() {
        return status;
    }

    public String getLocationLabel() {
        return locationLabel;
    }

    @Override
    public String toString() {
        return tableCode + " (" + capacity + " seats, " + locationLabel + ")";
    }
}
