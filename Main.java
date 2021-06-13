package DSPack;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {

    static List<Project> projects = new ArrayList<>();
    static List<User> users = new ArrayList<>();
    static List<UndoRedo> changelogs = new ArrayList<>();
    public static Scanner sc = new Scanner(System.in);
    public static DBConnect connection = new DBConnect();
    public static Stack<UndoRedo> issueDescUndo = new Stack<>();
    public static Stack<UndoRedo> issueDescRedo = new Stack<>();
    public static Stack<UndoRedo> commentUndo = new Stack<>();
    public static Stack<UndoRedo> commentRedo = new Stack<>();
    public static Scanner input = new Scanner(System.in);
    public static String comText, timeStamp, userName;
    public static int issueID, comID, userID, interaction;
    public static String dateEdited;

    
    public static void main(String[] args) throws IOException, ParseException, FileNotFoundException {
        
        // Declare new runtime JSON data for import/export use
        JSONArray projectsArr = new JSONArray();
        JSONArray issuesArr = new JSONArray();
        JSONArray usersArr = new JSONArray();
        
        // Initial read from MySQL DB
        if (connection.getProjectSize() == 0) {     // Project data read from SQL (if necessary)

        } else {
            for (int i = 1; i <= connection.getProjectSize(); i++) {
                projects.add(connection.getProject(i));
                JSONObject newProject = new JSONObject();
                newProject.put("id", (long) projects.get(i - 1).getId());
                newProject.put("name", (String) projects.get(i - 1).getName());
                newProject.put("issues", (JSONArray) projects.get(i - 1).getIssuesArr());
                projectsArr.add(newProject);
            }
        }
        if (connection.getUserSize() == 0) {        // User data read from SQL (if necessary)

        } else {
            for (int i = 1; i <= connection.getUserSize(); i++) {
                users.add(connection.getUser(i));
                JSONObject newUser = new JSONObject();
                newUser.put("userid", (long) users.get(i - 1).getUserid());
                newUser.put("username", (String) users.get(i - 1).getUsername());
                newUser.put("password", (String) users.get(i - 1).getPassword());
                newUser.put("skey", (String) users.get(i - 1).getSecretkey());
                usersArr.add(newUser);
            }
        }
        
        // Asking user if they want to import new data through JSON file.
        System.out.print("Import JSON? (y/n): ");
        char importjsonchoice = sc.next().charAt(0);
        if (importjsonchoice == 'y' || importjsonchoice == 'Y') {       // If yes,

            // Ask user for filepath to JSON file.
            System.out.print("Specify file path (do \\\\ instead of \\): ");
            sc.nextLine();
            String filepath = sc.nextLine();

            //Read JSON file
            JSONParser jp = new JSONParser();
            JSONObject jo = (JSONObject) jp.parse(new FileReader(filepath));
            JSONArray JSONprojectsArr = (JSONArray) jo.get("projects");
            JSONArray JSONusersArr = (JSONArray) jo.get("users");
            
            //Reading changelog data
            if (jo.containsKey("changelog")) {
                JSONArray JSONchangelogArr = (JSONArray) jo.get("changelog");
                for (int i = 0; i < JSONchangelogArr.size(); i++) {
                    JSONObject changelogIndex = (JSONObject) JSONchangelogArr.get(i);

                    String proName = (String) changelogIndex.get("project_name");
                    String isuName = (String) changelogIndex.get("issue_name");
                    String prevDesc = (String) changelogIndex.get("previous_description");
                    String editDesc = (String) changelogIndex.get("edited_description");
                    long comId = (long) changelogIndex.get("comment_id");
                    String prevCom = (String) changelogIndex.get("previous_comment");
                    String editCom = (String) changelogIndex.get("edited_comment");
                    String editTime = (String) changelogIndex.get("time_edited");

                    changelogs.add(new UndoRedo(proName, isuName, prevDesc, editDesc, (int) comId, prevCom, editCom, editTime));
                }
            }

            //Adding JSON Data to runtime & update SQL data as necessary (Project)
            if (projects.size() == 0) {
                for (int i = 0; i < JSONprojectsArr.size(); i++) {
                    JSONObject projectIndex = (JSONObject) JSONprojectsArr.get(i);

                    long projectID = (long) projectIndex.get("id");
                    String projectName = (String) projectIndex.get("name");
                    issuesArr = (JSONArray) projectIndex.get("issues");

                    projectsArr.add(projectIndex);
                    projects.add(new Project(projectID, projectName, issuesArr));
                    connection.newProject(projectID, projectName, issuesArr.toJSONString());
                }
            } else {    // Project ID uses the last ID of an existing project plus 1.
                for (int i = 0; i < JSONprojectsArr.size(); i++) {
                    JSONObject projectIndex = (JSONObject) JSONprojectsArr.get(i);
                    boolean isSameName = false;

                    long projectID = (long) projectIndex.get("id");
                    String projectName = (String) projectIndex.get("name");
                    issuesArr = (JSONArray) projectIndex.get("issues");

                    // Checks if same project name already exists and update the project's data as necessary
                    for (int j = 0; j < projects.size(); j++) {
                        if (projects.get(j).getName().equals(projectName)) {
                            isSameName = true;
                            projectsArr.set(j, projectIndex);
                            projects.set(j, new Project(projectID, projectName, issuesArr));
                            connection.setProject(projectID, issuesArr.toJSONString());
                        } else {
                            JSONObject newprojectIndex = new JSONObject();
                            projectID = connection.getProjectSize() + 1;
                            newprojectIndex.put("id", (long) projectID);
                            newprojectIndex.put("name", (String) projectName);
                            newprojectIndex.put("issues", (JSONArray) issuesArr);
                            projectsArr.add(newprojectIndex);
                            projects.add(new Project(projectID, projectName, issuesArr));
                            connection.newProject(projectID, projectName, issuesArr.toJSONString());
                        }
                    }
                }
            }

            //Adding JSON Data to runtime & update SQL data as necessary (User)
            if (users.size() == 0) {
                for (int i = 0; i < JSONusersArr.size(); i++) {
                    JSONObject userIndex = (JSONObject) JSONusersArr.get(i);

                    long userID = (long) userIndex.get("userid");
                    String userName = (String) userIndex.get("username");
                    String password = (String) userIndex.get("password");
                    String SecretKey = "";

                    userIndex.put("skey", (String) SecretKey);
                    usersArr.add(userIndex);
                    users.add(new User((int) userID, userName, password, SecretKey));
                    connection.newUser(userID, userName, password, SecretKey);

                }
            } else {    // User ID uses the last ID of existing user plus 1
                for (int i = 0; i < JSONusersArr.size(); i++) {
                    JSONObject userIndex = (JSONObject) JSONusersArr.get(i);
                    boolean isSameUser = false;

                    long userID = (long) userIndex.get("userid");
                    String userName = (String) userIndex.get("username");
                    String password = (String) userIndex.get("password");
                    String SecretKey = "";

                    // Checks if username same as in any user data in runtime
                    for (int j = 0; j < users.size(); j++) {
                        if (users.get(j).getUsername().equals(userName)) {
                            isSameUser = true;
                            System.out.println("No user information has been changed due to security." +"(" +userName +")");
                        } else {
                            JSONObject newuserIndex = new JSONObject();
                            userID = connection.getUserSize() + 1;
                            newuserIndex.put("userid", userID);
                            newuserIndex.put("username", userName);
                            newuserIndex.put("password", password);
                            newuserIndex.put("skey", SecretKey);
                            usersArr.add(newuserIndex);
                            users.add(new User((int) userID, userName, password, SecretKey));
                            connection.newUser(userID, userName, password, SecretKey);
                        }
                    }
                }
            }
        // All initial data read/write is done. Runs authentication system.
            loginInterface(usersArr, projectsArr);
        } else {
            loginInterface(usersArr, projectsArr);
        }
    }

    // Visible runtime START (Authentication page)
    public static void loginInterface(JSONArray usersArr, JSONArray projectsArr) {
        
        // Declare and initialize required variables.
        String getPass = "";
        long getID = 0;
        String getUsername = "";
        String getSecretKey = "";

        // Asks user if want to login using existing account or register a new account.
        System.out.println("Authentication Required");
        System.out.println("1 - to login\n2 - to register");
        System.out.print("Input: ");
        int choiceAuth = sc.nextInt();
        sc.nextLine();

        // Verify choices and run suitable instructions.
        if (choiceAuth == 1) {      //Login
            System.out.println("==================== Please Login! ====================");
            System.out.print("Username: ");
            String username = sc.nextLine();
            boolean userExist = false;
            // Search runtime users data to determine if user exists.
            for (User user : users) {
                if (username.equals(user.getUsername())) {
                    getID = user.getUserid();
                    getPass = user.getPassword();
                    getUsername = user.getUsername();
                    getSecretKey = user.getSecretkey();
                    userExist = true;
                }
            }
            
            // If user exists,
            if (userExist == true) {    
                System.out.print("Password: ");
                String password = sc.nextLine();
                if (password.equals(getPass)) { //Login Success
                    
                    //Checks database if the user already has a secret key, if no, generates a new one and displays it to the user.
                    if (getSecretKey == null) { 
                        System.out.println("New imported user, generating new Secret Key...");
                        getSecretKey = authCode.generateSecretKey();
                        System.out.println("Your new Secret Key is: " + getSecretKey + ", Please create a new 2FA user in the website: ");
                        System.out.println("https://gauth.apps.gbraad.nl/#main");
                        connection.setUser(getID, getSecretKey);
                    } else {    //Asks user for the OTP if they have a secret key associated with their account.
                        System.out.print("Enter your OTP from the website: ");
                        String OTP = sc.nextLine();
                        if (OTP.equals(authCode.getTOTPCode(getSecretKey))) {
                            System.out.println("====================== Success! =======================");
                            User programUser = new User((int) getID, getUsername, getPass, getSecretKey);
                            projectsArr = projectBoard(projectsArr, programUser, 1);
                        } else {
                            System.out.println("====================== Failure! =======================");
                            loginInterface(usersArr, projectsArr);
                        }
                    }

                    //Changelog
                    JSONArray changeArr = new JSONArray();
                    JSONObject changelog = new JSONObject();
                    for (int i = 0; i < changelogs.size(); i++) {
                        changelog.put("project_name", changelogs.get(i).getProjectName());
                        changelog.put("issue_name", changelogs.get(i).getIssueName());
                        changelog.put("previous_description", changelogs.get(i).getOldIssueDesc());
                        changelog.put("edited_description", changelogs.get(i).getNewIssueDesc());
                        changelog.put("comment_id", changelogs.get(i).getCommentId());
                        changelog.put("previous_comment", changelogs.get(i).getOldComment());
                        changelog.put("edited_comment", changelogs.get(i).getNewComment());
                        changelog.put("time_edited", changelogs.get(i).getTime());
                        changeArr.add(changelog);

                    }

                    while (!issueDescUndo.isEmpty() || !commentUndo.isEmpty()) {
                        JSONObject newChangelog = new JSONObject();
                        if (!issueDescUndo.isEmpty()) {
                            newChangelog.put("project_name", issueDescUndo.peek().getProjectName());
                            newChangelog.put("issue_name", issueDescUndo.peek().getIssueName());
                            newChangelog.put("previous_description", issueDescUndo.peek().getOldIssueDesc());
                            newChangelog.put("edited_description", issueDescUndo.peek().getNewIssueDesc());
                            newChangelog.put("comment_id", -1);
                            newChangelog.put("previous_comment", "");
                            newChangelog.put("edited_comment", "");
                            newChangelog.put("time_edited", issueDescUndo.peek().getTime());
                            changeArr.add(newChangelog);
                            issueDescUndo.pop();
                        } else {
                            newChangelog.put("project_name", commentUndo.peek().getProjectName());
                            newChangelog.put("issue_name", commentUndo.peek().getIssueName());
                            newChangelog.put("previous_description", "");
                            newChangelog.put("edited_description", "");
                            newChangelog.put("comment_id", commentUndo.peek().getCommentId());
                            newChangelog.put("previous_comment", commentUndo.peek().getOldComment());
                            newChangelog.put("edited_comment", commentUndo.peek().getNewComment());
                            newChangelog.put("timestamp", commentUndo.peek().getTime());
                            changeArr.add(newChangelog);
                            commentUndo.pop();
                        }
                    }

                    //  Report Generation
                    String alignFormat = "|  %-13s  | %-5d |%n";
                    for (int i = 0; i < projects.size(); i++) {
                        System.out.println("Project Name: " + projects.get(i).getName());
                        System.out.println("==========================================");
                        int open = 0, resolve = 0, inProgress = 0, closed = 0;
                        for (int j = 0; j < projects.get(i).getIssuesArr().size(); j++) {
                            if (projects.get(i).getIssues().get(j).getStatus().equalsIgnoreCase("open")) {
                                open++;
                            } else if (projects.get(i).getIssues().get(j).getStatus().equalsIgnoreCase("resolved")) {
                                resolve++;
                            } else if (projects.get(i).getIssues().get(j).getStatus().equalsIgnoreCase("In Progress")) {
                                inProgress++;
                            } else if (projects.get(i).getIssues().get(j).getStatus().equalsIgnoreCase("closed")) {
                                closed++;
                            }
                        }
                        int total = open + resolve + inProgress + closed;
                        System.out.format("+-----------------+-------+%n");
                        System.out.format("| Status Category | Issue |%n");
                        System.out.format("+-----------------+-------+%n");
                        System.out.format(alignFormat, "Open", open);
                        System.out.format(alignFormat, "Resolved", resolve);
                        System.out.format(alignFormat, "In progress", inProgress);
                        System.out.format(alignFormat, "Closed", closed);
                        System.out.format("+-----------------+-------+%n");
                        System.out.format(alignFormat, "Total", total);
                        System.out.format("+-----------------+-------+%n");
                        System.out.println();
                    }

                    // Asks user if wants to export new JSON data.
                    System.out.print("Do you want to export new JSON data to a file? (y/n): ");
                    char export = sc.next().charAt(0);
                    if (export == 'y' || export == 'Y') {
                        JSONObject newjsondata = new JSONObject();
                        newjsondata.put("projects", projectsArr);
                        newjsondata.put("users", usersArr);
                        newjsondata.put("changelog", changeArr);

                        // Pretty-print JSON using GSON
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        JsonParser jp = new JsonParser();
                        JsonElement je = jp.parse(newjsondata.toJSONString());
                        String formattedJsonString = gson.toJson(je);

                        // write data to json algorithm
                        try (FileWriter file = new FileWriter("data.json")) {
                            file.write(formattedJsonString);
                            file.flush();

                        } catch (IOException ee) {
                            ee.printStackTrace();
                        }
                    }

                } else { //Login fail (wrong password)
                    System.out.println("====================== Failure! =======================");
                    loginInterface(usersArr, projectsArr);
                }
            } else { // User does not exist
                System.out.println("================ User does not exist ==================");
                loginInterface(usersArr, projectsArr);
            }
          // Registration part
        } else if (choiceAuth == 2) { 
            System.out.println("==================== Registration ====================");
            getID = connection.getUserSize() + 1;
            System.out.print("Username: ");     // Request for username
            getUsername = sc.nextLine();
            
            // Check if user is already exist in runtime/db data
            boolean userExist = false;
            for (int i=0; i<users.size(); i++) {
                if (getUsername.equals(users.get(i).getUsername())) {
                    userExist = true;
                }
            }
            if (userExist = true) { // Jump back to authentication board if user exist
                System.out.println("Username already exists, please login!");
                
                loginInterface(usersArr,projectsArr);
            } else {    // Request for password.
                System.out.print("Password: ");
                getPass = sc.nextLine();
                String SecretKey = authCode.generateSecretKey();
                users.add(new User((int) getID, getUsername, getPass, SecretKey));
                JSONObject newUser = new JSONObject();
                newUser.put("userid", (long) getID);
                newUser.put("username", (String) getUsername);
                newUser.put("password", (String) getPass);
                usersArr.add(newUser);
                connection.newUser(getID, getUsername, getPass, SecretKey);
                
                // (2FA) Display secret key to user for them to register in the website to get OTP codes in the future.
                System.out.println("================ Registration Success ================");
                System.out.println("Your Secret Key to your account for 2FA is: " + SecretKey + ", Please create a new 2FA user in the website: ");
                System.out.println("https://gauth.apps.gbraad.nl/#main");

                loginInterface(usersArr, projectsArr);
            }
        }

        System.out.println("End of Program");

    }

    public static JSONArray projectBoard(JSONArray projectsArr, User programUser, int recurseCheck) {

        if (recurseCheck == 0) {    // An insurance to ensure no infinite recursive loop.
            return projectsArr;
        } else {
            String alignFormatLeft = "| %-2d | %-16s | %-6d |%n";
            // Project Board
            System.out.println("Project board\n-------------");
            System.out.format("+----+------------------+--------+%n");
            System.out.format("| ID |   Project Name   | Issues |%n");
            System.out.format("+----+------------------+--------+%n");
            // List projects
            for (int i = 0; i < projects.size(); i++) {
                System.out.format(alignFormatLeft, projects.get(i).getId(), projects.get(i).getName(), projects.get(i).getIssues().size());
            }
            System.out.format("+----+------------------+--------+%n");

            // Project Dashboard input
            System.out.println("Enter selected project ID to check project");
            System.out.println("or 'c' to create new Project");
            System.out.println("or 'exit' to logout and shutdown the program");
            System.out.print("Input: ");
            String input = sc.next();

            // Select or Create new?
            try { //Try to parse input to integer (Select)
                
                int projectSel = Integer.parseInt(input) - 1;
                boolean confirmExitIssueCore = false;
                JSONArray projectIssues = issueCore(projects.get(projectSel), programUser, 1);  // Enters Issue Dashboard (issueCore)

                // Recurse patch to exit to ProjectBoard
                while (confirmExitIssueCore == false) {
                    System.out.print("Confirm save changes and exit to Project Board? (y/n): ");
                    char confirmation = sc.next().charAt(0);
                    if (confirmation == 'y') {
                        confirmExitIssueCore = true;
                    } else {
                        projectIssues = issueCore(projects.get(projectSel), programUser, 1);
                    }
                }

                // update Project data in all forms. (runtime, JSON, SQL)
                long id = projects.get(projectSel).getId();
                String projectName = projects.get(projectSel).getName();
                Project modifiedProjectIndex = new Project(id, projectName, projectIssues);
                projects.set(projectSel, modifiedProjectIndex);
                connection.setProject(projectSel, projectIssues.toJSONString());

                // Add project data in json syntax
                JSONObject newProjectIndex = new JSONObject();
                newProjectIndex.put("id", (long) id);
                newProjectIndex.put("name", (String) projectName);
                newProjectIndex.put("issues", (JSONArray) projectIssues);
                projectsArr.set(projectSel, newProjectIndex);

                projectsArr = projectBoard(projectsArr, programUser, recurseCheck); // Recurse project board with updated data.
                return projectsArr;     // No matter how many recurse, at the end newest data will be returned.

            } catch (NumberFormatException e) {
                // Create new project
                if (input.equalsIgnoreCase("c")) {
                    long id = projects.size() + 1;
                    System.out.print("Enter new Project name: ");
                    sc.nextLine();
                    String projectName = sc.nextLine();
                    JSONArray projectIssues = new JSONArray();

                    // Add project data in runtime
                    projects.add(new Project(id, projectName, projectIssues));
                    // Recursion issueCore START 
                    projectIssues = issueCore(projects.get(projects.size() - 1), programUser, 1);

                    // Add project data in all forms (JSON, SQL)
                    JSONObject newProject = new JSONObject();
                    newProject.put("id", (long) id);
                    newProject.put("name", (String) projectName);
                    newProject.put("issues", (JSONArray) projectIssues);
                    projectsArr.add(newProject);
                    connection.newProject(connection.getProjectSize() + 1, projectName, projectIssues.toJSONString());

                    // Recursing projectBoard with new project data
                    projectsArr = projectBoard(projectsArr, programUser, recurseCheck);

                } else if (input.equalsIgnoreCase("exit")) {
                    recurseCheck = 0;
                } else {
                    System.out.println("Unknown command.");
                    projectsArr = projectBoard(projectsArr, programUser, recurseCheck); // Recurse if unknown command, using back current latest data
                }
                return projectsArr; // Insurance to ensure newest project data is returned.
            }
        }
    }

    // Contains all about issues
    public static JSONArray issueCore(Project specificProject, User programUser, int recurseCheck) {
        JSONArray projectIssues = specificProject.getIssuesArr();   // For updating JSON data purpose later.

        if (recurseCheck == 0) { // Insurance.
            return projectIssues;
        } else {
            String alignFormatLeft = "| %-2d | %-32s | %-11s | %-18s | %-8d | %-16s | %-8s | %-10s |%n";
            // Issue board
            String multipleTagsAlign = "|    |                                  |             | %-18s |          |                  |          |            |%n";
            System.out.println("Issue board");
            System.out.format("+----+----------------------------------+-------------+--------------------+----------+------------------+----------+------------+%n");
            System.out.format("| ID |              Title               |   Status    |        Tag         | Priority |       Time       | Assignee | CreatedBy  |%n");
            System.out.format("+----+----------------------------------+-------------+--------------------+----------+------------------+----------+------------+%n");
            // List Issues
            for (int i = 0; i < specificProject.getIssues().size(); i++) {
                SimpleDateFormat FormatPattern = new SimpleDateFormat("yyyy/MM/dd hh:mm");
                String datetimeFormatted = FormatPattern.format(specificProject.getIssues().get(i).getTimestamp());
                if (specificProject.getIssues().get(i).getTags().size() > 1) {
                    System.out.format(alignFormatLeft, specificProject.getIssues().get(i).getId(), specificProject.getIssues().get(i).getTitle(), specificProject.getIssues().get(i).getStatus(), specificProject.getIssues().get(i).getTags().get(0), specificProject.getIssues().get(i).getPriority(), datetimeFormatted, specificProject.getIssues().get(i).getAssignee(), specificProject.getIssues().get(i).getCreatedBy());
                    for (int j = 1; j < specificProject.getIssues().get(i).getTags().size(); j++) {
                        System.out.format(multipleTagsAlign, specificProject.getIssues().get(i).getTags().get(j));
                    }
                    System.out.format("+----+----------------------------------+-------------+--------------------+----------+------------------+----------+------------+%n");
                } else {
                    System.out.format(alignFormatLeft, specificProject.getIssues().get(i).getId(), specificProject.getIssues().get(i).getTitle(), specificProject.getIssues().get(i).getStatus(), specificProject.getIssues().get(i).getTags().get(0), specificProject.getIssues().get(i).getPriority(), datetimeFormatted, specificProject.getIssues().get(i).getAssignee(), specificProject.getIssues().get(i).getCreatedBy());
                    System.out.format("+----+----------------------------------+-------------+--------------------+----------+------------------+----------+------------+%n");
                }
            }
            
            // Asks user if they want sort.
            int sortCheck = 0;
            System.out.println("Specify your sorting preference: ");
            System.out.println("'1'       - Sort by Priority");
            System.out.println("'2'       - Sort by Timestamps (Latest first)");
            System.out.println("otherwise - Default sorting by ID (Use the board above)");
            System.out.print("Your choice: ");
            int sortPref = sc.nextInt();
            ArrayList<Issue> UnSortedIssues = (ArrayList<Issue>) specificProject.getIssues().clone();
            if (sortPref == 2) {
                UnSortedIssues.sort(Comparator.comparingLong(Issue::getTimestampUndated).reversed());
                sortCheck = 1;
            } else if (sortPref == 1) {
                UnSortedIssues.sort(Comparator.comparingLong(Issue::getPriority).reversed());
                sortCheck = 1;
            } else {
            }

            // Print with Sorted (if necessary)
            if (sortCheck == 1) {
                System.out.println("Issue board");
                System.out.format("+----+----------------------------------+-------------+--------------------+----------+------------------+----------+------------+\n");
                System.out.format("| ID |              Title               |   Status    |        Tag         | Priority |       Time       | Assignee | CreatedBy  |\n");
                System.out.format("+----+----------------------------------+-------------+--------------------+----------+------------------+----------+------------+\n");
                // List Issues
                for (int i = 0; i < UnSortedIssues.size(); i++) {
                    SimpleDateFormat FormatPattern = new SimpleDateFormat("yyyy/MM/dd hh:mm");
                    String datetimeFormatted = FormatPattern.format(UnSortedIssues.get(i).getTimestamp());
                    if (UnSortedIssues.get(i).getTags().size() > 1) {
                        System.out.format(alignFormatLeft, UnSortedIssues.get(i).getId(), UnSortedIssues.get(i).getTitle(), UnSortedIssues.get(i).getStatus(), UnSortedIssues.get(i).getTags().get(0), UnSortedIssues.get(i).getPriority(), datetimeFormatted, UnSortedIssues.get(i).getAssignee(), UnSortedIssues.get(i).getCreatedBy());
                        for (int j = 1; j < UnSortedIssues.get(i).getTags().size(); j++) {
                            System.out.format(multipleTagsAlign, UnSortedIssues.get(i).getTags().get(j));
                        }
                        System.out.format("+----+----------------------------------+-------------+--------------------+----------+------------------+----------+------------+\n");
                    } else {
                        System.out.format(alignFormatLeft, UnSortedIssues.get(i).getId(), UnSortedIssues.get(i).getTitle(), UnSortedIssues.get(i).getStatus(), UnSortedIssues.get(i).getTags().get(0), UnSortedIssues.get(i).getPriority(), datetimeFormatted, UnSortedIssues.get(i).getAssignee(), UnSortedIssues.get(i).getCreatedBy());
                        System.out.format("+----+----------------------------------+-------------+--------------------+----------+------------------+----------+------------+\n");
                    }
                }
            }

            // Issue Dashboard inputs
            System.out.println("Enter selected issue ID to check issue");
            System.out.println("or 's' to search");
            System.out.println("or 'c' to create issue");
            System.out.println("or 'exit' to logout and shutdown the program");
            System.out.print("Input: ");
            sc.nextLine();
            String issueInput = sc.nextLine();

            //Search Issue (Fuzzy, Levenshtein algo)
            if (issueInput.equalsIgnoreCase("s")) {
                ArrayList<FuzzySearch> UnSortedSimScores = new ArrayList<>();
                String searchKey;
                double similarityScore = 0.0;
                System.out.print("Which column you want to search from? \n't' to search with title \n'd' to search with description \n'T' to search with Tags \n'c' to search for comments \nYour choice: ");
                char searchType = sc.next().charAt(0);
                sc.nextLine();
                if (searchType == 't') {
                    System.out.print("Enter keywords to search for issue title: ");
                    searchKey = sc.nextLine();
                    for (int i=0;i<specificProject.getIssues().size();i++) {
                        
                        similarityScore = FuzzySearch.matchScore(searchKey, specificProject.getIssues().get(i).getTitle());
                        
                        if (similarityScore > 0.2) { // Minimum similarity score threshold (title)
                            UnSortedSimScores.add(new FuzzySearch(similarityScore, i));
                        }
                    }
                }
                else if (searchType == 'd') {
                    System.out.print("Enter keywords to search issue by description: ");
                    searchKey = sc.nextLine();
                    for (int i=0;i<specificProject.getIssues().size();i++) {
                        
                        similarityScore = FuzzySearch.matchScore(searchKey, specificProject.getIssues().get(i).getDescriptionText());
                        
                        if (similarityScore > 0.05) { // Minimum similarity score threshold (description)
                            UnSortedSimScores.add(new FuzzySearch(similarityScore, i));
                        }
                    }
                } else if (searchType == 'T') {
                    System.out.print("Enter desired tag to search for issue: ");
                    searchKey = sc.nextLine();
                    for (int i=0;i<specificProject.getIssues().size();i++) {
                        // Checks every tags (if got multiple, resets score for each.
                            for (int j=0;j<specificProject.getIssues().get(i).getTags().size();j++) {
                                similarityScore = FuzzySearch.matchScore(searchKey, specificProject.getIssues().get(i).getTags().get(j));
                            }

                        if (similarityScore >= 0.5) { // Minimum similarity score threshold (Tags)
                            UnSortedSimScores.add(new FuzzySearch(similarityScore, i));
                        }
                    }
                } else if (searchType == 'c') {
                    System.out.print("Enter keywords to search issue by comments: ");
                    searchKey = sc.nextLine();
                    double highestSimScore = 0.0;
                    
                    for (int i=0;i<specificProject.getIssues().size();i++) {
                        highestSimScore = 0.0;
                            for (int j=0;j<specificProject.getIssues().get(i).getComments().size();j++) {
                                similarityScore = FuzzySearch.matchScore(searchKey, specificProject.getIssues().get(i).getComments().get(j).getText());
                                if (similarityScore > highestSimScore) { highestSimScore = similarityScore; }
                            }
                        
                        if (highestSimScore > 0.25) { // Minimum similarity score threshold (comment)
                            UnSortedSimScores.add(new FuzzySearch(highestSimScore, i));
                        }
                    }
                }
                
                // sort by similarity score.
                UnSortedSimScores.sort(Comparator.comparingDouble(FuzzySearch::getSimScore));
                
                // displays the search results.
                System.out.println("Search Results: (Similarity Score sorted in Descending order)\n---------------\n");
                System.out.format("+----+----------------------------------+-------------+--------------------+----------+------------------+----------+------------+\n");
                System.out.format("| ID |              Title               |   Status    |        Tag         | Priority |       Time       | Assignee | CreatedBy  |\n");
                System.out.format("+----+----------------------------------+-------------+--------------------+----------+------------------+----------+------------+\n");
                for (int i=0; i<UnSortedSimScores.size(); i++) {
                    SimpleDateFormat FormatPattern = new SimpleDateFormat("yyyy/MM/dd hh:mm");
                    String datetimeFormatted = FormatPattern.format(specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getTimestamp());
                    if (specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getTags().size() > 1) {
                        System.out.format(alignFormatLeft, specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getId(), specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getTitle(), specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getStatus(), specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getTags().get(0), specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getPriority(), datetimeFormatted, specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getAssignee(), specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getCreatedBy());
                        for (int j = 1; j < UnSortedIssues.get(i).getTags().size(); j++) {
                            System.out.format(multipleTagsAlign, specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getTags().get(j));
                        }
                        System.out.format("+----+----------------------------------+-------------+--------------------+----------+------------------+----------+------------+\n");
                    } else {
                        System.out.format(alignFormatLeft, specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getId(), specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getTitle(), specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getStatus(), specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getTags().get(0), specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getPriority(), datetimeFormatted, specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getAssignee(), specificProject.getIssues().get(UnSortedSimScores.get(i).getIssueIndex()).getCreatedBy());
                        System.out.format("+----+----------------------------------+-------------+--------------------+----------+------------------+----------+------------+\n");
                    }
                }
                
                // input without search.
                System.out.println("Enter issue ID to check issue");
                System.out.println("or 'c' to create issue");
                System.out.println("or 'exit' to logout and shutdown the program");
                System.out.print("Input: ");
                issueInput = sc.nextLine();
                
                // check exit command (search)
                if (issueInput.equalsIgnoreCase("exit")) {
                    recurseCheck = 0;
                } else {    // pass to issuePage to identify input (search)
                    projectIssues = issuePage(specificProject, programUser, issueInput, specificProject.getIssuesArr());
                }
            }
            // check exit command
            else if (issueInput.equalsIgnoreCase("exit")) {
                recurseCheck = 0;
            } else { // pass to issuePage to identify input
                projectIssues = issuePage(specificProject, programUser, issueInput, specificProject.getIssuesArr());
            }
        }
        return projectIssues; // Insurance.
    }

    public static JSONArray issuePage(Project specificProject, User programUser, String issueInput, JSONArray projectIssues) {
        //Issue page
        try { // try to parse input as a number.
            
            int issueSel = Integer.parseInt(issueInput) - 1;

            JSONArray commentsArr = specificProject.getIssues().get(issueSel).getCommentsArr(); // for updating issue json data use.
            
            // displays specific issue information
            SimpleDateFormat FormatPattern = new SimpleDateFormat("yyyy/MM/dd hh:mm");
            String datetimeFormatted = FormatPattern.format(specificProject.getIssues().get(issueSel).getTimestamp());
            System.out.println("Issue ID: " + specificProject.getIssues().get(issueSel).getId() + "\tStatus: " + specificProject.getIssues().get(issueSel).getStatus());
            System.out.println("Tag: " + specificProject.getIssues().get(issueSel).getTags().toString() + "\tPriority: " + specificProject.getIssues().get(issueSel).getPriority() + "\tCreated On: " + datetimeFormatted);
            System.out.println("==[ Title ]===========================================================");
            System.out.println(specificProject.getIssues().get(issueSel).getTitle());
            System.out.println("======================================================================");
            System.out.println("Assigned to: " + specificProject.getIssues().get(issueSel).getAssignee() + "\t\t\tCreated by: " + specificProject.getIssues().get(issueSel).getCreatedBy() + "\n");
            System.out.println("Issue Description\n-----------------");
            System.out.format(specificProject.getIssues().get(issueSel).getDescriptionText() + "\n");
            System.out.println("Comments\n---------");
            
            // formatting to display all the comments.
            for (int i = 0; i < specificProject.getIssues().get(issueSel).getComments().size(); i++) {
                System.out.println("#" + specificProject.getIssues().get(issueSel).getComments().get(i).getCommentId() + "\tCreated on: " + FormatPattern.format(specificProject.getIssues().get(issueSel).getComments().get(i).getTimestamp()) + "\tBy: " + specificProject.getIssues().get(issueSel).getComments().get(i).getUser());
                System.out.println(specificProject.getIssues().get(issueSel).getComments().get(i).getText());
                if (specificProject.getIssues().get(issueSel).getComments().get(i).getReact().size() < 3) {
                    if (specificProject.getIssues().get(issueSel).getComments().get(i).getReact().get(0).getCount() == 0 && specificProject.getIssues().get(issueSel).getComments().get(i).getReact().get(1).getCount() == 0) {
                        System.out.println("$$");
                    } else {
                        for (int j = 0; j < specificProject.getIssues().get(issueSel).getComments().get(i).getReact().size(); j++) {
                            if (specificProject.getIssues().get(issueSel).getComments().get(i).getReact().get(j).getCount() > 0) {
                                System.out.println("$$ " + specificProject.getIssues().get(issueSel).getComments().get(i).getReact().get(j).getCount() + " people react with " + specificProject.getIssues().get(issueSel).getComments().get(i).getReact().get(j).getReaction());
                            }
                        }
                    }
                } else {
                    if (specificProject.getIssues().get(issueSel).getComments().get(i).getReact().get(0).getCount() == 0 && specificProject.getIssues().get(issueSel).getComments().get(i).getReact().get(1).getCount() == 0 && specificProject.getIssues().get(issueSel).getComments().get(i).getReact().get(2).getCount() == 0) {
                        System.out.println("$$");
                    } else {
                        for (int j = 0; j < specificProject.getIssues().get(issueSel).getComments().get(i).getReact().size(); j++) {
                            if (specificProject.getIssues().get(issueSel).getComments().get(i).getReact().get(j).getCount() > 0) {
                                System.out.println("$$ " + specificProject.getIssues().get(issueSel).getComments().get(i).getReact().get(j).getCount() + " people react with " + specificProject.getIssues().get(issueSel).getComments().get(i).getReact().get(j).getReaction());
                            }
                        }
                    }
                }
                System.out.println("");
            }

            // issue page user inputs.
            System.out.println("Enter");
            System.out.println("'r' to react");
            System.out.println("'c' to comment");
            System.out.println("or 'help' for more commands: ");
            System.out.print("Input: ");
            String choiceAction = sc.nextLine();
            
            // checks the input matches any existing commands
            if (choiceAction.equalsIgnoreCase("r")) {   // react command
                System.out.println("Which comment you want to react?");
                System.out.print("Enter comment ID: ");
                int choiceCmtID = sc.nextInt() - 1;
                
                // asks reaction type
                System.out.println("What reaction you want to react with?");
                System.out.print("1-angry, 2-happy, 3-thumbsUp: ");
                int choiceReact = sc.nextInt() - 1;
                
                // if thumbsUp not exist in this issue.
                if (specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().size() < 3 && choiceReact == 2) {
                    specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().add(new React("thumbsUp", 0));
                }
                // adds count of that react
                specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().get(choiceReact).setCount(specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().get(choiceReact).getCount() + 1);

                // update json data. (reactArr)
                JSONArray reactArr = specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReactArr();
                JSONObject newReactIndex = new JSONObject();
                newReactIndex.put("reaction", (String) specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().get(choiceReact).getReaction());
                newReactIndex.put("count", (long) specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().get(choiceReact).getCount() + 1);
                if (specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReactArr().size() < 3) {
                    reactArr.add(newReactIndex);
                } else {
                    reactArr.set(choiceReact, (JSONObject) newReactIndex);
                }

                // update json data. (commentsArr)
                JSONObject newCommentIndex = new JSONObject();
                newCommentIndex.put("comment_id", (long) choiceCmtID);
                newCommentIndex.put("text", (String) specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getText());
                newCommentIndex.put("react", (JSONArray) reactArr);
                newCommentIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getTimestampUndated());
                newCommentIndex.put("user", specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getUser());
                commentsArr.set(choiceCmtID, (JSONObject) newCommentIndex);

                // update json data. (projectIssues)
                JSONObject newIssueIndex = new JSONObject();
                newIssueIndex.put("id", (long) specificProject.getIssues().get(issueSel).getId());
                newIssueIndex.put("title", (String) specificProject.getIssues().get(issueSel).getTitle());
                newIssueIndex.put("priority", (long) specificProject.getIssues().get(issueSel).getPriority());
                newIssueIndex.put("status", (String) specificProject.getIssues().get(issueSel).getStatus());
                newIssueIndex.put("tag", (JSONArray) specificProject.getIssues().get(issueSel).getTag());
                newIssueIndex.put("descriptionText", (String) specificProject.getIssues().get(issueSel).getDescriptionText());
                newIssueIndex.put("createdBy", (String) specificProject.getIssues().get(issueSel).getCreatedBy());
                newIssueIndex.put("assignee", (String) specificProject.getIssues().get(issueSel).getAssignee());
                newIssueIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getTimestampUndated());
                newIssueIndex.put("comments", (JSONArray) commentsArr);
                projectIssues.set(issueSel, newIssueIndex);

                // displays to user what they have done.
                System.out.println("You reacted to the comment with: " + specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().get(choiceReact).getReaction());
                projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                return projectIssues;

            } else if (choiceAction.equalsIgnoreCase("c")) {    // create new comment.
                // get new comment id
                int newCommentID = specificProject.getIssues().get(issueSel).getComments().size() + 1;
                
                // user enter comment text.
                System.out.println("Enter your comment text: \nHint: Use escape characters like backslash t or n to apply formatting for display\n");
                sc.nextLine();
                String newText = sc.nextLine();
                
                // fix Scanner auto escape String input
                newText = StringEscapeUtils.unescapeJava(newText);
                
                // create new reactArr json data to this new comment.
                JSONArray newReactArr = new JSONArray();
                ////////////////////////////////////////////////////////
                JSONObject newReactAngry = new JSONObject();
                newReactAngry.put("reaction", (String) "angry");
                newReactAngry.put("count", (long) 0);
                JSONObject newReactHappy = new JSONObject();
                newReactHappy.put("reaction", (String) "happy");
                newReactHappy.put("count", (long) 0);
                JSONObject newReactthumbsUp = new JSONObject();
                newReactthumbsUp.put("reaction", (String) "thumbsUp");
                newReactthumbsUp.put("count", (long) 0);

                newReactArr.add(newReactAngry);
                newReactArr.add(newReactHappy);
                newReactArr.add(newReactthumbsUp);
                ////////////////////////////////////////////////////////
                
                // other comment information needed to create new comment
                long newTimestampUndated = Instant.now().getEpochSecond();
                String user = programUser.getUsername();

                // creating new json data for comment.
                JSONObject newCommentIndex = new JSONObject();
                newCommentIndex.put("comment_id", (long) newCommentID);
                newCommentIndex.put("text", (String) newText);
                newCommentIndex.put("react", (JSONArray) newReactArr);
                newCommentIndex.put("timestamp", (long) newTimestampUndated);
                newCommentIndex.put("user", (String) user);
                commentsArr.add(newCommentIndex);

                // Updating json data for projectIssues.
                JSONObject newIssueIndex = new JSONObject();
                newIssueIndex.put("id", (long) specificProject.getIssues().get(issueSel).getId());
                newIssueIndex.put("title", (String) specificProject.getIssues().get(issueSel).getTitle());
                newIssueIndex.put("priority", (long) specificProject.getIssues().get(issueSel).getPriority());
                newIssueIndex.put("status", (String) specificProject.getIssues().get(issueSel).getStatus());
                newIssueIndex.put("tag", (JSONArray) specificProject.getIssues().get(issueSel).getTag());
                newIssueIndex.put("descriptionText", (String) specificProject.getIssues().get(issueSel).getDescriptionText());
                newIssueIndex.put("createdBy", (String) specificProject.getIssues().get(issueSel).getCreatedBy());
                newIssueIndex.put("assignee", (String) specificProject.getIssues().get(issueSel).getAssignee());
                newIssueIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getTimestampUndated());
                newIssueIndex.put("comments", (JSONArray) commentsArr);
                projectIssues.set(issueSel, newIssueIndex);

                // add in runtime data.
                specificProject.getIssues().get(issueSel).getComments().add(new Comment(newCommentID, newText, newReactArr, newTimestampUndated, user));

                System.out.println("Comment successfully posted.");
                projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues); // returns to issuePage with new data.
                return projectIssues;   // no matter what, returns newest data.

            } else if (choiceAction.equalsIgnoreCase("help")) { // displays all commands.
                System.out.println("List of all commands: ");
                System.out.println("--[ General ]-----------------------------------------------------------------------");
                System.out.println("'help'    - Displays this list");
                System.out.println("'r'       - To react a comment");
                System.out.println("'c'       - To create a new comment");
                System.out.println("'exit'    - Back to Issue Dashboard");
                System.out.println("'changes' - To see all edit histories in this issue.");
                System.out.println("--[ Requires Ownership ]------------------------------------------------------------");
                System.out.println("'edit'    - To edit this issue's description or a comment (limited to user of the issue and comment).");
                System.out.println("'open'    - To set this issue's status to be 'Open'");
                System.out.println("'progress'- To set this issue's status to be 'In Progress'");
                System.out.println("'close'   - To set this issue's status to be 'Closed'");
                System.out.println("'resolve' - To set this issue's status to be 'Resolved'");

                System.out.print("Press enter to continue...");
                sc.nextLine();
                sc.nextLine();
                projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                return projectIssues;
            
            // all about issue status (open, close, inprogress, etc.)
            } else if (choiceAction.equalsIgnoreCase("open")) {     // change status to open/reopened
                if (specificProject.getIssues().get(issueSel).getCreatedBy().equals(programUser.getUsername())) {   // Check ownership
                    if (specificProject.getIssues().get(issueSel).getStatus().equals("Open")) { // if same, return
                        System.out.println("This issue's status is currently Open!");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;
                    } else {        // if its already closed it will become reopened instead of open
                        if (specificProject.getIssues().get(issueSel).getStatus().equals("Closed")) {
                            System.out.println("Reopening Issue...");
                            specificProject.getIssues().get(issueSel).setStatus("Reopened");
                            System.out.println("Issue status set to: Reopened");
                            ;
                        } else {
                            specificProject.getIssues().get(issueSel).setStatus("Open");
                            System.out.println("Issue status set to: Open");
                        }

                        // update json data (projectIssues)
                        JSONObject newIssueIndex = new JSONObject();
                        newIssueIndex.put("id", (long) specificProject.getIssues().get(issueSel).getId());
                        newIssueIndex.put("title", (String) specificProject.getIssues().get(issueSel).getTitle());
                        newIssueIndex.put("priority", (long) specificProject.getIssues().get(issueSel).getPriority());
                        newIssueIndex.put("status", (String) specificProject.getIssues().get(issueSel).getStatus());
                        newIssueIndex.put("tag", (JSONArray) specificProject.getIssues().get(issueSel).getTag());
                        newIssueIndex.put("descriptionText", (String) specificProject.getIssues().get(issueSel).getDescriptionText());
                        newIssueIndex.put("createdBy", (String) specificProject.getIssues().get(issueSel).getCreatedBy());
                        newIssueIndex.put("assignee", (String) specificProject.getIssues().get(issueSel).getAssignee());
                        newIssueIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getTimestampUndated());
                        newIssueIndex.put("comments", (JSONArray) commentsArr);
                        projectIssues.set(issueSel, newIssueIndex);

                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;
                    }
                } else {    // reject command deny access.
                    System.out.println("You do not have permission. (not issue owner)");
                    projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                    return projectIssues;
                }

            } else if (choiceAction.equalsIgnoreCase("progress")) { // change status to in progress
                // check ownership/assignee
                if (specificProject.getIssues().get(issueSel).getCreatedBy().equals(programUser.getUsername()) || specificProject.getIssues().get(issueSel).getAssignee().equals(programUser.getUsername())) {
                    if (specificProject.getIssues().get(issueSel).getStatus().equals("In Progress")) {  // if same, return
                        System.out.println("This issue's status is currently In Progress!");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;
                    } else {
                        // update runtime data
                        specificProject.getIssues().get(issueSel).setStatus("In Progress");
                        
                        // update json data
                        JSONObject newIssueIndex = new JSONObject();
                        newIssueIndex.put("id", (long) specificProject.getIssues().get(issueSel).getId());
                        newIssueIndex.put("title", (String) specificProject.getIssues().get(issueSel).getTitle());
                        newIssueIndex.put("priority", (long) specificProject.getIssues().get(issueSel).getPriority());
                        newIssueIndex.put("status", (String) specificProject.getIssues().get(issueSel).getStatus());
                        newIssueIndex.put("tag", (JSONArray) specificProject.getIssues().get(issueSel).getTag());
                        newIssueIndex.put("descriptionText", (String) specificProject.getIssues().get(issueSel).getDescriptionText());
                        newIssueIndex.put("createdBy", (String) specificProject.getIssues().get(issueSel).getCreatedBy());
                        newIssueIndex.put("assignee", (String) specificProject.getIssues().get(issueSel).getAssignee());
                        newIssueIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getTimestampUndated());
                        newIssueIndex.put("comments", (JSONArray) commentsArr);
                        projectIssues.set(issueSel, newIssueIndex);

                        System.out.println("Issue status set to: In Progress");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;

                    }
                } else {    // deny access.
                    System.out.println("You do not have permission. (not issue owner nor assignee)");
                    projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                    return projectIssues;
                }

            } else if (choiceAction.equalsIgnoreCase("close")) {    // change status to close
                if (specificProject.getIssues().get(issueSel).getCreatedBy().equals(programUser.getUsername())) {   // check ownership
                    if (specificProject.getIssues().get(issueSel).getStatus().equals("Close")) {    //if same, return.
                        System.out.println("This issue's status is currently Closed!");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;
                    } else {
                        // update runtime data
                        specificProject.getIssues().get(issueSel).setStatus("Closed");

                        // update json data
                        JSONObject newIssueIndex = new JSONObject();
                        newIssueIndex.put("id", (long) specificProject.getIssues().get(issueSel).getId());
                        newIssueIndex.put("title", (String) specificProject.getIssues().get(issueSel).getTitle());
                        newIssueIndex.put("priority", (long) specificProject.getIssues().get(issueSel).getPriority());
                        newIssueIndex.put("status", (String) specificProject.getIssues().get(issueSel).getStatus());
                        newIssueIndex.put("tag", (JSONArray) specificProject.getIssues().get(issueSel).getTag());
                        newIssueIndex.put("descriptionText", (String) specificProject.getIssues().get(issueSel).getDescriptionText());
                        newIssueIndex.put("createdBy", (String) specificProject.getIssues().get(issueSel).getCreatedBy());
                        newIssueIndex.put("assignee", (String) specificProject.getIssues().get(issueSel).getAssignee());
                        newIssueIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getTimestampUndated());
                        newIssueIndex.put("comments", (JSONArray) commentsArr);
                        projectIssues.set(issueSel, newIssueIndex);

                        System.out.println("Issue status set to: Closed");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;

                    }
                } else {    // deny access.
                    System.out.println("You do not have permission. (not issue owner)");
                    projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                    return projectIssues;
                }

            } else if (choiceAction.equalsIgnoreCase("resolve")) {  // change status to resolved.
                // check ownership/assignee
                if (specificProject.getIssues().get(issueSel).getCreatedBy().equals(programUser.getUsername()) || specificProject.getIssues().get(issueSel).getAssignee().equals(programUser.getUsername())) {
                    if (specificProject.getIssues().get(issueSel).getStatus().equals("Resolved")) { //if same, return
                        System.out.println("This issue's status is currently Resolved!");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;
                    } else {
                        // update runtime data
                        specificProject.getIssues().get(issueSel).setStatus("Resolved");

                        // update json data
                        JSONObject newIssueIndex = new JSONObject();
                        newIssueIndex.put("id", (long) specificProject.getIssues().get(issueSel).getId());
                        newIssueIndex.put("title", (String) specificProject.getIssues().get(issueSel).getTitle());
                        newIssueIndex.put("priority", (long) specificProject.getIssues().get(issueSel).getPriority());
                        newIssueIndex.put("status", (String) specificProject.getIssues().get(issueSel).getStatus());
                        newIssueIndex.put("tag", (JSONArray) specificProject.getIssues().get(issueSel).getTag());
                        newIssueIndex.put("descriptionText", (String) specificProject.getIssues().get(issueSel).getDescriptionText());
                        newIssueIndex.put("createdBy", (String) specificProject.getIssues().get(issueSel).getCreatedBy());
                        newIssueIndex.put("assignee", (String) specificProject.getIssues().get(issueSel).getAssignee());
                        newIssueIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getTimestampUndated());
                        newIssueIndex.put("comments", (JSONArray) commentsArr);
                        projectIssues.set(issueSel, newIssueIndex);

                        System.out.println("Issue status set to: Resolved");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;

                    }
                } else {    //deny access.
                    System.out.println("You do not have permission. (not issue owner nor assignee)");
                    projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                    return projectIssues;
                }

            } else if (choiceAction.equalsIgnoreCase("edit")) { // edit description or comment
                dateEdited = new SimpleDateFormat("yyyy.MM.dd.HH:mm").format(new java.util.Date());
                
                //asks what to edit
                System.out.println("What do you want to edit in this issue?");
                System.out.println("1-Issue Desc, 2-Comments");
                int choiceEdit = sc.nextInt();
                
                // edit issue description
                if (choiceEdit == 1) {
                    if (specificProject.getIssues().get(issueSel).getCreatedBy().equals(programUser.getUsername())) {   // check ownership
                        // clear undo redo runtime clipboard
                        issueDescRedo.clear();
                        
                        // shows preview of old and new desc text.
                        System.out.println("Original Issue Description\n--------------------------");
                        System.out.println(specificProject.getIssues().get(issueSel).getDescriptionText());
                        String old = specificProject.getIssues().get(issueSel).getDescriptionText();
                        System.out.println("------------------------------------------------------------");
                        System.out.println("Your desired new description text: \n(Hint!: You can copy the original text from this terminal and paste in your input for easy editing)\n(Hint!: escape characters like backslash t or n can be used)\n");
                        sc.nextLine();
                        String newDescText = sc.nextLine();
                        newDescText = StringEscapeUtils.unescapeJava(newDescText);
                        specificProject.getIssues().get(issueSel).setDescriptionText(newDescText);
                        
                        // add new undo to runtime clipboard
                        issueDescUndo.push(new UndoRedo(specificProject.getName(), specificProject.getIssues().get(issueSel).getTitle(), old, newDescText, dateEdited));

                        // update json data and runtime data
                        JSONObject newIssueIndex = new JSONObject();
                        newIssueIndex.put("id", (long) specificProject.getIssues().get(issueSel).getId());
                        newIssueIndex.put("title", (String) specificProject.getIssues().get(issueSel).getTitle());
                        newIssueIndex.put("priority", (long) specificProject.getIssues().get(issueSel).getPriority());
                        newIssueIndex.put("status", (String) specificProject.getIssues().get(issueSel).getStatus());
                        newIssueIndex.put("tag", (JSONArray) specificProject.getIssues().get(issueSel).getTag());
                        newIssueIndex.put("descriptionText", (String) specificProject.getIssues().get(issueSel).getDescriptionText());
                        newIssueIndex.put("createdBy", (String) specificProject.getIssues().get(issueSel).getCreatedBy());
                        newIssueIndex.put("assignee", (String) specificProject.getIssues().get(issueSel).getAssignee());
                        newIssueIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getTimestampUndated());
                        newIssueIndex.put("comments", (JSONArray) commentsArr);
                        projectIssues.set(issueSel, newIssueIndex);

                        System.out.println("Editing successful.");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;

                    } else {    //deny access.
                        System.out.println("You do not have permission. (not issue owner)");
                        sc.nextLine();
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;
                    }
                // edit comment
                } else if (choiceEdit == 2) {
                    
                    //asks which comment to edit
                    System.out.println("Which comment you want to edit? (only comment belongs to yours is allowed)");
                    System.out.print("Comment ID: ");
                    int editCmtID = sc.nextInt() - 1;
                    if (specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getUser().equals(programUser.getUsername())) { //check ownership
                        
                        // clear undo redo comment runtime clipboard
                        commentRedo.clear();
                        
                        // display preview before and after edit.
                        System.out.println("Original comment text\n---------------------");
                        System.out.println(specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getText());
                        String oldComment = specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getText();
                        System.out.println("------------------------------------------------------------------------------");
                        System.out.println("Your desired new comment text: \n(Hint!: You can copy the original text from this terminal and paste in your input for easy editing)\n(Hint!: escape characters like backslash t or n can be used)\n");
                        sc.nextLine();
                        String newCmtText = sc.nextLine();
                        newCmtText = StringEscapeUtils.unescapeJava(newCmtText);
                        specificProject.getIssues().get(issueSel).getComments().get(editCmtID).setText(newCmtText);
                        // add undo redo comment runtime clipboard
                        commentUndo.push(new UndoRedo(specificProject.getName(), specificProject.getIssues().get(issueSel).getTitle(), editCmtID, oldComment, newCmtText, dateEdited));

                        // update json data (comments)
                        JSONObject newCommentIndex = new JSONObject();
                        newCommentIndex.put("comment_id", (long) specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getCommentId());
                        newCommentIndex.put("text", (String) specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getText());
                        newCommentIndex.put("react", (JSONArray) specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getReactArr());
                        newCommentIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getTimestampUndated());
                        newCommentIndex.put("user", (String) specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getUser());
                        commentsArr.set(editCmtID, newCommentIndex);

                        // update json data (issue)
                        JSONObject newIssueIndex = new JSONObject();
                        newIssueIndex.put("id", (long) specificProject.getIssues().get(issueSel).getId());
                        newIssueIndex.put("title", (String) specificProject.getIssues().get(issueSel).getTitle());
                        newIssueIndex.put("priority", (long) specificProject.getIssues().get(issueSel).getPriority());
                        newIssueIndex.put("status", (String) specificProject.getIssues().get(issueSel).getStatus());
                        newIssueIndex.put("tag", (JSONArray) specificProject.getIssues().get(issueSel).getTag());
                        newIssueIndex.put("descriptionText", (String) specificProject.getIssues().get(issueSel).getDescriptionText());
                        newIssueIndex.put("createdBy", (String) specificProject.getIssues().get(issueSel).getCreatedBy());
                        newIssueIndex.put("assignee", (String) specificProject.getIssues().get(issueSel).getAssignee());
                        newIssueIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getTimestampUndated());
                        newIssueIndex.put("comments", (JSONArray) commentsArr);
                        projectIssues.set(issueSel, newIssueIndex);

                        System.out.println("Editing successful.");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;
                    } else {    // deny access
                        System.out.println("You do not have permission. (not issue comment owner)");
                        sc.nextLine();
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;
                    }
                } else {    //unknown choice.
                    System.out.println("Invalid choice!");
                    projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                    return projectIssues;
                }

            } else if (choiceAction.equalsIgnoreCase("changes")) {  // runtime changelog
                // display changelog
                System.out.println("Edit history of issue description");
                System.out.println("===========================================================");
                for (int i = 0; i < issueDescUndo.size(); i++) {
                    System.out.println(issueDescUndo.get(i).toIssueString());
                }
                System.out.println();
                System.out.println();
                System.out.println("Edit history of comment");
                System.out.println("===========================================================");
                for (int i = 0; i < commentUndo.size(); i++) {
                    System.out.println(commentUndo.get(i).toCommentString());
                }
                System.out.println();
                System.out.println();
                
                // changelog user input
                System.out.println("You can undo and redo your previous change.");
                System.out.println("1 - Undo");
                System.out.println("2 - Redo");
                System.out.println("3 - Exit");
                System.out.print("Input: ");
                int choiceUR = sc.nextInt();
                
                if (choiceUR == 1) {    // undo changes
                    
                    //ask what type of changes.
                    System.out.println("1 - Issue Desc, 2 - Comments");
                    int choiceUndo = sc.nextInt();
                    if (choiceUndo == 1) {  // Issue description undo
                        if (!issueDescUndo.isEmpty()) {
                            
                            //display preview before and after
                            System.out.println("Preview of previous issue description: ");
                            System.out.println(issueDescUndo.peek().getOldIssueDesc());
                            System.out.println("Edited issue description: ");
                            System.out.println(issueDescUndo.peek().getNewIssueDesc());
                            System.out.println("Do you want to undo it ?\nPress enter to continue...");
                            sc.nextLine();
                            sc.nextLine();
                            String undoIssueText = issueDescUndo.peek().getOldIssueDesc();
                            specificProject.getIssues().get(issueSel).setDescriptionText(undoIssueText);
                            
                            //update runtime undo redo clipboard
                            issueDescRedo.push(issueDescUndo.pop());

                            // update runtime and json data
                            JSONObject newIssueIndex = new JSONObject();
                            newIssueIndex.put("id", (long) specificProject.getIssues().get(issueSel).getId());
                            newIssueIndex.put("title", (String) specificProject.getIssues().get(issueSel).getTitle());
                            newIssueIndex.put("priority", (long) specificProject.getIssues().get(issueSel).getPriority());
                            newIssueIndex.put("status", (String) specificProject.getIssues().get(issueSel).getStatus());
                            newIssueIndex.put("tag", (JSONArray) specificProject.getIssues().get(issueSel).getTag());
                            newIssueIndex.put("descriptionText", (String) specificProject.getIssues().get(issueSel).getDescriptionText());
                            newIssueIndex.put("createdBy", (String) specificProject.getIssues().get(issueSel).getCreatedBy());
                            newIssueIndex.put("assignee", (String) specificProject.getIssues().get(issueSel).getAssignee());
                            newIssueIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getTimestampUndated());
                            newIssueIndex.put("comments", (JSONArray) commentsArr);
                            projectIssues.set(issueSel, newIssueIndex);

                            projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                            return projectIssues;
                        } else {    // if changelog is empty
                            System.out.println("You have nothing to undo");
                            sc.nextLine();
                            projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                            return projectIssues;
                        }
                    } else if (choiceUndo == 2) {   // Issue comments undo.
                        if (!commentUndo.isEmpty()) {
                            
                            // display preview before and after
                            System.out.println("Preview of previous comment: ");
                            System.out.println(commentUndo.peek().getOldComment());
                            System.out.println("Edited comment: ");
                            System.out.println(commentUndo.peek().getNewComment());
                            System.out.println("Do you want to undo it ?\nPress enter to continue...");
                            sc.nextLine();
                            sc.nextLine();
                            String undoCommentText = commentUndo.peek().getOldComment();
                            int undoCommentId = commentUndo.peek().getCommentId();
                            specificProject.getIssues().get(issueSel).getComments().get(undoCommentId).setText(undoCommentText);
                            commentRedo.push(commentUndo.pop());

                            JSONObject newCommentIndex = new JSONObject();
                            newCommentIndex.put("comment_id", (long) specificProject.getIssues().get(issueSel).getComments().get(undoCommentId).getCommentId());
                            newCommentIndex.put("text", (String) specificProject.getIssues().get(issueSel).getComments().get(undoCommentId).getText());
                            newCommentIndex.put("react", (JSONArray) specificProject.getIssues().get(issueSel).getComments().get(undoCommentId).getReactArr());
                            newCommentIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getComments().get(undoCommentId).getTimestampUndated());
                            newCommentIndex.put("user", (String) specificProject.getIssues().get(issueSel).getComments().get(undoCommentId).getUser());
                            commentsArr.set(undoCommentId, newCommentIndex);

                            JSONObject newIssueIndex = new JSONObject();
                            newIssueIndex.put("id", (long) specificProject.getIssues().get(issueSel).getId());
                            newIssueIndex.put("title", (String) specificProject.getIssues().get(issueSel).getTitle());
                            newIssueIndex.put("priority", (long) specificProject.getIssues().get(issueSel).getPriority());
                            newIssueIndex.put("status", (String) specificProject.getIssues().get(issueSel).getStatus());
                            newIssueIndex.put("tag", (JSONArray) specificProject.getIssues().get(issueSel).getTag());
                            newIssueIndex.put("descriptionText", (String) specificProject.getIssues().get(issueSel).getDescriptionText());
                            newIssueIndex.put("createdBy", (String) specificProject.getIssues().get(issueSel).getCreatedBy());
                            newIssueIndex.put("assignee", (String) specificProject.getIssues().get(issueSel).getAssignee());
                            newIssueIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getTimestampUndated());
                            newIssueIndex.put("comments", (JSONArray) commentsArr);
                            projectIssues.set(issueSel, newIssueIndex);

                            projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                            return projectIssues;
                        } else {        // if changelog empty
                            System.out.println("You have nothing to undo");
                            sc.nextLine();
                            projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                            return projectIssues;
                        }
                    }
                } else if (choiceUR == 2) { //redo changes
                    
                    //ask for what type of changes
                    System.out.println("1 - Issue Desc, 2 - Comments");
                    int choiceRedo = sc.nextInt();
                    if (choiceRedo == 1) {  // Issue description
                        if (!issueDescRedo.isEmpty()) {
                            
                            // display preview before and after changes
                            System.out.println("Preview of previous issue description: ");
                            System.out.println(issueDescRedo.peek().getNewIssueDesc());
                            System.out.println("Edited issue description: ");
                            System.out.println(issueDescRedo.peek().getOldIssueDesc());
                            System.out.println("Do you want to redo it ?\nPress enter to continue...");
                            sc.nextLine();
                            sc.nextLine();
                            String redoIssueText = issueDescRedo.peek().getNewIssueDesc();
                            specificProject.getIssues().get(issueSel).setDescriptionText(redoIssueText);
                            
                            // update runtime undo redo clipboard
                            issueDescUndo.push(issueDescRedo.pop());

                            // update json and runtime data
                            JSONObject newIssueIndex = new JSONObject();
                            newIssueIndex.put("id", (long) specificProject.getIssues().get(issueSel).getId());
                            newIssueIndex.put("title", (String) specificProject.getIssues().get(issueSel).getTitle());
                            newIssueIndex.put("priority", (long) specificProject.getIssues().get(issueSel).getPriority());
                            newIssueIndex.put("status", (String) specificProject.getIssues().get(issueSel).getStatus());
                            newIssueIndex.put("tag", (JSONArray) specificProject.getIssues().get(issueSel).getTag());
                            newIssueIndex.put("descriptionText", (String) specificProject.getIssues().get(issueSel).getDescriptionText());
                            newIssueIndex.put("createdBy", (String) specificProject.getIssues().get(issueSel).getCreatedBy());
                            newIssueIndex.put("assignee", (String) specificProject.getIssues().get(issueSel).getAssignee());
                            newIssueIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getTimestampUndated());
                            newIssueIndex.put("comments", (JSONArray) commentsArr);
                            projectIssues.set(issueSel, newIssueIndex);

                            projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                            return projectIssues;

                        } else {    // if changelog empty
                            System.out.println("You have nothing to redo");
                            sc.nextLine();
                            projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                            return projectIssues;
                        }
                    } else if (choiceRedo == 2) {   // redo comment
                        if (!commentRedo.isEmpty()) {
                            
                            //preview changes before after
                            System.out.println("Preview of previous comment: ");
                            System.out.println(commentRedo.peek().getNewComment());
                            System.out.println("Edited comment: ");
                            System.out.println(commentRedo.peek().getOldComment());
                            System.out.println("Do you want to redo it ?\nPress enter to continue...");
                            sc.nextLine();
                            sc.nextLine();
                            String redoCommentText = commentRedo.peek().getNewComment();
                            int redoCommentId = commentUndo.peek().getCommentId();
                            specificProject.getIssues().get(issueSel).getComments().get(redoCommentId).setText(redoCommentText);
                            
                            // update runtime undo redo clipboard
                            commentUndo.push(commentRedo.pop());

                            // update runtime and json data
                            JSONObject newCommentIndex = new JSONObject();
                            newCommentIndex.put("comment_id", (long) specificProject.getIssues().get(issueSel).getComments().get(redoCommentId).getCommentId());
                            newCommentIndex.put("text", (String) specificProject.getIssues().get(issueSel).getComments().get(redoCommentId).getText());
                            newCommentIndex.put("react", (JSONArray) specificProject.getIssues().get(issueSel).getComments().get(redoCommentId).getReactArr());
                            newCommentIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getComments().get(redoCommentId).getTimestampUndated());
                            newCommentIndex.put("user", (String) specificProject.getIssues().get(issueSel).getComments().get(redoCommentId).getUser());
                            commentsArr.set(redoCommentId, newCommentIndex);

                            JSONObject newIssueIndex = new JSONObject();
                            newIssueIndex.put("id", (long) specificProject.getIssues().get(issueSel).getId());
                            newIssueIndex.put("title", (String) specificProject.getIssues().get(issueSel).getTitle());
                            newIssueIndex.put("priority", (long) specificProject.getIssues().get(issueSel).getPriority());
                            newIssueIndex.put("status", (String) specificProject.getIssues().get(issueSel).getStatus());
                            newIssueIndex.put("tag", (JSONArray) specificProject.getIssues().get(issueSel).getTag());
                            newIssueIndex.put("descriptionText", (String) specificProject.getIssues().get(issueSel).getDescriptionText());
                            newIssueIndex.put("createdBy", (String) specificProject.getIssues().get(issueSel).getCreatedBy());
                            newIssueIndex.put("assignee", (String) specificProject.getIssues().get(issueSel).getAssignee());
                            newIssueIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getTimestampUndated());
                            newIssueIndex.put("comments", (JSONArray) commentsArr);
                            projectIssues.set(issueSel, newIssueIndex);

                            projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                            return projectIssues;
                        } else {    // if changelog empty
                            System.out.println("You have nothing to redo");
                            sc.nextLine();
                            projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                            return projectIssues;
                        }
                    }
                } else {    // other than 1 and 2
                    projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                    return projectIssues;
                }
            } else if (choiceAction.equalsIgnoreCase("exit")) { // exit back to issue dashboard with updated data
                projectIssues = issueCore(specificProject, programUser, 1);
                return projectIssues;
            } else {    // return to issue page due to unknown command
                System.out.println("Unknown command.");
                projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                return projectIssues;
            }


        } catch (NumberFormatException eee) {   // if the issueInput is not number to select, check for other commands available.
           
            if (issueInput.equalsIgnoreCase("c")) { // Create new issue
                
                //all variables required to create a new issue
                long id = specificProject.getIssues().size() + 1;
                System.out.print("Enter new issue Title: ");
                String title = sc.nextLine();
                String status = "Open";
                
                //save issue tags from user
                System.out.print("Enter new issue Tags: (use spaces to split tags if required)");
                String tagsUnsafe = sc.nextLine();
                String[] tagsSafe = tagsUnsafe.split(" ");
                JSONArray Tags = new JSONArray();
                for (int i = 0; i < tagsSafe.length; i++) {
                    Tags.add((String) tagsSafe[i]);
                }
                //save issue priority from user
                System.out.print("Specify priority of this issue: ");
                int priority = sc.nextInt();
                long timestamp = Instant.now().getEpochSecond();
                String createdBy = programUser.getUsername();
                // set an assignee
                System.out.println("Available assignees:");
                String alignFormatLeft = "| %-2d | %-16s |%n";
                System.out.println("+----+------------------+");
                System.out.println("| ID |     Username     |");
                System.out.println("+----+------------------+");
                for (int i = 0; i < users.size(); i++) {
                    System.out.format(alignFormatLeft, users.get(i).getUserid(), users.get(i).getUsername());
                    System.out.println("+----+------------------+");
                }
                System.out.print("Choose by User ID (0 for null): ");
                int choiceAssignee = sc.nextInt() - 1;
                String Assignee = null;
                if (choiceAssignee < 0) {
                    Assignee = null;
                } else {
                    Assignee = users.get(choiceAssignee).getUsername();
                }
                long timestampUndated = Instant.now().getEpochSecond();
                JSONArray comments = new JSONArray();
                // get description text for issue
                System.out.println("Description of the issue: \n(Hint!: escape characters like backslash t or n can be used)\n");
                sc.nextLine();
                String descText = sc.nextLine();
                descText = StringEscapeUtils.unescapeJava(descText);

                // update new json data and runtime data (issues)
                JSONObject newIssueIndex = new JSONObject();
                newIssueIndex.put("id", (long) id);
                newIssueIndex.put("title", (String) title);
                newIssueIndex.put("priority", (long) priority);
                newIssueIndex.put("status", (String) status);
                newIssueIndex.put("tag", (JSONArray) Tags);
                newIssueIndex.put("descriptionText", (String) descText);
                newIssueIndex.put("createdBy", (String) createdBy);
                newIssueIndex.put("assignee", (String) Assignee);
                newIssueIndex.put("timestamp", (long) timestampUndated);
                newIssueIndex.put("comments", (JSONArray) comments);
                projectIssues.add(newIssueIndex);

                specificProject.getIssues().add(new Issue(id, title, priority, status, Tags, descText, createdBy, Assignee, timestampUndated, comments));

                System.out.println("Issue created successfully.");
                projectIssues = issueCore(specificProject, programUser, 1);

            } else if (issueInput.equalsIgnoreCase("exit")) {   // back to issue dashboard
                projectIssues = issueCore(specificProject, programUser, 0);
            } else {    // unknown command, back to issue dashboard
                System.out.println("Unknown command.");
                projectIssues = issueCore(specificProject, programUser, 1);
            }
        }
        return projectIssues;   //insurance
    }
}
