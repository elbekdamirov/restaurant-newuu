package restaurant.app;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import restaurant.billing.Bill;
import restaurant.billing.BillItem;
import restaurant.billing.BillingService;
import restaurant.billing.PaymentMethod;
import restaurant.db.Database;
import restaurant.menu.MenuItem;
import restaurant.menu.MenuSection;
import restaurant.menu.MenuService;
import restaurant.orders.KitchenService;
import restaurant.orders.KitchenTicket;
import restaurant.orders.MealItem;
import restaurant.orders.Order;
import restaurant.orders.OrderService;
import restaurant.orders.OrderStatus;
import restaurant.reservations.Reservation;
import restaurant.reservations.ReservationService;
import restaurant.structure.Branch;
import restaurant.structure.RestaurantTable;
import restaurant.structure.TableService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Function;

public class RestaurantFxApplication extends javafx.application.Application {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    private final TableService tableService = new TableService();
    private final MenuService menuService = new MenuService();
    private final ReservationService reservationService = new ReservationService();
    private final OrderService orderService = new OrderService();
    private final KitchenService kitchenService = new KitchenService();
    private final BillingService billingService = new BillingService();
    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-ok");

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(createDashboardTab());
        tabPane.getTabs().add(createTablesTab());
        tabPane.getTabs().add(createMenuTab());
        tabPane.getTabs().add(createReservationsTab());
        tabPane.getTabs().add(createOrdersTab());
        tabPane.getTabs().add(createBillingTab());
        tabPane.getTabs().forEach(tab -> tab.setClosable(false));

        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(tabPane);
        root.setBottom(createStatusBar());

        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        stage.setTitle("Restaurant Management System");
        stage.setMinWidth(1060);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    private HBox createHeader() {
        ImageView logo = imageView("/images/logo.png", 82, 58);
        Label title = new Label("Restaurant Management System");
        title.getStyleClass().add("title-label");
        Label subtitle = new Label("Tables, menu, reservations, orders, kitchen, bills and payments");
        subtitle.getStyleClass().add("subtitle-label");
        VBox text = new VBox(2, title, subtitle);
        HBox header = new HBox(16, logo, text);
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox createStatusBar() {
        HBox box = new HBox(statusLabel);
        box.getStyleClass().add("status-bar");
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Tab createDashboardTab() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(20));

        HBox hero = new HBox(18);
        hero.getChildren().addAll(photo("/images/dashboard.jpg", 470, 260), dashboardIntro());
        HBox.setHgrow(hero.getChildren().get(1), Priority.ALWAYS);

        HBox stats = new HBox(14);
        stats.setAlignment(Pos.CENTER_LEFT);
        try {
            stats.getChildren().addAll(
                    statCard(String.valueOf(tableService.findAllBranches().size()), "Branches"),
                    statCard(String.valueOf(tableService.countTables()), "Tables"),
                    statCard(String.valueOf(menuService.countItems()), "Menu items"),
                    statCard(String.valueOf(reservationService.countTodayReservations()), "Today reservations"),
                    statCard(String.valueOf(orderService.countOpenOrders()), "Open orders"),
                    statCard(money(billingService.sumPaidToday()), "Paid today")
            );
        } catch (Exception ex) {
            stats.getChildren().add(statCard("MySQL", "Start XAMPP and import SQL"));
        }

        VBox flow = card();
        flow.getChildren().addAll(
                sectionTitle("Simple workflow"),
                text("Search tables, create reservations, take orders, send them to kitchen, create bills and receive payments.")
        );

        content.getChildren().addAll(hero, stats, flow);
        return createTab("Dashboard", scroll(content));
    }

