package AssignDS;

import java.sql.*;

public class UserDatabase {
    private static Connection connect;
    private static Statement st;
    private static ResultSet rs;

    public void connect() {
        try {
            String path = "jdbc:mysql://localhost:3306/Users";

            connect = DriverManager.getConnection(path, "root", "12345678");
            st = connect.createStatement();

        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public static void getUser(int userid) {
        try {
            String query = "SELECT username, password FROM Users";
            rs = st.executeQuery(query);
            while (rs.next()) {
                String username = rs.getString("User_Name");
                String password = rs.getString("User_Password");
                System.out.println(username + password);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public static void newUser(int userid, String username, String password) {
        try {
            String query = "INSERT INTO users (userid, username, password) values "
                    + userid + "\'+ \'" + username + "\', \'" + password;
            st.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
