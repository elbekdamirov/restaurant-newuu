package restaurant.menu;

public class Menu {
    private final int id;
    private final int branchId;
    private final String title;
    private final String description;
    private final boolean active;

    public Menu(int id, int branchId, String title, String description, boolean active) {
        this.id = id;
        this.branchId = branchId;
        this.title = title;
        this.description = description;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public int getBranchId() {
        return branchId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}