    private VBox dashboardIntro() {
        VBox intro = card();
        Label heading = sectionTitle("Clean JavaFX version");
        Label connection = text(Database.testConnection()
                ? "Database: connected to MySQL"
                : "Database: not connected. Start XAMPP MySQL and import database/restaurant_db.sql");
        Button refresh = button("Refresh", "secondary-button");
        refresh.setOnAction(event -> setStatus("Dashboard refreshed", true));
        intro.getChildren().addAll(heading, text("This version uses a simpler layout, softer colors and in-window messages instead of pop-up alerts."), connection, refresh);
        return intro;
    }

    private Tab createTablesTab() {
        ComboBox<Branch> branchCombo = branchComboBox();
        Spinner<Integer> peopleSpinner = new Spinner<>(1, 20, 4);
        peopleSpinner.setEditable(true);
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        TextField timeField = new TextField("19:00");

        TableView<RestaurantTable> tableView = new TableView<>();
        tableView.getColumns().addAll(
                column("Table", RestaurantTable::getTableCode),
                column("Capacity", table -> String.valueOf(table.getCapacity())),
                column("Status", table -> table.getStatus().name()),
                column("Location", RestaurantTable::getLocationLabel)
        );
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Button showAll = button("Show tables", "secondary-button");
        showAll.setOnAction(event -> runSafely(() -> {
            Branch branch = selected(branchCombo, "Choose a branch.");
            List<RestaurantTable> tables = tableService.findTablesByBranch(branch.getId());
            tableView.setItems(FXCollections.observableArrayList(tables));
            setStatus(tables.size() + " tables loaded", true);
        }));

        Button search = button("Find available", "primary-button");
        search.setOnAction(event -> runSafely(() -> {
            Branch branch = selected(branchCombo, "Choose a branch.");
            LocalDateTime dateTime = readDateTime(datePicker, timeField);
            List<RestaurantTable> tables = tableService.findAvailableTables(branch.getId(), peopleSpinner.getValue(), dateTime);
            tableView.setItems(FXCollections.observableArrayList(tables));
            setStatus(tables.size() + " available tables found", true);
        }));

        GridPane form = formGrid();
        form.add(new Label("Branch"), 0, 0);
        form.add(branchCombo, 1, 0);
        form.add(new Label("People"), 0, 1);
        form.add(peopleSpinner, 1, 1);
        form.add(new Label("Date"), 0, 2);
        form.add(datePicker, 1, 2);
        form.add(new Label("Time"), 0, 3);
        form.add(timeField, 1, 3);
        form.add(new HBox(10, search, showAll), 1, 4);

        VBox left = sidePanel("Table availability", "/images/tables.jpg", form);
        VBox right = tablePanel("Tables", tableView);
        HBox content = split(left, right);
        loadBranches(branchCombo);
        showAll.fire();
        return createTab("Tables", content);
    }

