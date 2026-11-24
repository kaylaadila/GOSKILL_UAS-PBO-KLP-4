package goskill.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Sesuaikan USER / PASSWORD dengan MySQL di komputer kalian
    private static final String URL =
        "jdbc:mysql://localhost:3306/goskill?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
