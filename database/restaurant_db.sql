CREATE DATABASE IF NOT EXISTS restaurant_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE restaurant_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS bill_items;
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS menu_sections;
DROP TABLE IF EXISTS menus;
DROP TABLE IF EXISTS restaurant_tables;
DROP TABLE IF EXISTS branches;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE branches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(40),
    city VARCHAR(80) NOT NULL,
    address_line VARCHAR(180) NOT NULL,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL
);

CREATE TABLE restaurant_tables (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_id INT NOT NULL,
    table_code VARCHAR(20) NOT NULL,
    capacity INT NOT NULL,
    status ENUM('FREE','RESERVED','OCCUPIED','UNAVAILABLE') NOT NULL DEFAULT 'FREE',
    location_label VARCHAR(80) NOT NULL,
    CONSTRAINT fk_tables_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE
);

CREATE TABLE menus (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_id INT NOT NULL,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_menus_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE
);

CREATE TABLE menu_sections (
    id INT AUTO_INCREMENT PRIMARY KEY,
    menu_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    display_order INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_sections_menu FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE
);

CREATE TABLE menu_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    section_id INT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    image_name VARCHAR(80) NOT NULL DEFAULT 'menu-default.png',
    CONSTRAINT fk_items_section FOREIGN KEY (section_id) REFERENCES menu_sections(id) ON DELETE CASCADE
);

CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(40) NOT NULL,
    email VARCHAR(160),
    UNIQUE KEY uk_customers_phone (phone)
);

CREATE TABLE reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_id INT NOT NULL,
    table_id INT NOT NULL,
    customer_id INT NOT NULL,
    reservation_time DATETIME NOT NULL,
    people_count INT NOT NULL,
    status ENUM('PENDING','CONFIRMED','CHECKED_IN','CANCELED','COMPLETED','NO_SHOW') NOT NULL DEFAULT 'CONFIRMED',
    notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservations_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_reservations_table FOREIGN KEY (table_id) REFERENCES restaurant_tables(id),
    CONSTRAINT fk_reservations_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT,
    customer_id INT NOT NULL,
    type VARCHAR(30) NOT NULL,
    message VARCHAR(255) NOT NULL,
    sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_id INT NOT NULL,
    table_id INT NOT NULL,
    status ENUM('RECEIVED','PREPARING','READY','SERVED','CANCELED','CLOSED') NOT NULL DEFAULT 'RECEIVED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_orders_table FOREIGN KEY (table_id) REFERENCES restaurant_tables(id)
);

CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    seat_number INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

CREATE TABLE bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,
    subtotal DECIMAL(10,2) NOT NULL,
    tax DECIMAL(10,2) NOT NULL,
    service_charge DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    payment_status ENUM('PENDING','PAID','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bills_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE bill_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    item_name VARCHAR(120) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_bill_items_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE
);

CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    method ENUM('CASH','CREDIT_CARD','CHECK') NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING','PAID','FAILED','REFUNDED') NOT NULL DEFAULT 'PAID',
    details VARCHAR(255),
    paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE
);

INSERT INTO branches(name, phone, city, address_line, open_time, close_time) VALUES
('Central Branch', '+1 555 1000', 'City Center', '10 Main Street', '09:00:00', '23:00:00'),
('River Branch', '+1 555 2000', 'Riverside', '45 River Road', '10:00:00', '22:30:00');

INSERT INTO restaurant_tables(branch_id, table_code, capacity, status, location_label) VALUES
(1, 'T1', 2, 'FREE', 'Window'),
(1, 'T2', 4, 'FREE', 'Main floor'),
(1, 'T3', 4, 'RESERVED', 'Patio'),
(1, 'T4', 6, 'FREE', 'Family area'),
(1, 'T5', 8, 'OCCUPIED', 'Private room'),
(2, 'R1', 2, 'FREE', 'River view'),
(2, 'R2', 4, 'FREE', 'Main floor'),
(2, 'R3', 6, 'FREE', 'Terrace');

INSERT INTO menus(branch_id, title, description, active) VALUES
(1, 'Central Branch Menu', 'All-day dining menu for the central branch.', TRUE),
(2, 'River Branch Menu', 'Fresh dishes and specials for the river branch.', TRUE);

INSERT INTO menu_sections(menu_id, title, description, display_order) VALUES
(1, 'Appetizers', 'Small plates and starters.', 1),
(1, 'Main Course', 'Lunch and dinner meals.', 2),
(1, 'Desserts', 'Sweet dishes.', 3),
(1, 'Drinks', 'Hot and cold drinks.', 4),
(2, 'River Specials', 'Popular river branch items.', 1),
(2, 'Drinks', 'Branch drink menu.', 2);

INSERT INTO menu_items(section_id, name, description, price, available, image_name) VALUES
(1, 'Caesar Salad', 'Romaine, parmesan, and house dressing.', 7.99, TRUE, 'menu-salad.png'),
(1, 'Tomato Soup', 'Warm soup with herbs.', 6.50, TRUE, 'menu-soup.png'),
(2, 'Classic Burger', 'Beef burger with cheese, lettuce, and tomato.', 12.50, TRUE, 'menu-burger.png'),
(2, 'Creamy Pasta', 'Pasta with creamy garlic sauce.', 13.75, TRUE, 'menu-pasta.png'),
(2, 'Grilled Chicken', 'Grilled chicken with vegetables.', 15.25, TRUE, 'menu-chicken.png'),
(3, 'Chocolate Cake', 'Slice of rich chocolate cake.', 6.25, TRUE, 'menu-cake.png'),
(3, 'Cheesecake', 'New York style cheesecake.', 6.75, TRUE, 'menu-cheesecake.png'),
(4, 'Coffee', 'Fresh hot coffee.', 3.50, TRUE, 'menu-coffee.png'),
(4, 'Tea', 'Black or green tea.', 3.00, TRUE, 'menu-tea.png'),
(5, 'River Salmon', 'Grilled salmon with lemon butter.', 18.50, TRUE, 'menu-salmon.png'),
(5, 'Rice Bowl', 'Rice bowl with vegetables and sauce.', 11.75, TRUE, 'menu-bowl.png'),
(6, 'Lemonade', 'Fresh lemonade.', 4.00, TRUE, 'menu-lemonade.png');

INSERT INTO customers(full_name, phone, email) VALUES
('Sample Guest', '555-0100', 'guest@example.com'),
('Amina Karimova', '555-0111', 'amina@example.com');

INSERT INTO reservations(branch_id, table_id, customer_id, reservation_time, people_count, status, notes) VALUES
(1, 3, 1, DATE_ADD(NOW(), INTERVAL 2 HOUR), 4, 'CONFIRMED', 'Window table requested');

INSERT INTO notifications(reservation_id, customer_id, type, message, sent) VALUES
(1, 1, 'EMAIL', 'Reservation confirmed for Sample Guest.', TRUE);

INSERT INTO orders(branch_id, table_id, status) VALUES
(1, 5, 'RECEIVED');

INSERT INTO order_items(order_id, seat_number, menu_item_id, quantity, unit_price) VALUES
(1, 1, 3, 2, 12.50),
(1, 1, 8, 2, 3.50);
