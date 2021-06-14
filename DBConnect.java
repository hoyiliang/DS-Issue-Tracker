import java.sql.*;

/**
 *
 * @author wy
 */
public class DBConnect {

    private static Connection connection;
    private static Statement st;
    private static PreparedStatement prepst;
    private static ResultSet rs;
    private static final String path = "jdbc:mysql://localhost:3306/issuetrackerdata";

    public DBConnect() {
        try {
            connection = DriverManager.getConnection(path, "root", "root");
            st = connection.createStatement();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }
    
    public void newProject(long ID, String Name, String IssuesArrJSONString) {
        try {
            prepst = connection.prepareStatement("INSERT INTO projects (ID,Name,Issues) values (?,?,?)");
            prepst.setLong(1, ID);
            prepst.setString(2, Name);
            prepst.setString(3, IssuesArrJSONString);
            prepst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    public long getProjectSize() {
        long size = 0;
        try {
            String query = "SELECT COUNT(*) FROM projects";
            rs = st.executeQuery(query);
            while (rs.next()) {
                size = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        } 
        return size;
    }
    
    public Project getProject(long ID) throws ParseException {
        JSONArray issuesArr = new JSONArray();
        JSONParser parser = new JSONParser();
        String name = ""; String issues;
        try {
            prepst = connection.prepareStatement("SELECT * FROM projects WHERE ID=?");
            prepst.setLong(1, ID);
            rs = prepst.executeQuery();
            rs.next();
            ID = rs.getLong(1);
            name = rs.getString(2);
            issues = rs.getString(3);
            issuesArr = (JSONArray) parser.parse(issues);
            
            return new Project(ID,name,issuesArr);
        } catch (SQLException | ParseException e) {
            System.out.println("Fail to find a project with specified ID");
            System.out.println("Detail: " +e);
            return null;
        }
    }
    
    public void setProject(long ID, String issuesJsonString) {
        try {
            String query = "UPDATE projects SET Issues = ? WHERE ID = ?";
            prepst = connection.prepareStatement(query);
            prepst.setString(1, issuesJsonString);
            prepst.setLong(2, ID);
            prepst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Fail to update a project with new data.");
            System.out.println("Detail: " +e);
        }
    }
    
    public void newUser(long ID, String Username, String Password, String SecretKey) {
        try {
            prepst = connection.prepareStatement("INSERT INTO users (ID,Username,Password,skey) values (?,?,?,?)");
            prepst.setLong(1, ID);
            prepst.setString(2, Username);
            prepst.setString(3, Password);
            prepst.setString(4, SecretKey);
            prepst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Fail to register new user data into MySQL database");
            System.out.println("Detail: " +e);
        }
    }
    
    public long getUserSize() {
        long size = 0;
        try {
            String query = "SELECT COUNT(*) FROM users";
            rs = st.executeQuery(query);
            while (rs.next()) {
                size = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        } 
        return size;
    }
    
    public User getUser(long ID) {
        
        try {
            prepst = connection.prepareStatement("SELECT * FROM users WHERE ID=?");
            prepst.setLong(1, ID);
            
            rs = prepst.executeQuery();
            rs.next();
            ID = rs.getLong("ID");
            String Username = rs.getString("Username");
            String Password = rs.getString("Password");
            String SecretKey = rs.getString("skey");
            
            return new User(ID,Username,Password,SecretKey);
        } catch (SQLException e) {
            System.out.println("Fail to find a user with specified ID.");
            System.out.println("Detail: " +e);
            return null;
        }
    }
    
    public void setUser(long ID, String SecretKey) {
        try {
        prepst = connection.prepareStatement("UPDATE users SET skey=? WHERE ID=?");
        prepst.setString(1, SecretKey);
        prepst.setLong(2, ID);
        prepst.executeUpdate();
        
        } catch (SQLException e) {
            System.out.println("Failed to update user's Secret Key");
            System.out.println("Detail: " +e);
        }
    }
}
