package restaurant.menu;

import restaurant.db.Database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MenuService {
    public List<MenuSection> findSectionsByBranch(int branchId) throws SQLException {
        String sql = """
                SELECT ms.id, ms.menu_id, ms.title, ms.description, ms.display_order
                FROM menu_sections ms
                JOIN menus m ON m.id = ms.menu_id
                WHERE m.branch_id = ? AND m.active = TRUE
                ORDER BY ms.display_order, ms.title
                """;
        List<MenuSection> sections = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sections.add(new MenuSection(
                            resultSet.getInt("id"),
                            resultSet.getInt("menu_id"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getInt("display_order")
                    ));
                }
            }
        }
        return sections;
    }

    public List<MenuItem> findItemsByBranch(int branchId) throws SQLException {
        String sql = """
                SELECT mi.id, mi.section_id, ms.title AS section_title, mi.name, mi.description, mi.price, mi.available, mi.image_name
                FROM menu_items mi
                JOIN menu_sections ms ON ms.id = mi.section_id
                JOIN menus m ON m.id = ms.menu_id
                WHERE m.branch_id = ? AND m.active = TRUE
                ORDER BY ms.display_order, mi.name
                """;
        List<MenuItem> items = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapItem(resultSet));
                }
            }
        }
        return items;
    }

    public List<MenuItem> findAvailableItemsByBranch(int branchId) throws SQLException {
        String sql = """
                SELECT mi.id, mi.section_id, ms.title AS section_title, mi.name, mi.description, mi.price, mi.available, mi.image_name
                FROM menu_items mi
                JOIN menu_sections ms ON ms.id = mi.section_id
                JOIN menus m ON m.id = ms.menu_id
                WHERE m.branch_id = ? AND m.active = TRUE AND mi.available = TRUE
                ORDER BY ms.display_order, mi.name
                """;
        List<MenuItem> items = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(mapItem(resultSet));
                }
            }
        }
        return items;
    }

    public int addMenuItem(int sectionId, String name, String description, BigDecimal price) throws SQLException {
        validateMenuItem(name, price);
        String sql = """
                INSERT INTO menu_items (section_id, name, description, price, available, image_name)
                VALUES (?, ?, ?, ?, TRUE, 'menu-default.png')
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, sectionId);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setBigDecimal(4, price);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public int countItems() throws SQLException {
        String sql = "SELECT COUNT(*) FROM menu_items";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private void validateMenuItem(String name, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Menu item name is required.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
    }

    private MenuItem mapItem(ResultSet resultSet) throws SQLException {
        return new MenuItem(
                resultSet.getInt("id"),
                resultSet.getInt("section_id"),
                resultSet.getString("section_title"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getBigDecimal("price"),
                resultSet.getBoolean("available"),
                resultSet.getString("image_name")
        );
    }
}
