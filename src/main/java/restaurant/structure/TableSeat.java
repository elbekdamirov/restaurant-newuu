package restaurant.structure;

public class TableSeat {
    private final int id;
    private final int tableId;
    private final int seatNumber;

    public TableSeat(int id, int tableId, int seatNumber) {
        this.id = id;
        this.tableId = tableId;
        this.seatNumber = seatNumber;
    }

    public int getId() {
        return id;
    }

    public int getTableId() {
        return tableId;
    }

    public int getSeatNumber() {
        return seatNumber;
    }
}
