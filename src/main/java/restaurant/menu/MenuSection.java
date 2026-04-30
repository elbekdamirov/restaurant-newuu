package restaurant.menu;

public class MenuSection {
    private final int id;
    private final int menuId;
    private final String title;
    private final String description;
    private final int displayOrder;

    public MenuSection(int id, int menuId, String title, String description, int displayOrder) {
        this.id = id;
        this.menuId = menuId;
        this.title = title;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    public int getId() {
        return id;
    }

    public int getMenuId() {
        return menuId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    @Override
    public String toString() {
        return title;
    }
}
