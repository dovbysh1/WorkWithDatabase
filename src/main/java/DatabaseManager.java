import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String DATABASE_NAME = "my_database";
    private static final String USERNAME = "force";
    private static final String PASSWORD = "qmpoarysdahfg"; //

    public static void main(String[] args) {
        try {
            createDatabase();

            createTable();

            insertData();

            System.out.println("=== ВСІ КОРИСТУВАЧІ ===");
            selectAllUsers();

            deleteUser("Bob");

            System.out.println("\n=== КОРИСТУВАЧІ ПІСЛЯ ВИДАЛЕННЯ ===");
            selectAllUsers();

        } catch (SQLException e) {
            System.out.println("Помилка роботи з базою даних: " + e.getMessage());
        }
    }

    private static Connection getConnection(boolean useDatabase) throws SQLException {
        String connectionUrl = useDatabase ? URL + DATABASE_NAME : URL;
        return DriverManager.getConnection(connectionUrl, USERNAME, PASSWORD);
    }

    private static void createDatabase() throws SQLException {
        try (Connection conn = getConnection(false);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
            stmt.executeUpdate(sql);
            System.out.println("Базу даних '" + DATABASE_NAME + "' створено успішно!");
        }
    }

    private static void createTable() throws SQLException {
        try (Connection conn = getConnection(true);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL, " +
                    "age INT NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE" +
                    ")";

            stmt.executeUpdate(sql);
            System.out.println("Таблицю 'users' створено успішно!");
        }
    }

    private static void insertData() throws SQLException {
        try (Connection conn = getConnection(true)) {
            if (!isTableEmpty(conn)) {
                System.out.println("Дані вже існують в таблиці. Пропускаємо вставку.");
                return;
            }

            String sql = "INSERT INTO users (name, age, email) VALUES (?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "John");
                pstmt.setInt(2, 30);
                pstmt.setString(3, "john@example.com");
                pstmt.executeUpdate();

                pstmt.setString(1, "Alice");
                pstmt.setInt(2, 25);
                pstmt.setString(3, "alice@example.com");
                pstmt.executeUpdate();

                pstmt.setString(1, "Bob");
                pstmt.setInt(2, 35);
                pstmt.setString(3, "bob@example.com");
                pstmt.executeUpdate();

                System.out.println("Дані успішно вставлені в таблицю!");
            }
        }
    }

    private static boolean isTableEmpty(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
            return true;
        }
    }

    private static void selectAllUsers() throws SQLException {
        try (Connection conn = getConnection(true);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

            System.out.printf("%-5s %-10s %-5s %-20s%n", "ID", "Name", "Age", "Email");
            System.out.println("-----------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String email = rs.getString("email");

                System.out.printf("%-5d %-10s %-5d %-20s%n", id, name, age, email);
            }
        }
    }

    private static void deleteUser(String userName) throws SQLException {
        try (Connection conn = getConnection(true);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE name = ?")) {

            pstmt.setString(1, userName);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("\nКористувача '" + userName + "' успішно видалено!");
            } else {
                System.out.println("\nКористувача '" + userName + "' не знайдено!");
            }
        }
    }
}