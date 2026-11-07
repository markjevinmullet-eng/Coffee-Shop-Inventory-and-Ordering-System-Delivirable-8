package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final ActivityLogDao logDao = new ActivityLogDao();
    private final Scanner scanner;
    private final User adminUser;

    public AdminMenu(UserDao userDao, Scanner scanner, User adminUser) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.adminUser = adminUser;
    }

    public void start() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=================================================");
            System.out.println("        MANAGER / ADMIN MANAGEMENT PANEL         ");
            System.out.println("=================================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role & Status");
            System.out.println("5. View System Audit Logs");
            System.out.println("6. Logout / Back to Main");
            System.out.print("Choose an option (1-6): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUserWorkflow();
                case "4" -> updateUserWorkflow();
                case "5" -> auditLogWorkflow();
                case "6" -> {
                    System.out.println("Logging out from Admin panel...");
                    back = true;
                }
                default -> System.out.println("❌ Invalid choice. Please enter 1-6.");
            }
        }
    }

    private void displayUserTable(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("\n[No user records found.]");
            return;
        }

        String format = "%-5s | %-15s | %-12s | %-10s%n";
        System.out.println("\n---------------------------------------------------------");
        System.out.printf(format, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println("---------------------------------------------------------");

        for (User u : users) {
            System.out.printf(format, u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println("---------------------------------------------------------");
    }

    private void viewAllUsers() {
        System.out.println("\n--- ALL SYSTEM USERS ---");
        List<User> users = userDao.getAllUsers();
        displayUserTable(users);
    }

    private void viewPendingUsers() {
        System.out.println("\n--- PENDING REGISTRATIONS ---");
        List<User> pending = userDao.getPendingUsers();
        displayUserTable(pending);
    }

    private void approveUserWorkflow() {
        System.out.println("\n--- APPROVE USER ACCOUNT ---");
        List<User> pending = userDao.getPendingUsers();

        if (pending.isEmpty()) {
            System.out.println("✅ There are currently no pending accounts to approve.");
            return;
        }

        displayUserTable(pending);

        int targetId = readIntInput("\nEnter User ID to approve (or enter 0 to cancel): ");
        if (targetId == 0) {
            System.out.println("Action cancelled by user.");
            return;
        }

        boolean existsInPending = pending.stream().anyMatch(u -> u.id() == targetId);
        if (!existsInPending) {
            System.out.println("❌ Error: User ID " + targetId + " was not found in the pending list.");
            return;
        }

        boolean success = userDao.approveUser(adminUser.id(), targetId);
        if (success) {
            System.out.println("✅ Success! User ID " + targetId + " status updated to 'APPROVED'.");
        } else {
            System.out.println("❌ Failed to update user status in the database.");
        }
    }

    private void updateUserWorkflow() {
        System.out.println("\n--- UPDATE USER ROLE & STATUS ---");
        List<User> users = userDao.getAllUsers();
        displayUserTable(users);

        int targetId = readIntInput("\nEnter Target User ID to update (or enter 0 to cancel): ");
        if (targetId == 0) {
            System.out.println("Action cancelled by user.");
            return;
        }

        if (targetId == adminUser.id()) {
            System.out.println("⚠️ WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        boolean userExists = users.stream().anyMatch(u -> u.id() == targetId);
        if (!userExists) {
            System.out.println("❌ Error: User ID " + targetId + " does not exist.");
            return;
        }

        String newRole = readValidRole();
        String newStatus = readValidStatus();

        boolean success = userDao.updateUserRoleAndStatus(adminUser.id(), targetId, newRole, newStatus);
        if (success) {
            System.out.println("✅ Success! User ID " + targetId + " updated to Role: " + newRole + ", Status: " + newStatus);
        } else {
            System.out.println("❌ Failed to update user in the database.");
        }
    }

    private void auditLogWorkflow() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=================================================");
            System.out.println("             SYSTEM AUDIT LOG PANEL              ");
            System.out.println("=================================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Back to Admin Menu");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> displayLogTable(logDao.getAllLogs());
                case "2" -> {
                    int userId = readIntInput("Enter User ID to filter by: ");
                    displayLogTable(logDao.getLogsByUserId(userId));
                }
                case "3" -> back = true;
                default -> System.out.println("❌ Invalid option. Please enter 1-3.");
            }
        }
    }

    private void displayLogTable(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            System.out.println("\n[No activity logs found.]");
            return;
        }

        String format = "%-5s | %-8s | %-18s | %-35s | %-20s%n";
        System.out.println("\n------------------------------------------------------------------------------------------------------------------");
        System.out.printf(format, "ID", "USER ID", "ACTION TYPE", "DESCRIPTION", "TIMESTAMP");
        System.out.println("------------------------------------------------------------------------------------------------------------------");

        for (ActivityLog l : logs) {
            String uidStr = (l.userId() > 0) ? String.valueOf(l.userId()) : "SYSTEM";
            System.out.printf(format, l.id(), uidStr, l.actionType(), l.description(), l.timestamp());
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------");
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid numeric input. Please enter numbers only.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new Role (MANAGER, CASHIER, CUSTOMER): ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (role.equals("MANAGER") || role.equals("CASHIER") || role.equals("CUSTOMER")) {
                return role;
            }
            System.out.println("❌ Invalid role. Please choose MANAGER, CASHIER, or CUSTOMER.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new Status (APPROVED, PENDING): ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("❌ Invalid status. Please choose APPROVED or PENDING.");
        }
    }
}