    private Tab createMenuTab() {
        ComboBox<Branch> branchCombo = branchComboBox();
        ComboBox<MenuSection> sectionCombo = new ComboBox<>();
        TextField nameField = new TextField();
        nameField.setPromptText("Item name");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        TableView<MenuItem> menuTable = new TableView<>();
        menuTable.getColumns().addAll(
                column("Section", MenuItem::getSectionTitle),
                column("Item", MenuItem::getName),
                column("Price", item -> money(item.getPrice())),
                column("Available", item -> item.isAvailable() ? "Yes" : "No")
        );
        menuTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Runnable refreshMenu = () -> runSafely(() -> {
            Branch branch = selected(branchCombo, "Choose a branch.");
            List<MenuItem> items = menuService.findItemsByBranch(branch.getId());
            menuTable.setItems(FXCollections.observableArrayList(items));
            sectionCombo.setItems(FXCollections.observableArrayList(menuService.findSectionsByBranch(branch.getId())));
            if (!sectionCombo.getItems().isEmpty()) {
                sectionCombo.getSelectionModel().selectFirst();
            }
            setStatus(items.size() + " menu items loaded", true);
        });

        branchCombo.valueProperty().addListener((obs, oldValue, newValue) -> refreshMenu.run());

        Button refresh = button("Refresh", "secondary-button");
        refresh.setOnAction(event -> refreshMenu.run());

        Button add = button("Add item", "primary-button");
        add.setOnAction(event -> runSafely(() -> {
            MenuSection section = selected(sectionCombo, "Choose a section.");
            int id = menuService.addMenuItem(section.getId(), nameField.getText().trim(), descriptionArea.getText().trim(), readMoney(priceField.getText()));
            nameField.clear();
            priceField.clear();
            descriptionArea.clear();
            refreshMenu.run();
            setStatus("Menu item #" + id + " added", true);
        }));

        GridPane form = formGrid();
        form.add(new Label("Branch"), 0, 0);
        form.add(branchCombo, 1, 0);
        form.add(refresh, 2, 0);
        form.add(new Label("Section"), 0, 1);
        form.add(sectionCombo, 1, 1, 2, 1);
        form.add(new Label("Name"), 0, 2);
        form.add(nameField, 1, 2, 2, 1);
        form.add(new Label("Price"), 0, 3);
        form.add(priceField, 1, 3, 2, 1);
        form.add(new Label("Description"), 0, 4);
        form.add(descriptionArea, 1, 4, 2, 1);
        form.add(add, 1, 5, 2, 1);

        VBox left = sidePanel("Menu", "/images/menu.jpg", form);
        VBox right = tablePanel("Menu items", menuTable);
        HBox content = split(left, right);
        loadBranches(branchCombo);
        refreshMenu.run();
        return createTab("Menu", content);
    }

    private Tab createReservationsTab() {
        ComboBox<Branch> branchCombo = branchComboBox();
        Spinner<Integer> peopleSpinner = new Spinner<>(1, 20, 4);
        peopleSpinner.setEditable(true);
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        TextField timeField = new TextField("18:30");
        ComboBox<RestaurantTable> tableCombo = new ComboBox<>();
        TextField nameField = new TextField();
        nameField.setPromptText("Customer name");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes");
        notesArea.setPrefRowCount(3);

        TableView<Reservation> reservationTable = new TableView<>();
        reservationTable.getColumns().addAll(
                column("ID", reservation -> String.valueOf(reservation.getId())),
                column("Customer", Reservation::getCustomerName),
                column("Table", Reservation::getTableCode),
                column("People", reservation -> String.valueOf(reservation.getPeopleCount())),
                column("Time", reservation -> formatDateTime(reservation.getReservationTime())),
                column("Status", reservation -> reservation.getStatus().name())
        );
        reservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Runnable refreshReservations = () -> runSafely(() -> {
            List<Reservation> reservations = reservationService.findAllReservations();
            reservationTable.setItems(FXCollections.observableArrayList(reservations));
            setStatus(reservations.size() + " reservations loaded", true);
        });

        Button findTables = button("Find tables", "secondary-button");
        findTables.setOnAction(event -> runSafely(() -> {
            Branch branch = selected(branchCombo, "Choose a branch.");
            LocalDateTime dateTime = readDateTime(datePicker, timeField);
            List<RestaurantTable> tables = tableService.findAvailableTables(branch.getId(), peopleSpinner.getValue(), dateTime);
            tableCombo.setItems(FXCollections.observableArrayList(tables));
            if (!tables.isEmpty()) {
                tableCombo.getSelectionModel().selectFirst();
            }
            setStatus(tables.size() + " tables available for reservation", true);
        }));

        Button create = button("Create reservation", "primary-button");
        create.setOnAction(event -> runSafely(() -> {
            Branch branch = selected(branchCombo, "Choose a branch.");
            RestaurantTable table = selected(tableCombo, "Find and select a table.");
            int id = reservationService.createReservation(branch.getId(), table.getId(), nameField.getText().trim(), phoneField.getText().trim(), emailField.getText().trim(), readDateTime(datePicker, timeField), peopleSpinner.getValue(), notesArea.getText().trim());
            nameField.clear();
            phoneField.clear();
            emailField.clear();
            notesArea.clear();
            refreshReservations.run();
            setStatus("Reservation #" + id + " created", true);
        }));

        Button checkIn = button("Check in", "success-button");
        checkIn.setOnAction(event -> runSafely(() -> {
            Reservation reservation = selectedRow(reservationTable, "Select a reservation.");
            reservationService.checkInReservation(reservation);
            refreshReservations.run();
            setStatus("Reservation #" + reservation.getId() + " checked in", true);
        }));

        Button cancel = button("Cancel", "danger-button");
        cancel.setOnAction(event -> runSafely(() -> {
            Reservation reservation = selectedRow(reservationTable, "Select a reservation.");
            reservationService.cancelReservation(reservation);
            refreshReservations.run();
            setStatus("Reservation #" + reservation.getId() + " canceled", true);
        }));

        GridPane form = formGrid();
        form.add(new Label("Branch"), 0, 0);
        form.add(branchCombo, 1, 0, 2, 1);
        form.add(new Label("People"), 0, 1);
        form.add(peopleSpinner, 1, 1);
        form.add(new Label("Date"), 0, 2);
        form.add(datePicker, 1, 2);
        form.add(new Label("Time"), 0, 3);
        form.add(timeField, 1, 3);
        form.add(findTables, 2, 3);
        form.add(new Label("Table"), 0, 4);
        form.add(tableCombo, 1, 4, 2, 1);
        form.add(new Label("Name"), 0, 5);
        form.add(nameField, 1, 5, 2, 1);
        form.add(new Label("Phone"), 0, 6);
        form.add(phoneField, 1, 6, 2, 1);
        form.add(new Label("Email"), 0, 7);
        form.add(emailField, 1, 7, 2, 1);
        form.add(new Label("Notes"), 0, 8);
        form.add(notesArea, 1, 8, 2, 1);
        form.add(create, 1, 9, 2, 1);

        VBox left = sidePanel("Reservations", "/images/reservations.jpg", form);
        VBox right = tablePanel("Reservations", reservationTable, new HBox(10, checkIn, cancel));
        HBox content = split(left, right);
        loadBranches(branchCombo);
        refreshReservations.run();
        return createTab("Reservations", content);
    }

