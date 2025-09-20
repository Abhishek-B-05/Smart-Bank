import java.sql.*;
import java.util.Scanner;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SmartBankApp {

    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASS;

    private static Connection conn;
    private static Scanner sc = new Scanner(System.in);

    // 🔹 Store logged-in user/customer/account globally
    private static int loggedInUserId = -1;
    private static int loggedInCustomerId = -1;
    private static String loggedInAccountNumber = null;
    private static String loggedInUserName = null;

    // -------------------- ENV LOADER --------------------
    static {
        Properties props = new Properties();
        String[] possiblePaths = {"banking.env", "src/banking.env"};
        boolean loaded = false;

        for (String path : possiblePaths) {
            File envFile = new File(path);
//            System.out.println("Looking for .env at: " + envFile.getAbsolutePath());

            if (envFile.exists() && envFile.isFile()) {
                try (FileInputStream fis = new FileInputStream(envFile)) {
                    props.load(fis);
                    loaded = true;
//                    System.out.println("✅ Loaded .env successfully from: " + envFile.getAbsolutePath());
                    break;
                } catch (IOException e) {
//                    System.err.println("❌ Failed to load .env from: " + envFile.getAbsolutePath());
                    e.printStackTrace();
                }
            } else {
                System.out.println("❌ File not found at: " + envFile.getAbsolutePath());
            }
        }

        if (!loaded) {
            System.err.println("❌ Could not find a valid .env file. Please ensure it exists in the project root or src folder.");
            System.exit(1);
        }

        DB_URL = props.getProperty("DB_URL");
        DB_USER = props.getProperty("DB_USER");
        DB_PASS = props.getProperty("DB_PASS");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found!", e);
        }
    }

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("✅ Connected to SmartBank Database!");

            boolean running = true;
            while (running) {
                System.out.println("\n🏦 SmartBank Main Menu:");
                System.out.println("1. Register User");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("👉 Enter choice: ");
                int choice = sc.nextInt();

                switch (choice) {
                    case 1 -> registerUser();
                    case 2 -> {
                        if (loginUser()) loggedInMenu();
                    }
                    case 3 -> {
                        running = false;
                        System.out.println("👋 Exiting SmartBank. Goodbye!");
                    }
                    default -> System.out.println("❌ Invalid choice, try again.");
                }
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Post-login menu
    private static void loggedInMenu() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n💼 SmartBank User Menu:");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. Show Balance");
            System.out.println("5. Delete My Account ❗");
            System.out.println("6. Logout");
            System.out.print("👉 Enter choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1 -> deposit();
                case 2 -> withdraw();
                case 3 -> transfer();
                case 4 -> showBalance();
                case 5 -> {
                    deleteUser();
                    loggedIn = false; // end session after deletion
                }
                case 6 -> {
                    loggedIn = false;
                    loggedInUserId = -1;
                    loggedInCustomerId = -1;
                    loggedInAccountNumber = null;
                    loggedInUserName = null;
                    System.out.println("🔒 Logged out successfully!");
                }
                default -> System.out.println("❌ Invalid choice, try again.");
            }
        }
    }

    // 🔹 1. Register new user
    private static void registerUser() {
        try {
            sc.nextLine(); // clear buffer
            System.out.print("Enter Name: ");
            String name = sc.nextLine();
            System.out.print("Enter Email: ");
            String email = sc.nextLine();
            System.out.print("Enter Password: ");
            String password = sc.nextLine();
            System.out.print("Enter Phone: ");
            String phone = sc.nextLine();
            System.out.print("Enter Address: ");
            String address = sc.nextLine();
            System.out.print("Enter Account Number: ");
            String accountNumber = sc.nextLine();

            // ✅ Check if account number already exists
            String checkSql = "SELECT account_id FROM accounts WHERE account_number = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, accountNumber);
            ResultSet checkRs = checkPs.executeQuery();

            if (checkRs.next()) {
                System.out.println("❌ Account number already exists! Please try again with a different number.");
                return;
            }

            // ✅ Insert user
            String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, 'Customer')";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int userId = rs.getInt(1);

                // ✅ Insert into customers
                String custSql = "INSERT INTO customers (user_id, phone, address) VALUES (?, ?, ?) RETURNING customer_id";
                PreparedStatement cps = conn.prepareStatement(custSql);
                cps.setInt(1, userId);
                cps.setString(2, phone);
                cps.setString(3, address);
                ResultSet custRs = cps.executeQuery();

                if (custRs.next()) {
                    int customerId = custRs.getInt("customer_id");

                    // ✅ Insert account with given account number
                    String accSql = "INSERT INTO accounts (customer_id, account_number, account_type, balance) VALUES (?, ?, 'Savings', 0.00)";
                    PreparedStatement aps = conn.prepareStatement(accSql);
                    aps.setInt(1, customerId);
                    aps.setString(2, accountNumber);
                    aps.executeUpdate();

                    System.out.println("\n✅ User registered successfully!");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
        }
    }

    // 🔹 2. Login
    private static boolean loginUser() {
        try {
            sc.nextLine();
            System.out.print("Enter Email: ");
            String email = sc.nextLine();
            System.out.print("Enter Password: ");
            String password = sc.nextLine();

            String sql = """
                    SELECT u.user_id, u.name, c.customer_id, c.phone, c.address, a.account_number
                    FROM users u
                    JOIN customers c ON u.user_id = c.user_id
                    LEFT JOIN accounts a ON c.customer_id = a.customer_id
                    WHERE u.email=? AND u.password_hash=?
                    """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                loggedInUserId = rs.getInt("user_id");
                loggedInCustomerId = rs.getInt("customer_id");
                loggedInAccountNumber = rs.getString("account_number");
                loggedInUserName = rs.getString("name");

                System.out.println("\n✅ Welcome " + loggedInUserName + "!");
                System.out.println("📞 Phone: " + rs.getString("phone"));
                System.out.println("🏠 Address: " + rs.getString("address"));
                System.out.println("💳 Account: " + (loggedInAccountNumber != null ? loggedInAccountNumber : "No account yet"));

                return true;
            } else {
                System.out.println("❌ Invalid login.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 🔹 3. Deposit
    private static void deposit() {
        if (loggedInAccountNumber == null) {
            System.out.println("❌ No account linked to this user.");
            return;
        }
        try {
            System.out.print("Enter Amount to Deposit: ");
            double amt = sc.nextDouble();

            conn.setAutoCommit(false);
            String update = "UPDATE accounts SET balance = balance + ? WHERE account_number=?";
            PreparedStatement ps = conn.prepareStatement(update);
            ps.setDouble(1, amt);
            ps.setString(2, loggedInAccountNumber);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                String log = "INSERT INTO transactions (account_id, transaction_type, amount) " +
                        "VALUES ((SELECT account_id FROM accounts WHERE account_number=?), 'Deposit', ?)";
                PreparedStatement logPs = conn.prepareStatement(log);
                logPs.setString(1, loggedInAccountNumber);
                logPs.setDouble(2, amt);
                logPs.executeUpdate();

                conn.commit();
                System.out.println("✅ Deposit successful!");
                System.out.println();
                showBalance();
            } else {
                conn.rollback();
                System.out.println("❌ Account not found.");
            }
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            try { conn.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
        }
    }

    // 🔹 4. Withdraw
    private static void withdraw() {
        if (loggedInAccountNumber == null) {
            System.out.println("❌ No account linked to this user.");
            return;
        }
        try {
            System.out.print("Enter Amount to Withdraw: ");
            double amt = sc.nextDouble();

            conn.setAutoCommit(false);

            String check = "SELECT balance FROM accounts WHERE account_number=?";
            PreparedStatement ps = conn.prepareStatement(check);
            ps.setString(1, loggedInAccountNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && rs.getDouble("balance") >= amt) {
                String update = "UPDATE accounts SET balance = balance - ? WHERE account_number=?";
                PreparedStatement upd = conn.prepareStatement(update);
                upd.setDouble(1, amt);
                upd.setString(2, loggedInAccountNumber);
                upd.executeUpdate();

                String log = "INSERT INTO transactions (account_id, transaction_type, amount) " +
                        "VALUES ((SELECT account_id FROM accounts WHERE account_number=?), 'Withdrawal', ?)";
                PreparedStatement logPs = conn.prepareStatement(log);
                logPs.setString(1, loggedInAccountNumber);
                logPs.setDouble(2, amt);
                logPs.executeUpdate();

                conn.commit();
                System.out.println("✅ Withdrawal successful!");
                System.out.println();
                showBalance();
            } else {
                conn.rollback();
                System.out.println("❌ Insufficient balance or account not found.");
            }
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            try { conn.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
        }
    }

    // 🔹 5. Transfer
    private static void transfer() {
        if (loggedInAccountNumber == null) {
            System.out.println("❌ No account linked to this user.");
            return;
        }
        try {
            System.out.print("Enter To Account: ");
            String toAcc = sc.next();
            System.out.print("Enter Amount to Transfer: ");
            double amt = sc.nextDouble();

            conn.setAutoCommit(false);

            String check = "SELECT balance FROM accounts WHERE account_number=?";
            PreparedStatement ps = conn.prepareStatement(check);
            ps.setString(1, loggedInAccountNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && rs.getDouble("balance") >= amt) {
                PreparedStatement deduct = conn.prepareStatement("UPDATE accounts SET balance=balance-? WHERE account_number=?");
                deduct.setDouble(1, amt);
                deduct.setString(2, loggedInAccountNumber);
                deduct.executeUpdate();

                PreparedStatement add = conn.prepareStatement("UPDATE accounts SET balance=balance+? WHERE account_number=?");
                add.setDouble(1, amt);
                add.setString(2, toAcc);
                add.executeUpdate();

                String log = "INSERT INTO transactions (account_id, transaction_type, amount, related_account) " +
                        "VALUES ((SELECT account_id FROM accounts WHERE account_number=?), 'Transfer', ?, " +
                        "(SELECT account_id FROM accounts WHERE account_number=?))";
                PreparedStatement logPs = conn.prepareStatement(log);
                logPs.setString(1, loggedInAccountNumber);
                logPs.setDouble(2, amt);
                logPs.setString(3, toAcc);
                logPs.executeUpdate();

                conn.commit();
                System.out.println("✅ Transfer successful!");
                System.out.println();
                showBalance();
            } else {
                conn.rollback();
                System.out.println("❌ Insufficient balance.");
            }
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            try { conn.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
        }
    }

    // 🔹 6. Show Balance
    private static void showBalance() {
        if (loggedInAccountNumber == null) {
            System.out.println("❌ No account linked to this user.");
            return;
        }
        try {
            String sql = "SELECT balance FROM accounts WHERE account_number=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, loggedInAccountNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("💰 Balance: " + rs.getDouble("balance"));
            } else {
                System.out.println("❌ Account not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 7. Delete logged-in user
    private static void deleteUser() {
        if (loggedInUserId == -1) {
            System.out.println("⚠️ You must be logged in to delete your account.");
            return;
        }

        sc.nextLine(); // clear buffer
        System.out.print("⚠️ Are you sure you want to permanently delete your account? (Y/N): ");
        String confirm = sc.nextLine().trim().toUpperCase();
        if (!confirm.equals("Y")) {
            System.out.println("❌ Deletion cancelled.");
            return;
        }

        try {
            conn.setAutoCommit(false);

            // Delete transactions first (to maintain FK constraints)
            PreparedStatement delTx = conn.prepareStatement(
                    "DELETE FROM transactions WHERE account_id IN (SELECT account_id FROM accounts WHERE customer_id=?)");
            delTx.setInt(1, loggedInCustomerId);
            delTx.executeUpdate();

            // Delete accounts
            PreparedStatement delAcc = conn.prepareStatement("DELETE FROM accounts WHERE customer_id=?");
            delAcc.setInt(1, loggedInCustomerId);
            delAcc.executeUpdate();

            // Delete customer
            PreparedStatement delCust = conn.prepareStatement("DELETE FROM customers WHERE customer_id=?");
            delCust.setInt(1, loggedInCustomerId);
            delCust.executeUpdate();

            // Delete user
            PreparedStatement delUser = conn.prepareStatement("DELETE FROM users WHERE user_id=?");
            delUser.setInt(1, loggedInUserId);
            delUser.executeUpdate();

            conn.commit();
            System.out.println("\n✅ Your account and all associated data have been deleted.");

            // clear session
            loggedInUserId = -1;
            loggedInCustomerId = -1;
            loggedInAccountNumber = null;
            loggedInUserName = null;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("❌ Rollback failed: " + rollbackEx.getMessage());
            }
            System.out.println("❌ Failed to delete user: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }
}
