import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

// ============================================================
// 🧁 MODEL CLASSES
// ============================================================

class BakeryItem {
    private String name;
    private double price;
    private int quantity;

    public BakeryItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

// ---------- Singleton Database ----------
class BakeryDatabase {
    private static BakeryDatabase instance;
    private java.util.List<BakeryItem> regularItems;
    private java.util.List<BakeryItem> specialItems;

    private BakeryDatabase() {
        regularItems = new ArrayList<>(Arrays.asList(
                new BakeryItem("Bread", 2.5, 50),
                new BakeryItem("Cake", 15.0, 20),
                new BakeryItem("Cookies", 5.0, 30),
                new BakeryItem("Croissant", 3.0, 25),
                new BakeryItem("Cupcake", 4.0, 40),
                new BakeryItem("Donut", 2.0, 35),
                new BakeryItem("Muffin", 3.5, 28),
                new BakeryItem("Bagel", 2.5, 20),
                new BakeryItem("Brownie", 4.5, 18),
                new BakeryItem("Puff Pastry", 3.0, 22)
        ));

        specialItems = new ArrayList<>(Arrays.asList(
                new BakeryItem("Red Velvet Cake", 25.0, 10),
                new BakeryItem("Cheese Pastry", 20.0, 8),
                new BakeryItem("Chocolate Lava Cake", 30.0, 6),
                new BakeryItem("Fruit Tart", 22.0, 12),
                new BakeryItem("Strawberry Cheesecake", 28.0, 9),
                new BakeryItem("Macarons Box", 35.0, 5),
                new BakeryItem("Tiramisu", 27.0, 7),
                new BakeryItem("Blueberry Danish", 18.0, 10),
                new BakeryItem("Caramel Eclair", 24.0, 6),
                new BakeryItem("Premium Chocolate Cake", 40.0, 4)
        ));
    }

    public static BakeryDatabase getInstance() {
        if (instance == null)
            instance = new BakeryDatabase();
        return instance;
    }

    public java.util.List<BakeryItem> getRegularItems() { return regularItems; }
    public java.util.List<BakeryItem> getSpecialItems() { return specialItems; }
}

// ============================================================
// 🧮 STRATEGY PATTERN
// ============================================================

interface BillingStrategy {
    double calculate(double price, int qty);
}

class RegularBillingStrategy implements BillingStrategy {
    public double calculate(double price, int qty) {
        return price * qty;
    }
}

// ============================================================
// 🧭 CONTROLLER
// ============================================================

class BakeryController {
    private BakeryDatabase db = BakeryDatabase.getInstance();

    public java.util.List<BakeryItem> getRegularItems() { return db.getRegularItems(); }
    public java.util.List<BakeryItem> getSpecialItems() { return db.getSpecialItems(); }

    public void updateStock(String itemName, int newQty) {
        for (BakeryItem i : db.getRegularItems())
            if (i.getName().equalsIgnoreCase(itemName)) i.setQuantity(newQty);

        for (BakeryItem i : db.getSpecialItems())
            if (i.getName().equalsIgnoreCase(itemName)) i.setQuantity(newQty);
    }

    public double calculateTotal(java.util.List<BakeryItem> items, BillingStrategy strategy) {
        double total = 0;
        for (BakeryItem i : items) {
            total += strategy.calculate(i.getPrice(), i.getQuantity());
        }
        return total;
    }
}

// ============================================================
// ⚙️ COMMAND PATTERN
// ============================================================

interface Command {
    void execute();
}

// ============================================================
// 🎨 VIEW (WITH QUANTITY DROPDOWN & LIVE REFRESH)
// ============================================================

class BakeryView extends JFrame {
    private BakeryController controller;
    private JTable regularTable, specialTable;
    private DefaultTableModel regModel, specialModel;
    private JLabel totalLabel;

    public BakeryView(BakeryController controller) {
        this.controller = controller;
        setTitle("Sweet Delights Bakery Management System");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(createHeading(), BorderLayout.NORTH);
        add(createTables(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);

        loadItems();
        setVisible(true);
    }

    private JLabel createHeading() {
        JLabel lbl = new JLabel("Sweet Delights Bakery", JLabel.CENTER);
        lbl.setFont(new Font("Serif", Font.BOLD, 32));
        lbl.setForeground(new Color(128, 0, 64));
        return lbl;
    }

    private JPanel createTables() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Item Name", "Unit Price ($)", "Available Qty", "Purchase Qty"};
        regModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return col == 3; }
        };
        regularTable = new JTable(regModel);

        specialModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return col == 3; }
        };
        specialTable = new JTable(specialModel);

        styleTable(regularTable);
        styleTable(specialTable);

        // ✅ Add dropdown for quantity (0–10)
        JComboBox<Integer> qtyBox = new JComboBox<>();
        for (int i = 0; i <= 10; i++) qtyBox.addItem(i);
        regularTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(qtyBox));
        specialTable.getColumnModel().getColumn(3)
                .setCellEditor(new DefaultCellEditor(new JComboBox<>(new Integer[]{0,1,2,3,4,5,6,7,8,9,10})));

        panel.add(createTableTitle("Regular Items"));
        panel.add(new JScrollPane(regularTable));
        panel.add(createTableTitle("Special Items"));
        panel.add(new JScrollPane(specialTable));

        return panel;
    }

    private JLabel createTableTitle(String title) {
        JLabel lbl = new JLabel(title, JLabel.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl.setForeground(new Color(102, 0, 51));
        return lbl;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(26);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(240, 220, 240));
        table.setSelectionForeground(Color.BLACK);
        table.setBackground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(180, 140, 180));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new BorderLayout());

        totalLabel = new JLabel("Grand Total: $0.00", JLabel.CENTER);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(new Color(80, 0, 60));

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton billBtn = createButton("Generate Bill", new Color(180, 120, 180));
        JButton orderBtn = createButton("Place Order", new Color(160, 130, 180));
        JButton stockBtn = createButton("Update Stock", new Color(150, 120, 160));
        JButton exitBtn = createButton("Exit", new Color(120, 80, 140));

        billBtn.addActionListener(e -> new BillCommand(controller, this).execute());
        orderBtn.addActionListener(e -> new OrderCommand(this).execute());
        stockBtn.addActionListener(e -> new StockCommand(controller, this).execute());
        exitBtn.addActionListener(e -> System.exit(0));

        btnPanel.add(billBtn);
        btnPanel.add(orderBtn);
        btnPanel.add(stockBtn);
        btnPanel.add(exitBtn);

        panel.add(totalLabel, BorderLayout.NORTH);
        panel.add(btnPanel, BorderLayout.CENTER);
        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void loadItems() {
        regModel.setRowCount(0);
        specialModel.setRowCount(0);

        for (BakeryItem i : controller.getRegularItems())
            regModel.addRow(new Object[]{i.getName(), i.getPrice(), i.getQuantity(), 0});

        for (BakeryItem i : controller.getSpecialItems())
            specialModel.addRow(new Object[]{i.getName(), i.getPrice(), i.getQuantity(), 0});
    }

    public java.util.List<BakeryItem> getPurchasedItems() {
        java.util.List<BakeryItem> selected = new ArrayList<>();
        collectItemsFromTable(regModel, selected);
        collectItemsFromTable(specialModel, selected);
        return selected;
    }

    private void collectItemsFromTable(DefaultTableModel model, java.util.List<BakeryItem> list) {
        for (int i = 0; i < model.getRowCount(); i++) {
            String name = model.getValueAt(i, 0).toString();
            double price = Double.parseDouble(model.getValueAt(i, 1).toString());
            int qty = Integer.parseInt(model.getValueAt(i, 3).toString());
            if (qty > 0) list.add(new BakeryItem(name, price, qty));
        }
    }

    public void showBill(String customer, double total) {
        totalLabel.setText("Grand Total: $" + String.format("%.2f", total));
        JOptionPane.showMessageDialog(this,
                "Bill for " + customer + "\nTotal Amount: $" + String.format("%.2f", total),
                "Bill Generated", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}

// ============================================================
// ⚙️ COMMAND CLASSES
// ============================================================

class BillCommand implements Command {
    private BakeryController controller;
    private BakeryView view;

    public BillCommand(BakeryController c, BakeryView v) {
        this.controller = c;
        this.view = v;
    }

    public void execute() {
        String name = JOptionPane.showInputDialog("Enter Customer Name:");
        if (name == null || name.isEmpty()) return;

        java.util.List<BakeryItem> items = view.getPurchasedItems();
        double total = controller.calculateTotal(items, new RegularBillingStrategy());
        view.showBill(name, total);
    }
}

class OrderCommand implements Command {
    private BakeryView view;
    public OrderCommand(BakeryView v) { this.view = v; }

    public void execute() {
        JTextField name = new JTextField();
        JTextField address = new JTextField();
        JTextField phone = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Customer Name:")); panel.add(name);
        panel.add(new JLabel("Address:")); panel.add(address);
        panel.add(new JLabel("Phone:")); panel.add(phone);

        int result = JOptionPane.showConfirmDialog(view, panel, "Place Order",
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            view.showMessage("Order placed successfully!\nCustomer: " + name.getText());
        }
    }
}

class StockCommand implements Command {
    private BakeryController controller;
    private BakeryView view;

    public StockCommand(BakeryController c, BakeryView v) {
        this.controller = c;
        this.view = v;
    }

    public void execute() {
        java.util.List<BakeryItem> allItems = new ArrayList<>();
        allItems.addAll(controller.getRegularItems());
        allItems.addAll(controller.getSpecialItems());

        JComboBox<String> combo = new JComboBox<>();
        for (BakeryItem item : allItems)
            combo.addItem(item.getName());

        JTextField qtyField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Select Item:"));
        panel.add(combo);
        panel.add(new JLabel("Enter New Quantity:"));
        panel.add(qtyField);

        int result = JOptionPane.showConfirmDialog(view, panel,
                "Update Stock", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String itemName = combo.getSelectedItem().toString();
                int qty = Integer.parseInt(qtyField.getText());
                controller.updateStock(itemName, qty);
                view.loadItems(); // ✅ REFRESH TABLE
                view.showMessage("Stock updated successfully for " + itemName + "!");
            } catch (Exception e) {
                view.showMessage("Invalid quantity!");
            }
        }
    }
}

// ============================================================
// 🚀 MAIN CLASS
// ============================================================

public class BakeryManagementSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BakeryController controller = new BakeryController();
            new BakeryView(controller);
        });
    }
}