    private Tab createOrdersTab() {
        ComboBox<Branch> branchCombo = branchComboBox();
        ComboBox<RestaurantTable> tableCombo = new ComboBox<>();
        ComboBox<MenuItem> itemCombo = new ComboBox<>();
        Spinner<Integer> seatSpinner = new Spinner<>(1, 20, 1);
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 20, 1);
        seatSpinner.setEditable(true);
        quantitySpinner.setEditable(true);

        TableView<Order> ordersTable = new TableView<>();
        ordersTable.getColumns().addAll(
                column("Order", order -> "#" + order.getId()),
                column("Table", Order::getTableCode),
                column("Status", order -> order.getStatus().name()),
                column("Total", order -> money(order.getTotal()))
        );
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableView<MealItem> itemsTable = new TableView<>();
        itemsTable.getColumns().addAll(
                column("Seat", item -> String.valueOf(item.getSeatNumber())),
                column("Item", MealItem::getItemName),
                column("Qty", item -> String.valueOf(item.getQuantity())),
                column("Total", item -> money(item.getLineTotal()))
        );
        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        itemsTable.setPrefHeight(170);

        TableView<KitchenTicket> kitchenTable = new TableView<>();
        kitchenTable.getColumns().addAll(
                column("Order", ticket -> "#" + ticket.getOrderId()),
                column("Table", KitchenTicket::getTableCode),
                column("Status", ticket -> ticket.getStatus().name()),
                column("Items", KitchenTicket::getItemSummary)
        );
        kitchenTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        Runnable refreshOrders = () -> runSafely(() -> {
            ordersTable.setItems(FXCollections.observableArrayList(orderService.findActiveOrders()));
            kitchenTable.setItems(FXCollections.observableArrayList(kitchenService.findKitchenTickets()));
            itemsTable.getItems().clear();
            setStatus("Orders refreshed", true);
        });

