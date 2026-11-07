package com.scc.ui;

import com.scc.dao.OrderDao;
import com.scc.dao.ProductDao;
import com.scc.model.Product;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class CashierMenu {
    private final ProductDao productDao = new ProductDao();
    private final OrderDao orderDao = new OrderDao();
    private final Scanner scanner;
    private final User cashierUser;

    public CashierMenu(Scanner scanner, User cashierUser) {
        this.scanner = scanner;
        this.cashierUser = cashierUser;
    }

    public void start() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=============================================");
            System.out.println("         CASHIER POS & ORDER PANEL           ");
            System.out.println("=============================================");
            System.out.println("1. View Product Inventory & Menu");
            System.out.println("2. Process Customer Order");
            System.out.println("3. Logout / Back");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewProducts();
                case "2" -> processOrderWorkflow();
                case "3" -> {
                    System.out.println("Logging out from Cashier panel...");
                    back = true;
                }
                default -> System.out.println("❌ Invalid choice. Please enter 1-3.");
            }
        }
    }

    private void viewProducts() {
        System.out.println("\n--- COFFEE SHOP MENU & INVENTORY ---");
        List<Product> products = productDao.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("[No products available.]");
            return;
        }

        String format = "%-5s | %-22s | %-12s | ₱%-8.2f | Stock: %-4d%n";
        System.out.println("-------------------------------------------------------------------");
        System.out.printf("%-5s | %-22s | %-12s | %-9s | %-8s%n", "ID", "PRODUCT NAME", "CATEGORY", "PRICE", "STOCK");
        System.out.println("-------------------------------------------------------------------");
        for (Product p : products) {
            System.out.printf(format, p.productId(), p.name(), p.category(), p.price(), p.stockQuantity());
        }
        System.out.println("-------------------------------------------------------------------");
    }

    private void processOrderWorkflow() {
        System.out.println("\n--- PROCESS ORDER ---");
        viewProducts();

        int productId = readIntInput("Enter Product ID to order (0 to cancel): ");
        if (productId == 0) return;

        List<Product> products = productDao.getAllProducts();
        Product selected = products.stream().filter(p -> p.productId() == productId).findFirst().orElse(null);

        if (selected == null) {
            System.out.println("❌ Invalid Product ID.");
            return;
        }

        int quantity = readIntInput("Enter quantity: ");
        if (quantity <= 0) {
            System.out.println("❌ Quantity must be greater than zero.");
            return;
        }

        if (quantity > selected.stockQuantity()) {
            System.out.println("❌ Insufficient stock! Available quantity: " + selected.stockQuantity());
            return;
        }

        boolean success = orderDao.createOrder(cashierUser.id(), 0, selected.productId(), quantity);

        if (success) {
            System.out.println("✅ Order successfully processed and recorded!");
        } else {
            System.out.println("❌ Failed to process order.");
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
            }
        }
    }
}