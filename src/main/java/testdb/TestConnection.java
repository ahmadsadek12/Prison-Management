package testdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/prison_management?useSSL=false&serverTimezone=UTC";
        String username = "root";
        String password = "A76891114s*";

        try {
            // Explicitly load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("? Connection successful!");
            connection.close();
        } catch (SQLException e) {
            System.out.println("? Connection failed!");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("? MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }
}
