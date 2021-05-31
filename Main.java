import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import java.text.SimpleDateFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Main {

    static List<Project> projects = new ArrayList<>();
    static List<User> users = new ArrayList<>();

    //Setting up JSON READ
    public static void main(String[] args) throws IOException, ParseException, FileNotFoundException {
        JSONParser jp = new JSONParser();
        JSONObject jo = (JSONObject) jp.parse(new FileReader("C:\\Users\\yilia\\Downloads\\data.json"));
        JSONArray projectsArr = (JSONArray) jo.get("projects");
        JSONArray usersArr = (JSONArray) jo.get("users");

        for (Object o : projectsArr) {
            JSONObject projectIndex = (JSONObject) o;

            long projectID = (long) projectIndex.get("id");
            String projectName = (String) projectIndex.get("name");
            JSONArray issuesArr = (JSONArray) projectIndex.get("issues");
            projects.add(new Project(projectID, projectName, issuesArr));
        }

        for (Object o : usersArr) {
            JSONObject userIndex = (JSONObject) o;

            long userID = (long) userIndex.get("userid");
            String userName = (String) userIndex.get("username");
            String password = (String) userIndex.get("password");
            users.add(new User(userID, userName, password));
        }

        loginInterface(usersArr , projectsArr);
    }

    // Visible runtime START
    public static void loginInterface(JSONArray usersArr,JSONArray projectsArr) {
        Scanner sc = new Scanner(System.in);
        String getPass = "";
        long getID = 0;
        String getUsername = "";

        System.out.println("Authentication Required");
        System.out.println("1 - to login\n2 - to register");
        System.out.print("Input: ");
        int choiceAuth = sc.nextInt();
        sc.nextLine();

        if (choiceAuth == 1) { //Login
            System.out.println("==================== Please Login! ====================");
            System.out.print("Username: ");
            String username = sc.nextLine();
            for (User user : users) {
                if (username.equals(user.getUsername())) {
                    getID = user.getUserid();
                    getPass = user.getPassword();
                    getUsername = user.getUsername();
                }
            }
            System.out.println("Password: ");
            String password = sc.nextLine();
            if (password.equals(getPass)) { //Login Success
                System.out.println("====================== Success! =======================");
                User programUser = new User(getID, getUsername, getPass);
                projectsArr = projectBoard(projectsArr, programUser);

                JSONObject newjsondata = new JSONObject();
                newjsondata.put("projects", projectsArr);
                newjsondata.put("users", usersArr);
                // write data to json algorithm
                try (FileWriter file = new FileWriter("data.json")) {
                    file.write(newjsondata.toJSONString());
                    file.flush();

                } catch (IOException ee) {
                    ee.printStackTrace();
                }

            } else { //Login fail
                System.out.println("====================== Failure! =======================");
                loginInterface(usersArr, projectsArr);
            }
        }
        else if (choiceAuth == 2) { //Registration
            System.out.println("==================== Registration ====================");
            getID = usersArr.size();
            System.out.print("Username: "); getUsername = sc.nextLine();
            System.out.print("Password: "); getPass = sc.nextLine();
            users.add(new User(getID, getUsername, getPass));
            JSONObject newUser = new JSONObject();
            newUser.put("userid", (long) getID);
            newUser.put("username", (String) getUsername);
            newUser.put("password", (String) getPass);
            usersArr.add(newUser);
            System.out.println("================ Registration Success ================");

            loginInterface(usersArr, projectsArr);
        }

        System.out.println("End of Program");
    }

    public static JSONArray projectBoard(JSONArray projectsArr, User programUser) {
        Scanner sc = new Scanner(System.in);

        String alignFormatLeft = "| %-2d | %-16s | %-6d |%n";
        // Project Board
        System.out.println("Project board\n-------------");
        System.out.format("+----+------------------+--------+%n");
        System.out.format("| ID |   Project Name   | Issues |%n");
        System.out.format("+----+------------------+--------+%n");
        // List projects
        for (int i=0; i<projects.size(); i++) {
            System.out.format(alignFormatLeft, projects.get(i).getId(), projects.get(i).getName(), projects.get(i).getIssues().size());
        }
        System.out.format("+----+------------------+--------+%n");

        // Project Dashboard input
        System.out.println("Enter selected project ID to check project");
        System.out.println("or 'c' to create new Project");
        System.out.println("or 'exit' to logout and shutdown the program");
        System.out.print("Input: ");
        String input = sc.next();

        // Search or Create new?
        try {

            int projectSel = Integer.parseInt(input);
            JSONArray projectIssues = issueCore(projects.get(projectSel-1), programUser,1);
            long id = projects.get(projectSel-1).getId();
            String projectName = projects.get(projectSel-1).getName();
            Project modifiedProjectIndex = new Project(id, projectName, projectIssues);
            projects.set(projectSel-1, modifiedProjectIndex);

                // Add project data in json syntax
                JSONObject newProject = new JSONObject();
                newProject.put("id", (long) id);
                newProject.put("name", (String) projectName);
                newProject.put("issues", (JSONArray) projectIssues);
                projectsArr.set(projectSel-1, newProject);

                return projectsArr;

        } catch (NumberFormatException e) {
            // Create new project
            if (input.equalsIgnoreCase("c")) {
                long id = projects.size();
                System.out.print("Enter new Project name: ");
                sc.nextLine();
                String projectName = sc.nextLine();
                JSONArray projectIssues = new JSONArray();

                // Add project data in runtime
                projects.add(new Project(id, projectName, projectIssues));
                // Recursion issueBoard START
                projectIssues = issueCore(projects.get(projects.size()-1), programUser,1);

                // Add project data in json syntax
                JSONObject newProject = new JSONObject();
                newProject.put("id", (long) id);
                newProject.put("name", (String) projectName);
                newProject.put("issues", (JSONArray) projectIssues);
                projectsArr.add(newProject);

                return projectsArr;

            } else if (input.equalsIgnoreCase("exit")) {
                return projectsArr;
            }
        }
        return projectsArr;
    }

    // Contains all about issues
    public static JSONArray issueCore(Project specificProject, User programUser, int recurseCheck) {
        Scanner sc = new Scanner(System.in);
        JSONArray projectIssues = specificProject.getIssuesArr();

        if (recurseCheck == 0) {
            return projectIssues;
        } else {
            String alignFormatLeft = "| %-2d | %-23s | %-11s | %-8s | %-8d | %-16s | %-8s | %-10s |%n";
            // Issue board
            String multipleTagsAlign = "|    |                         |             | %-8s |          |                  |          |            |%n";
            System.out.println("Issue board");
            System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+%n");
            System.out.format("| ID |          Title          |   Status    |   Tag    | Priority |       Time       | Assignee | CreatedBy  |%n");
            System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+%n");
            // List Issues
            for (int i = 0; i < specificProject.getIssues().size(); i++) {
                SimpleDateFormat FormatPattern = new SimpleDateFormat("yyyy/MM/dd hh:mm");
                String datetimeFormatted = FormatPattern.format(specificProject.getIssues().get(i).getTimestamp());
                if (specificProject.getIssues().get(i).getTags().size() > 1) {
                    System.out.format(alignFormatLeft, specificProject.getIssues().get(i).getId(), specificProject.getIssues().get(i).getTitle(), specificProject.getIssues().get(i).getStatus(), specificProject.getIssues().get(i).getTags().get(0), specificProject.getIssues().get(i).getPriority(), datetimeFormatted, specificProject.getIssues().get(i).getAssignee(), specificProject.getIssues().get(i).getCreatedBy());
                    for (int j = 1; j < specificProject.getIssues().get(i).getTags().size(); j++) {
                        System.out.format(multipleTagsAlign, specificProject.getIssues().get(i).getTags().get(j));
                    }
                    System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+%n");
                } else {
                    System.out.format(alignFormatLeft, specificProject.getIssues().get(i).getId(), specificProject.getIssues().get(i).getTitle(), specificProject.getIssues().get(i).getStatus(), specificProject.getIssues().get(i).getTags().get(0), specificProject.getIssues().get(i).getPriority(), datetimeFormatted, specificProject.getIssues().get(i).getAssignee(), specificProject.getIssues().get(i).getCreatedBy());
                    System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+%n");
                }
            }

            System.out.println("Specify your sorting preference: ");
            System.out.println("'1'       - Sort by Priority");
            System.out.println("'2'       - Sort by Timestamps (Latest first)");
            System.out.println("otherwise - Default sorting by ID (Use the board above)");
            System.out.print("Your choice: ");
            int sortCheck = 0;
            String sortPref = sc.next();
            ArrayList<Issue> UnSortedIssues = specificProject.getIssues();
            if (sortPref.equalsIgnoreCase("2")) {
                UnSortedIssues.sort(Comparator.comparingLong(Issue::getTimestampUndated).reversed());
                sortCheck = 1;
            } else if (sortPref.equalsIgnoreCase("1")) {
                UnSortedIssues.sort(Comparator.comparingLong(Issue::getPriority).reversed());
                sortCheck = 1;
            } else {}

            // Print with Sorted (if necessary)
            if (sortCheck == 1) {
                System.out.println("Issue board");
                System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+");
                System.out.format("| ID |          Title          |   Status    |   Tag    | Priority |       Time       | Assignee | CreatedBy  |");
                System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+");
                // List Issues
                for (int i = 0; i < UnSortedIssues.size(); i++) {
                    SimpleDateFormat FormatPattern = new SimpleDateFormat("yyyy/MM/dd hh:mm");
                    String datetimeFormatted = FormatPattern.format(UnSortedIssues.get(i).getTimestamp());
                    if (UnSortedIssues.get(i).getTags().size() > 1) {
                        System.out.format(alignFormatLeft, UnSortedIssues.get(i).getId(), UnSortedIssues.get(i).getTitle(), UnSortedIssues.get(i).getStatus(), UnSortedIssues.get(i).getTags().get(0), UnSortedIssues.get(i).getPriority(), datetimeFormatted, UnSortedIssues.get(i).getAssignee(), UnSortedIssues.get(i).getCreatedBy());
                        for (int j = 1; j < UnSortedIssues.get(i).getTags().size(); j++) {
                            System.out.format(multipleTagsAlign, UnSortedIssues.get(i).getTags().get(j));
                        }
                        System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+");
                    } else {
                        System.out.format(alignFormatLeft, UnSortedIssues.get(i).getId(), UnSortedIssues.get(i).getTitle(), UnSortedIssues.get(i).getStatus(), UnSortedIssues.get(i).getTags().get(0), UnSortedIssues.get(i).getPriority(), datetimeFormatted, UnSortedIssues.get(i).getAssignee(), UnSortedIssues.get(i).getCreatedBy());
                        System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+");
                    }
                }
            }

            // Issue Dashboard inputs
            System.out.println("Enter selected issue ID to check issue");
            System.out.println("or 's' to search");
            System.out.println("or 'c' to create issue");
            System.out.println("or 'exit' to exit to Project Board");
            System.out.print("Input: ");
            String issueInput = sc.next();

            if (issueInput.equalsIgnoreCase("exit")) {
                recurseCheck = 0;
            } else {
                projectIssues = issuePage(specificProject, programUser, issueInput, specificProject.getIssuesArr());
            }
        }
        return projectIssues;
    }

    public static JSONArray issuePage(Project specificProject, User programUser, String issueInput, JSONArray projectIssues) {
        //Issue page
        Scanner sc = new Scanner(System.in);
        try {

            int issueSel = Integer.parseInt(issueInput)-1;

            JSONArray commentsArr = specificProject.getIssues().get(issueSel).getCommentsArr();

            SimpleDateFormat FormatPattern = new SimpleDateFormat("yyyy/MM/dd hh:mm");
            String datetimeFormatted = FormatPattern.format(specificProject.getIssues().get(issueSel).getTimestamp());
            System.out.println("Issue ID: " +specificProject.getIssues().get(issueSel).getId() +"\tStatus: " +specificProject.getIssues().get(issueSel).getStatus());
            System.out.println("Tag: " +specificProject.getIssues().get(issueSel).getTags().toString() +"\tPriority: " +specificProject.getIssues().get(issueSel).getPriority() +"\tCreated On: " +datetimeFormatted);
            System.out.println("==[ Title ]===========================================================");
            System.out.println(specificProject.getIssues().get(issueSel).getTitle());
            System.out.println("======================================================================");
            System.out.println("Assigned to: " +specificProject.getIssues().get(issueSel).getAssignee() +"\t\t\tCreated by: " +specificProject.getIssues().get(issueSel).getCreatedBy() +"\n");
            System.out.println("Issue Description\n-----------------");
            System.out.println(specificProject.getIssues().get(issueSel).getDescriptionText() +"\n");
            System.out.println("Comments\n---------");
            for (int i=0; i<specificProject.getIssues().get(issueSel).getComments().size(); i++) {
                System.out.println("#"+specificProject.getIssues().get(issueSel).getComments().get(i).getCommentId() +"\tCreated on: "+FormatPattern.format(specificProject.getIssues().get(issueSel).getComments().get(i).getTimestamp()) +"\tBy: "+specificProject.getIssues().get(issueSel).getComments().get(i).getUser());
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

            System.out.println("Enter");
            System.out.println("'r' to react");
            System.out.println("'c' to comment");
            System.out.println("or 'help' for more commands: ");
            System.out.print("Input: ");
            String choiceAction = sc.next();
            if (choiceAction.equalsIgnoreCase("r")) {
                System.out.println("Which comment you want to react?");
                System.out.print("Enter comment ID: ");
                int choiceCmtID = sc.nextInt()-1;

                System.out.println("What reaction you want to react with?");
                System.out.print("1-angry, 2-happy, 3-thumbsUp: ");
                int choiceReact = sc.nextInt()-1;
                if (specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().size() < 3 && choiceReact == 2) {
                    specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().add(new React("thumbsUp", 0));
                }
                specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().get(choiceReact).setCount(specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().get(choiceReact).getCount()+1);

                JSONArray reactArr = specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReactArr();
                JSONObject newReactIndex = new JSONObject();
                newReactIndex.put("reaction", (String) specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().get(choiceReact).getReaction());
                newReactIndex.put("count", (long) specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().get(choiceReact).getCount()+1);
                if (specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReactArr().size() < 3) {
                    reactArr.add(newReactIndex);
                } else {
                    reactArr.set(choiceReact, (JSONObject) newReactIndex);
                }

                JSONObject newCommentIndex = new JSONObject();
                newCommentIndex.put("comment_id", (long) choiceCmtID);
                newCommentIndex.put("text", (String) specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getText());
                newCommentIndex.put("react", (JSONArray) reactArr);
                newCommentIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getTimestampUndated());
                newCommentIndex.put("user", specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getUser());
                commentsArr.set(choiceCmtID, (JSONObject) newCommentIndex);

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

                System.out.println("You reacted to the comment with: " +specificProject.getIssues().get(issueSel).getComments().get(choiceCmtID).getReact().get(choiceReact).getReaction());
                projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                return projectIssues;

            } else if (choiceAction.equalsIgnoreCase("c")) {
                int newCommentID = specificProject.getIssues().get(issueSel).getComments().size()+1;
                System.out.println("Enter your comment text: \nHint: Use escape characters like backslash t or n to apply formatting for display\n");
                sc.nextLine();
                String newText = sc.nextLine();
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
                long newTimestampUndated = Instant.now().getEpochSecond();
                String user = programUser.getUsername();

                JSONObject newCommentIndex = new JSONObject();
                newCommentIndex.put("comment_id", (long) newCommentID);
                newCommentIndex.put("text", (String) newText);
                newCommentIndex.put("react", (JSONArray) newReactArr);
                newCommentIndex.put("timestamp", (long) newTimestampUndated);
                newCommentIndex.put("user", (String) user);
                commentsArr.add(newCommentIndex);

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

                System.out.println("Comment successfully posted.");
                projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                return projectIssues;

            } else if (choiceAction.equalsIgnoreCase("help")) {
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
                projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                return projectIssues;

            } else if (choiceAction.equalsIgnoreCase("open")) {
                if (specificProject.getIssues().get(issueSel).getCreatedBy().equals(programUser.getUsername())) {
                    if (specificProject.getIssues().get(issueSel).getStatus().equals("Open")) {
                        System.out.println("This issue's status is currently Open!");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;
                    } else {
                        if (specificProject.getIssues().get(issueSel).getStatus().equals("Closed")) {
                            System.out.println("Reopening Issue...");
                            specificProject.getIssues().get(issueSel).setStatus("Reopened");
                            System.out.println("Issue status set to: Reopened");;
                        } else {
                            specificProject.getIssues().get(issueSel).setStatus("Open");
                            System.out.println("Issue status set to: Open");
                        }

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
                } else {
                    System.out.println("You do not have permission. (not issue owner)");
                    projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                    return projectIssues;
                }

            } else if (choiceAction.equalsIgnoreCase("progress")) {
                if (specificProject.getIssues().get(issueSel).getCreatedBy().equals(programUser.getUsername()) || specificProject.getIssues().get(issueSel).getAssignee().equals(programUser.getUsername())) {
                    if (specificProject.getIssues().get(issueSel).getStatus().equals("In Progress")) {
                        System.out.println("This issue's status is currently In Progress!");
                         projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                         return projectIssues;
                    } else {
                        specificProject.getIssues().get(issueSel).setStatus("In Progress");

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
                } else {
                    System.out.println("You do not have permission. (not issue owner nor assignee)");
                    projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                    return projectIssues;
                }

            } else if (choiceAction.equalsIgnoreCase("close")) {
                if (specificProject.getIssues().get(issueSel).getCreatedBy().equals(programUser.getUsername())) {
                    if (specificProject.getIssues().get(issueSel).getStatus().equals("Close")) {
                        System.out.println("This issue's status is currently Closed!");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;
                    } else {
                        specificProject.getIssues().get(issueSel).setStatus("Closed");

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
                } else {
                    System.out.println("You do not have permission. (not issue owner)");
                    projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                    return projectIssues;
                }

            } else if (choiceAction.equalsIgnoreCase("resolve")) {
                if (specificProject.getIssues().get(issueSel).getCreatedBy().equals(programUser.getUsername()) || specificProject.getIssues().get(issueSel).getAssignee().equals(programUser.getUsername())) {
                    if (specificProject.getIssues().get(issueSel).getStatus().equals("Resolved")) {
                        System.out.println("This issue's status is currently Resolved!");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;
                    } else {
                        specificProject.getIssues().get(issueSel).setStatus("Resolved");

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
                } else {
                    System.out.println("You do not have permission. (not issue owner nor assignee)");
                    projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                    return projectIssues;
                }

            } else if (choiceAction.equalsIgnoreCase("edit")) {
                System.out.println("What do you want to edit in this issue?");
                System.out.println("1-Issue Desc, 2-Comments");
                int choiceEdit = sc.nextInt();
                if (choiceEdit == 1) {
                    if (specificProject.getIssues().get(issueSel).getCreatedBy().equals(programUser.getUsername())) {
                        System.out.println("Original Issue Description\n--------------------------");
                        System.out.println(specificProject.getIssues().get(issueSel).getDescriptionText());
                        System.out.println("------------------------------------------------------------");
                        System.out.println("Your desired new description text: \n(Hint!: You can copy the original text from this terminal and paste in your input for easy editing)\n(Hint!: escape characters like backslash t or n can be used)\n");
                        sc.nextLine();
                        specificProject.getIssues().get(issueSel).setDescriptionText(sc.nextLine());

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

                    } else {
                        System.out.println("You do not have permission. (not issue owner)");
                        projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                        return projectIssues;
                    }
                } else if (choiceEdit == 2) {
                    System.out.println("Which comment you want to edit? (only comment belongs to yours is allowed)");
                    System.out.print("Comment ID: ");
                    int editCmtID = sc.nextInt()-1;
                    if (specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getUser().equals(programUser.getUsername())) {
                        System.out.println("Original comment text\n---------------------");
                        System.out.println(specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getText());
                        System.out.println("------------------------------------------------------------------------------");
                        System.out.println("Your desired new comment text: \n(Hint!: You can copy the original text from this terminal and paste in your input for easy editing)\n(Hint!: escape characters like backslash t or n can be used)\n");
                        sc.nextLine();
                        String newCmtText = sc.nextLine();
                        specificProject.getIssues().get(issueSel).getComments().get(editCmtID).setText(newCmtText);

                        JSONObject newCommentIndex = new JSONObject();
                            newCommentIndex.put("comment_id", (long) specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getCommentId() );
                            newCommentIndex.put("text", (String) specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getText() );
                            newCommentIndex.put("react", (JSONArray) specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getReactArr() );
                            newCommentIndex.put("timestamp", (long) specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getTimestampUndated() );
                            newCommentIndex.put("user", (String) specificProject.getIssues().get(issueSel).getComments().get(editCmtID).getUser() );
                        commentsArr.add(newCommentIndex);

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
                    }
                } else {
                    System.out.println("Invalid choice!");
                    projectIssues = issuePage(specificProject, programUser, issueInput, projectIssues);
                    return projectIssues;
                }
            } else if (choiceAction.equalsIgnoreCase("exit")) {
                return projectIssues;
            } else if (choiceAction.equalsIgnoreCase("changes")) {
                System.out.println("Changes History & Revert: not implemented yet.");
            }


        } catch (NumberFormatException eee) {

            // Search issue
            if (issueInput.equalsIgnoreCase("s")) {
                System.out.println("Search: not implemented yet.");
            }
            // Create new issue
            else if (issueInput.equalsIgnoreCase("c")) {
                long id = specificProject.getIssues().size()+1;
                System.out.print("Enter new issue Title: ");
                sc.nextLine();
                String title = sc.nextLine();
                String status = "Open";
                System.out.print("Enter new issue Tags: (use spaces to split tags if required)");
                String tagsUnsafe = sc.nextLine();
                String[] tagsSafe = tagsUnsafe.split(" ");
                JSONArray Tags = new JSONArray();
                for (int i=0; i<tagsSafe.length; i++) {
                    Tags.add((String) tagsSafe[i]);
                }
                System.out.print("Specify priority of this issue: ");
                int priority = sc.nextInt();
                long timestamp = Instant.now().getEpochSecond();
                String createdBy = programUser.getUsername();
                System.out.print("Available assignees:");
                String alignFormatLeft = "| %-2d | %-16s |%n";
                System.out.println("+----+------------------+");
                System.out.println("| ID |     Username     |");
                System.out.println("+----+------------------+");
                for (int i=0; i<users.size(); i++) {
                    System.out.format(alignFormatLeft, users.get(i).getUserid(), users.get(i).getUsername());
                    System.out.println("+----+------------------+");
                }
                System.out.print("Choose by User ID: ");
                int choiceAssignee = sc.nextInt()-1;
                String Assignee = users.get(choiceAssignee).getUsername();
                long timestampUndated = Instant.now().getEpochSecond();
                JSONArray comments = new JSONArray();
                System.out.println("Description of the issue: \n(Hint!: escape characters like backslash t or n can be used)\n");
                sc.nextLine();
                String descText = sc.nextLine();

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

                System.out.println("Issue created successfully.");
            }

        }
        return projectIssues;
    }
}
