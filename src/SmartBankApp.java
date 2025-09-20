import java.sql.*;
import java.util.Scanner;

public class SmartBankApp {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/smartbank";
    private static final String DB_USER = "postgres"; // change to your DB username
    private static final String DB_PASS = "password"; // change to your DB password

    private static Connection conn;
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("✅ Connected to SmartBank Database!");

            boolean running = true;
            while (running) {
                System.out.println("\n🏦 SmartBank Menu:");
                System.out.println("1. Register User");
                System.out.println("2. Login");
                System.out.println("3. Deposit");
                System.out.println("4. Withdraw");
                System.out.println("5. Transfer");
                System.out.println("6. Show Balance");
                System.out.println("7. Exit");
                System.out.print("👉 Enter choice: ");
                int choice = sc.nextInt();

                switch (choice) {
                    case 1 -> registerUser();
                    case 2 -> loginUser();
                    case 3 -> deposit();
                    case 4 -> withdraw();
                    case 5 -> transfer();
                    case 6 -> showBalance();
                    case 7 -> {
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

            String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, 'Customer')";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password); // ⚠ You should hash the password in real apps
            ps.executeUpdate();

            System.out.println("✅ User registered successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
        }
    }

    // 🔹 2. Login
    private static void loginUser() {
        try {
            sc.nextLine();
            System.out.print("Enter Email: ");
            String email = sc.nextLine();
            System.out.print("Enter Password: ");
            String password = sc.nextLine();

            String sql = "SELECT user_id, name FROM users WHERE email=? AND password_hash=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("✅ Welcome " + rs.getString("name") + "!");
            } else {
                System.out.println("❌ Invalid login.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 3. Deposit
    private static void deposit() {
        try {
            System.out.print("Enter Account Number: ");
            String accNo = sc.next();
            System.out.print("Enter Amount to Deposit: ");
            double amt = sc.nextDouble();

            conn.setAutoCommit(false);

            String update = "UPDATE accounts SET balance = balance + ? WHERE account_number=?";
            PreparedStatement ps = conn.prepareStatement(update);
            ps.setDouble(1, amt);
            ps.setString(2, accNo);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                String log = "INSERT INTO transactions (account_id, transaction_type, amount) " +
                        "VALUES ((SELECT account_id FROM accounts WHERE account_number=?), 'Deposit', ?)";
                PreparedStatement logPs = conn.prepareStatement(log);
                logPs.setString(1, accNo);
                logPs.setDouble(2, amt);
                logPs.executeUpdate();

                conn.commit();
                System.out.println("✅ Deposit successful!");
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
        try {
            System.out.print("Enter Account Number: ");
            String accNo = sc.next();
            System.out.print("Enter Amount to Withdraw: ");
            double amt = sc.nextDouble();

            conn.setAutoCommit(false);

            String check = "SELECT balance FROM accounts WHERE account_number=?";
            PreparedStatement ps = conn.prepareStatement(check);
            ps.setString(1, accNo);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && rs.getDouble("balance") >= amt) {
                String update = "UPDATE accounts SET balance = balance - ? WHERE account_number=?";
                PreparedStatement upd = conn.prepareStatement(update);
                upd.setDouble(1, amt);
                upd.setString(2, accNo);
                upd.executeUpdate();

                String log = "INSERT INTO transactions (account_id, transaction_type, amount) " +
                        "VALUES ((SELECT account_id FROM accounts WHERE account_number=?), 'Withdrawal', ?)";
                PreparedStatement logPs = conn.prepareStatement(log);
                logPs.setString(1, accNo);
                logPs.setDouble(2, amt);
                logPs.executeUpdate();

                conn.commit();
                System.out.println("✅ Withdrawal successful!");
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
        try {
            System.out.print("Enter From Account: ");
            String fromAcc = sc.next();
            System.out.print("Enter To Account: ");
            String toAcc = sc.next();
            System.out.print("Enter Amount to Transfer: ");
            double amt = sc.nextDouble();

            conn.setAutoCommit(false);

            String check = "SELECT balance FROM accounts WHERE account_number=?";
            PreparedStatement ps = conn.prepareStatement(check);
            ps.setString(1, fromAcc);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && rs.getDouble("balance") >= amt) {
                // Deduct from sender
                PreparedStatement deduct = conn.prepareStatement("UPDATE accounts SET balance=balance-? WHERE account_number=?");
                deduct.setDouble(1, amt);
                deduct.setString(2, fromAcc);
                deduct.executeUpdate();

                // Add to receiver
                PreparedStatement add = conn.prepareStatement("UPDATE accounts SET balance=balance+? WHERE account_number=?");
                add.setDouble(1, amt);
                add.setString(2, toAcc);
                add.executeUpdate();

                // Log transaction
                String log = "INSERT INTO transactions (account_id, transaction_type, amount, related_account) " +
                        "VALUES ((SELECT account_id FROM accounts WHERE account_number=?), 'Transfer', ?, " +
                        "(SELECT account_id FROM accounts WHERE account_number=?))";
                PreparedStatement logPs = conn.prepareStatement(log);
                logPs.setString(1, fromAcc);
                logPs.setDouble(2, amt);
                logPs.setString(3, toAcc);
                logPs.executeUpdate();

                conn.commit();
                System.out.println("✅ Transfer successful!");
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

    // 🔹 6. Show Balance
    private static void showBalance() {
        try {
            System.out.print("Enter Account Number: ");
            String accNo = sc.next();

            String sql = "SELECT balance FROM accounts WHERE account_number=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, accNo);
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
}