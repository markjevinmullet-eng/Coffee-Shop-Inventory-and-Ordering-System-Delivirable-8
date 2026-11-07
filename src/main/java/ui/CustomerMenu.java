package com.scc.ui;

import com.scc.dao.OrderDao;
import com.scc.dao.ProductDao;
import com.scc.model.Product;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class CustomerMenu {
    private final ProductDao productDao = new ProductDao();
    private final OrderDao orderDao = new OrderDao();
    private final Scanner scanner;
    private final User customerUser;

    public CustomerMenu(Scanner scanner, User customerUser) {
        this.scanner = scanner;
        this.customerUser = customerUser;
    }

    public void start() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=============================================");
            System.out.println("           CUSTOMER ORDERING PORTAL          ");
            System.out.println("=============================================");
            System.out.println("1. Browse Coffee Shop Menu");
            System.out.println("2. Place New Order");
            System.out.println("3. Logout / Back");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> browseMenu();
                case "2" -> placeOrderWorkflow();
                case "3" -> {
                    System.out.println("Logging out from Customer portal...");
                    back = true;
                }
                default -> System.out.println("❌ Invalid choice. Please enter 1-3.");
            }
        }
    }

    private void browseMenu() {
        System.out.println("\n--- OUR MENU ---");
        List<Product> products = productDao.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("[No products available.]");
            return;
        }

        String format = "%-5s | %-22s | %-12s | ₱%-8.2f%n";
        System.out.println("---------------------------------------------------------");
        System.out.printf("%-5s | %-22s | %-12s | %-9s%n", "ID", "PRODUCT NAME", "CATEGORY", "PRICE");
        System.out.println("---------------------------------------------------------");
        for (Product p : products) {
            System.out.printf(format, p.productId(), p.name(), p.category(), p.price());
        }
        System.out.println("---------------------------------------------------------");
    }

    private void placeOrderWorkflow() {
        System.out.println("\n--- PLACE ORDER ---");
        browseMenu();

        int productId = readIntInput("Enter Product ID to purchase (0 to cancel): ");
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

        boolean success = orderDao.createOrder(0, customerUser.id(), selected.productId(), quantity);

        if (success) {
            System.out.println("✅ Order placed successfully!");
        } else {
            System.out.println("❌ Failed to place order.");
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