        Runnable loadBranchData = () -> runSafely(() -> {
            Branch branch = selected(branchCombo, "Choose a branch.");
            tableCombo.setItems(FXCollections.observableArrayList(tableService.findTablesByBranch(branch.getId())));
            itemCombo.setItems(FXCollections.observableArrayList(menuService.findAvailableItemsByBranch(branch.getId())));
            if (!tableCombo.getItems().isEmpty()) {
                tableCombo.getSelectionModel().selectFirst();
            }
            if (!itemCombo.getItems().isEmpty()) {
                itemCombo.getSelectionModel().selectFirst();
            }
        });

        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                itemsTable.getItems().clear();
            } else {
                runSafely(() -> itemsTable.setItems(FXCollections.observableArrayList(orderService.findItemsByOrder(newValue.getId()))));
            }
        });

        branchCombo.valueProperty().addListener((obs, oldValue, newValue) -> loadBranchData.run());

        Button createOrder = button("Create order", "primary-button");
        createOrder.setOnAction(event -> runSafely(() -> {
            Branch branch = selected(branchCombo, "Choose a branch.");
            RestaurantTable table = selected(tableCombo, "Choose a table.");
            int id = orderService.createOrder(branch.getId(), table.getId());
            refreshOrders.run();
            setStatus("Order #" + id + " created", true);
        }));

        Button addItem = button("Add item", "secondary-button");
        addItem.setOnAction(event -> runSafely(() -> {
            Order order = selectedRow(ordersTable, "Select an order.");
            MenuItem item = selected(itemCombo, "Choose a menu item.");
            orderService.addItem(order.getId(), seatSpinner.getValue(), item.getId(), quantitySpinner.getValue());
            ordersTable.getSelectionModel().select(order);
            refreshOrders.run();
            setStatus(item.getName() + " added to order #" + order.getId(), true);
        }));

        Button sendToKitchen = button("Send to kitchen", "primary-button");
        sendToKitchen.setOnAction(event -> runSafely(() -> {
            Order order = selectedRow(ordersTable, "Select an order.");
            orderService.updateStatus(order.getId(), OrderStatus.PREPARING);
            refreshOrders.run();
            setStatus("Order #" + order.getId() + " sent to kitchen", true);
        }));

        Button markReady = button("Mark ready", "success-button");
        markReady.setOnAction(event -> runSafely(() -> {
            KitchenTicket ticket = selectedRow(kitchenTable, "Select a kitchen ticket.");
            orderService.updateStatus(ticket.getOrderId(), OrderStatus.READY);
            refreshOrders.run();
            setStatus("Order #" + ticket.getOrderId() + " marked ready", true);
        }));

        GridPane form = formGrid();
        form.add(new Label("Branch"), 0, 0);
        form.add(branchCombo, 1, 0, 2, 1);
        form.add(new Label("Table"), 0, 1);
        form.add(tableCombo, 1, 1, 2, 1);
        form.add(createOrder, 1, 2, 2, 1);
        form.add(new Label("Menu item"), 0, 3);
        form.add(itemCombo, 1, 3, 2, 1);
        form.add(new Label("Seat"), 0, 4);
        form.add(seatSpinner, 1, 4);
        form.add(new Label("Qty"), 0, 5);
        form.add(quantitySpinner, 1, 5);
        form.add(new HBox(10, addItem, sendToKitchen), 1, 6, 2, 1);

        VBox left = sidePanel("Orders", "/images/orders.jpg", form);
        VBox right = tablePanel("Active orders", ordersTable, sectionTitle("Selected order items"), itemsTable, sectionTitle("Kitchen"), kitchenTable, markReady);
        HBox content = split(left, right);
        loadBranches(branchCombo);
        loadBranchData.run();
        refreshOrders.run();
        return createTab("Orders", content);
    }

    private Tab createBillingTab() {
        TableView<Order> billableOrdersTable = new TableView<>();
        billableOrdersTable.getColumns().addAll(
                column("Order", order -> "#" + order.getId()),
                column("Table", Order::getTableCode),
                column("Status", order -> order.getStatus().name()),
                column("Total", order -> money(order.getTotal()))
        );
        billableOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableView<Bill> billsTable = new TableView<>();
        billsTable.getColumns().addAll(
                column("Bill", bill -> "#" + bill.getId()),
                column("Order", bill -> "#" + bill.getOrderId()),
                column("Table", Bill::getTableCode),
                column("Total", bill -> money(bill.getTotal())),
                column("Status", bill -> bill.getPaymentStatus().name())
        );
        billsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableView<BillItem> billItemsTable = new TableView<>();
        billItemsTable.getColumns().addAll(
                column("Item", BillItem::getItemName),
                column("Qty", item -> String.valueOf(item.getQuantity())),
                column("Total", item -> money(item.getLineTotal()))
        );
        billItemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        billItemsTable.setPrefHeight(150);

        ComboBox<PaymentMethod> methodCombo = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        methodCombo.getSelectionModel().select(PaymentMethod.CASH);
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        TextField detailsField = new TextField();
        detailsField.setPromptText("Payment details");

        Runnable refreshBilling = () -> runSafely(() -> {
            billableOrdersTable.setItems(FXCollections.observableArrayList(orderService.findBillableOrders()));
            billsTable.setItems(FXCollections.observableArrayList(billingService.findAllBills()));
            billItemsTable.getItems().clear();
            setStatus("Billing refreshed", true);
        });

        billsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                billItemsTable.getItems().clear();
            } else {
                runSafely(() -> {
                    billItemsTable.setItems(FXCollections.observableArrayList(billingService.findBillItems(newValue.getId())));
                    amountField.setText(newValue.getTotal().toPlainString());
                });
            }
        });

        Button createBill = button("Create bill", "primary-button");
        createBill.setOnAction(event -> runSafely(() -> {
            Order order = selectedRow(billableOrdersTable, "Select an order.");
            int id = billingService.createBill(order.getId());
            refreshBilling.run();
            setStatus("Bill #" + id + " created", true);
        }));

        Button payBill = button("Pay bill", "success-button");
        payBill.setOnAction(event -> runSafely(() -> {
            Bill bill = selectedRow(billsTable, "Select a bill.");
            billingService.payBill(bill, selected(methodCombo, "Choose a payment method."), readMoney(amountField.getText()), detailsField.getText().trim());
            detailsField.clear();
            refreshBilling.run();
            setStatus("Bill #" + bill.getId() + " paid", true);
        }));

        GridPane paymentForm = formGrid();
        paymentForm.add(new Label("Method"), 0, 0);
        paymentForm.add(methodCombo, 1, 0);
        paymentForm.add(new Label("Amount"), 0, 1);
        paymentForm.add(amountField, 1, 1);
        paymentForm.add(new Label("Details"), 0, 2);
        paymentForm.add(detailsField, 1, 2);
        paymentForm.add(payBill, 1, 3);

        VBox left = sidePanel("Billing", "/images/billing.jpg", billableOrdersTable, createBill);
        VBox right = tablePanel("Bills", billsTable, sectionTitle("Bill items"), billItemsTable, sectionTitle("Payment"), paymentForm);
        HBox content = split(left, right);
        refreshBilling.run();
        return createTab("Billing", content);
    }

    private Tab createTab(String title, Region content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        tab.setClosable(false);
        return tab;
    }

    private HBox split(VBox left, VBox right) {
        HBox content = new HBox(18, left, right);
        content.setPadding(new Insets(20));
        left.setPrefWidth(390);
        HBox.setHgrow(right, Priority.ALWAYS);
        return content;
    }

    private VBox sidePanel(String title, String imagePath, Region form) {
        VBox box = card();
        box.getChildren().addAll(sectionTitle(title), photo(imagePath, 340, 190), form);
        return box;
    }

    private VBox sidePanel(String title, String imagePath, Region table, Region button) {
        VBox box = card();
        box.getChildren().addAll(sectionTitle(title), photo(imagePath, 340, 190), table, button);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox tablePanel(String title, Region table, Object... extraNodes) {
        VBox box = card();
        box.getChildren().add(sectionTitle(title));
        box.getChildren().add(table);
        for (Object node : extraNodes) {
            box.getChildren().add((javafx.scene.Node) node);
        }
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private ScrollPane scroll(Region content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("page-scroll");
        return scrollPane;
    }

    private VBox card() {
        VBox box = new VBox(12);
        box.getStyleClass().add("card");
        return box;
    }

    private VBox statCard(String value, String label) {
        VBox box = card();
        box.setPrefWidth(170);
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-number");
        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-label");
        box.getChildren().addAll(valueLabel, textLabel);
        return box;
    }

    private Label sectionTitle(String value) {
        Label label = new Label(value);
        label.getStyleClass().add("section-title");
        return label;
    }

    private Label text(String value) {
        Label label = new Label(value);
        label.setWrapText(true);
        label.getStyleClass().add("body-text");
        return label;
    }

    private Button button(String value, String styleClass) {
        Button button = new Button(value);
        button.getStyleClass().add(styleClass);
        return button;
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.TOP_LEFT);
        return grid;
    }

    private ComboBox<Branch> branchComboBox() {
        ComboBox<Branch> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(240);
        return comboBox;
    }

    private void loadBranches(ComboBox<Branch> comboBox) {
        runSafely(() -> {
            comboBox.setItems(FXCollections.observableArrayList(tableService.findAllBranches()));
            if (!comboBox.getItems().isEmpty()) {
                comboBox.getSelectionModel().selectFirst();
            }
        });
    }

    private ImageView imageView(String resource, double width, double height) {
        InputStream inputStream = getClass().getResourceAsStream(resource);
        ImageView imageView = inputStream == null ? new ImageView() : new ImageView(new Image(inputStream));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }

    private ImageView photo(String resource, double width, double height) {
        ImageView view = imageView(resource, width, height);
        view.getStyleClass().add("photo");
        return view;
    }

    private <T> TableColumn<T, String> column(String title, Function<T, String> extractor) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(extractor.apply(data.getValue())));
        return column;
    }

    private LocalDateTime readDateTime(DatePicker datePicker, TextField timeField) {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        try {
            LocalTime time = LocalTime.parse(timeField.getText().trim(), TIME_FORMAT);
            return LocalDateTime.of(date, time);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Time must be like 18:30.");
        }
    }

    private BigDecimal readMoney(String value) {
        try {
            BigDecimal amount = new BigDecimal(value.trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
            return amount;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Enter a valid amount greater than zero.");
        }
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME_FORMAT.format(value);
    }

    private String money(BigDecimal value) {
        return value == null ? "$0.00" : "$" + value.setScale(2, RoundingMode.HALF_UP);
    }

    private <T> T selected(ComboBox<T> comboBox, String message) {
        T value = comboBox.getSelectionModel().getSelectedItem();
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private <T> T selectedRow(TableView<T> tableView, String message) {
        T value = tableView.getSelectionModel().getSelectedItem();
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private void runSafely(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            setStatus(errorMessage(ex), false);
        }
    }

    private String errorMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = ex.getClass().getSimpleName();
        }
        if (ex instanceof SQLException) {
            message += " Check XAMPP MySQL and import database/restaurant_db.sql.";
        }
        return message;
    }

    private void setStatus(String message, boolean ok) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().setAll(ok ? "status-ok" : "status-error");
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
