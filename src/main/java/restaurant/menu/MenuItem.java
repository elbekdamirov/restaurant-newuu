package restaurant.menu;

import java.math.BigDecimal;

public class MenuItem {
    private final int id;
    private final int sectionId;
    private final String sectionTitle;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final boolean available;
    private final String imageName;

    public MenuItem(int id, int sectionId, String sectionTitle, String name, String description, BigDecimal price, boolean available, String imageName) {
        this.id = id;
        this.sectionId = sectionId;
        this.sectionTitle = sectionTitle;
        this.name = name;
        this.description = description;
        this.price = price;
        this.available = available;
        this.imageName = imageName;
    }

    public int getId() {
        return id;
    }

    public int getSectionId() {
        return sectionId;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getImageName() {
        return imageName;
    }

    @Override
    public String toString() {
        return name + " ($" + price + ")";
    }
}
