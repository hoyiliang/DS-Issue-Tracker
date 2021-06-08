import java.sql.*;

/**
 *
 * @author wy
 */
public class DBConnect {

    private static Connection connection;
    private static Statement st;
    private static ResultSet rs;
    private static final String path = "jdbc:mysql://localhost:3306/bananabun";

    public DBConnect() {
        try {
            connection = DriverManager.getConnection(path, "root", "12345678");
            st = connection.createStatement();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

        public void newProject(long ID, String Name, String IssuesArrJSONString) {
        try {
            String query = "INSERT INTO projects (ID, Name, Issues) values (\'" + ID + "\', \'" + Name + "\', \'" + IssuesArrJSONString + "\')";
            st.executeUpdate(query);
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
            String query = "SELECT * FROM projects WHERE ID = " + ID +"";
            rs = st.executeQuery(query);
            
            ID = rs.getLong("ID");
            name = rs.getString("Name");
            issues = rs.getString("Issues");
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
            String query = "UPDATE projects SET Issues = " +issuesJsonString +" WHERE ID = " +ID +"";
            rs = st.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Fail to update a project with new data.");
            System.out.println("Detail: " +e);
        }
    }
    
    public void newUser(long ID, String Username, String Password) {
        try {
            String query = "INSERT INTO projects (ID, Name, Issues) values (\'" + ID + "\', \'" + Username + "\', \'" + Password + "\')";
            rs = st.executeQuery(query);
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
            String query = "SELECT * FROM users WHERE ID = " +ID +"";
            rs = st.executeQuery(query);
            
            ID = rs.getLong("ID");
            String Username = rs.getString("Username");
            String Password = rs.getString("Password");
            
            return new User(ID,Username,Password);
        } catch (SQLException e) {
            System.out.println("Fail to find a user with specified ID.");
            System.out.println("Detail: " +e);
            return null;
        }
    }
    
    public void setUser() {
        System.out.println("No need modify user data.");
    }
    //SELECT EXISTS(SELECT * FROM yourTableName WHERE yourCondition);
    /*
    public void newReaction(int userID, int comID, int interaction) {
        try {
            String query = "INSERT INTO reaction (userID , comID, interaction) values (\'" + userID + "\', \'" + comID + "\', \'" + interaction + "\')";
            st.executeUpdate(query);
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public void newComment(int issueID, String userName, int comID, String comText, String timeStamp) {
        try {
            String query = "INSERT INTO comment (issueID, userName, comID, comText, timeStamp, reaction) values "
                    + issueID + "\'+ \'" + userName + "\', \'" + comID + "\', \'" + comText + "\', \'" + timeStamp + "\')";
            st.executeUpdate(query);
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public static void getComment(int issueID) {
        int order_num = 1;
        int happy = 0, sad = 0, angry = 0, confused = 0, thankful = 0;
        try {
            String query = "SELECT comment.comID, comment.userName, comment.timeStamp, comment.comText,"
                    + "(SELECT COUNT(interaction) FROM reaction WHERE interaction = 1 AND reaction.comID = comment.comID) as happy,"
                    + "(SELECT COUNT(interaction) FROM reaction WHERE interaction = 2 AND reaction.comID = comment.comID) as sad,"
                    +//interactions are tabulated
                    "(SELECT COUNT(interaction) FROM reaction WHERE interaction = 3 AND reaction.comID = comment.comID) as angry,"
                    + "(SELECT COUNT(interaction) FROM reaction WHERE interaction = 4 AND reaction.comID = comment.comID) as confused,"
                    + "(SELECT COUNT(interaction) FROM reaction WHERE interaction = 5 AND reaction.comID = comment.comID) as thankful"
                    + " FROM comment"//from the comment table
                    + " WHERE issueID = " + issueID + "";//whereby the issueID is the one passed through the args

            rs = st.executeQuery(query);
            System.out.println("Comments");
            System.out.println("----------");

            while (rs.next()) {
                String userName = rs.getString("userName");
                String timeStamp = rs.getString("timeStamp");
                String comText = rs.getString("comText");
                String comID = rs.getString("comID");
                happy = rs.getInt("happy");
                sad = rs.getInt("sad");
                angry = rs.getInt("angry");
                confused = rs.getInt("confused");
                thankful = rs.getInt("thankful");

                System.out.println("#" + order_num + "\t Created on: " + timeStamp 
                        + "\tBy: " + userName + "\tcomID: " + comID + "\n" + comText);
                System.out.println("Reactions: Happy (" + happy + ") | Sad (" + sad + ")"
                        + "| Angry (" + angry + ") | Confused (" + confused + ") | Thankful (" + thankful + ")");
                System.out.println("");
                order_num++;

            }
            if (order_num == 1) {
                System.out.println("No comment");
            }

        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }
    */
    /*reactionDraft
    public static void getReaction(int comID) {
        int happy = 0, sad = 0, angry = 0, confused = 0, thankful = 0;
        try {
            String query = "SELECT * FROM reaction WHERE comID = " + comID + "";
            rs = st.executeQuery(query);
            while (rs.next()) {
                int interaction = rs.getInt("interaction");
 
                switch (interaction) {
                    case 1:
                        happy++;
                        break;
                    case 2:
                        sad++;
                        break;
                    case 3:
                        angry++;
                        break;
                    case 4:
                        confused++;
                        break;
                    case 5:
                        thankful++;
                        break;
                }

            }
            System.out.println("Reactions: Happy (" +happy+ ") | Sad (" +sad+ ")"
                    + "| Angry (" +angry+ ") | Confused (" +confused+ ") | Thankful (" +thankful+ ")");
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }*/
    
    
    /*Draft for FuzzySearch
    public static String getComment(int issueID) {
        int order_num = 1;
        int happy = 0, sad = 0, angry = 0, confused = 0, thankful = 0;
        String all = "";
        try {
            String query = "SELECT comment.comID, comment.userName, comment.timeStamp, comment.comText,"
                    + "(SELECT COUNT(interaction) FROM reaction WHERE interaction = 1 AND reaction.comID = comment.comID) as happy,"
                    + "(SELECT COUNT(interaction) FROM reaction WHERE interaction = 2 AND reaction.comID = comment.comID) as sad,"
                    +//interactions are tabulated
                    "(SELECT COUNT(interaction) FROM reaction WHERE interaction = 3 AND reaction.comID = comment.comID) as angry,"
                    + "(SELECT COUNT(interaction) FROM reaction WHERE interaction = 4 AND reaction.comID = comment.comID) as confused,"
                    + "(SELECT COUNT(interaction) FROM reaction WHERE interaction = 5 AND reaction.comID = comment.comID) as thankful"
                    + " FROM comment"//from the comment table
                    + " WHERE issueID = " + issueID + "";//whereby the issueID is the one passed through the args

            rs = st.executeQuery(query);

            while (rs.next()) {
                String userName = rs.getString("userName");
                String timeStamp = rs.getString("timeStamp");
                String comText = rs.getString("comText");
                String comID = rs.getString("comID");
                happy = rs.getInt("happy");
                sad = rs.getInt("sad");
                angry = rs.getInt("angry");
                confused = rs.getInt("confused");
                thankful = rs.getInt("thankful");

                all += userName + timeStamp + comText + comID;
                order_num++;

            }

        } catch (SQLException ex) {
            System.out.println(ex);
        }
        return all;
    }
    */
}
