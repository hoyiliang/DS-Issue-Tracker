import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import java.text.SimpleDateFormat;
import jdk.swing.interop.SwingInterOpUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Main {

    static List<Project> projects = new ArrayList<>();

    public static void main(String[] args) throws IOException, ParseException {
        JSONParser jp = new JSONParser();
        JSONObject jo = (JSONObject) jp.parse(new FileReader("C:\\Users\\yilia\\Downloads\\data.json"));
        JSONArray projectsArr = (JSONArray) jo.get("projects");

        for (int i=0; i< projectsArr.size(); i++) {
            JSONObject projectIndex = (JSONObject) projectsArr.get(i);

            long projectID = (long) projectIndex.get("id");
            String projectName = (String) projectIndex.get("name");
            JSONArray issuesArr = (JSONArray) projectIndex.get("issues");
            projects.add(new Project(projectID, projectName, issuesArr));
        }

        projectBoard(projectsArr);
    }

    public static void projectBoard(JSONArray projectsArr) {
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
        System.out.print("Input: ");
        String input = sc.next();

        // Search or Create new?
        try {

            int projectSel = Integer.parseInt(input);
            issueBoard(projects.get(projectSel-1));

        } catch (NumberFormatException e) {

            if (input.equalsIgnoreCase("c")) {
                long id = projects.size();
                System.out.print("Enter new Project name: ");
                sc.nextLine();
                String projectName = sc.nextLine();
                JSONArray projectIssues = new JSONArray();

                // Add project data in runtime
                projects.add(new Project(id, projectName, projectIssues));
                projectIssues = issueBoard(projects.get(projects.size()-1));

                // Add project data in json syntax
                JSONObject newProject = new JSONObject();
                newProject.put("id", (long) id);
                newProject.put("name", (String) projectName);
                newProject.put("issues", (JSONArray) projectIssues);
                projectsArr.add(newProject);
                JSONObject newjsondata = new JSONObject();
                newjsondata.put("projects", projectsArr);

                // write data to json algorithm
                try (FileWriter file = new FileWriter("data.json")) {
                    file.write(newjsondata.toJSONString());
                    file.flush();

                } catch (IOException ee) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("Invalid input specified! Please check your input!");
                projectBoard(projectsArr);

            }

        }
    }

    public static JSONArray issueBoard(Project specificProject) {
        Scanner sc = new Scanner(System.in);

        String alignFormatLeft = "| %-2d | %-23s | %-11s | %-8s | %-8d | %-16s | %-8s | %-10s |%n";
        // Issue board
        String multipleTagsAlign= "|    |                         |             | %-8s |          |                  |          |            |";
        System.out.println("Issue board");
        System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+");
        System.out.format("| ID |          Title          |   Status    |   Tag    | Priority |       Time       | Assignee | CreatedBy  |");
        System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+");
        // List Issues
        for (int i=0; i<specificProject.getIssues().size(); i++) {
            SimpleDateFormat FormatPattern = new SimpleDateFormat("yyyy/MM/dd hh:mm");
            String datetimeFormatted = FormatPattern.format(specificProject.getIssues().get(i).getTimestamp());
            if (specificProject.getIssues().get(i).getTags().size() > 1) {
                System.out.format(alignFormatLeft, specificProject.getIssues().get(i).getId(), specificProject.getIssues().get(i).getTitle(), specificProject.getIssues().get(i).getStatus(), specificProject.getIssues().get(i).getTags().get(0), specificProject.getIssues().get(i).getPriority(), datetimeFormatted, specificProject.getIssues().get(i).getAssignee(), specificProject.getIssues().get(i).getCreatedBy());
                for (int j=1; j<specificProject.getIssues().get(i).getTags().size(); i++) {
                    System.out.format(multipleTagsAlign, specificProject.getIssues().get(i).getTags().get(j));
                }
                System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+");
            } else {
                System.out.format(alignFormatLeft, specificProject.getIssues().get(i).getId(), specificProject.getIssues().get(i).getTitle(), specificProject.getIssues().get(i).getStatus(), specificProject.getIssues().get(i).getTags().get(0), specificProject.getIssues().get(i).getPriority(), datetimeFormatted, specificProject.getIssues().get(i).getAssignee(), specificProject.getIssues().get(i).getCreatedBy());
                System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+");
            }
        }

        System.out.println("Specify your sorting preference: ");
        System.out.println("'1'       - Sort by Priority");
        System.out.println("'2'       - Sort by Timestamps (Latest first)");
        System.out.println("otherwise - Default sorting by ID (Use the board above)");
        System.out.print("Your choice: ");
        String sortPref = sc.next();
        if (sortPref.equalsIgnoreCase("2")) {
            specificProject.getIssues().sort(Comparator.comparingLong(Issue::getTimestampUndated));
        } else if (sortPref.equalsIgnoreCase("1"))

        // Print with Sorted (if necessary)
        System.out.println("Issue board");
        System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+");
        System.out.format("| ID |          Title          |   Status    |   Tag    | Priority |       Time       | Assignee | CreatedBy  |");
        System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+");
        // List Issues
        for (int i=0; i<specificProject.getIssues().size(); i++) {
            SimpleDateFormat FormatPattern = new SimpleDateFormat("yyyy/MM/dd hh:mm");
            String datetimeFormatted = FormatPattern.format(specificProject.getIssues().get(i).getTimestamp());
            if (specificProject.getIssues().get(i).getTags().size() > 1) {
                System.out.format(alignFormatLeft, specificProject.getIssues().get(i).getId(), specificProject.getIssues().get(i).getTitle(), specificProject.getIssues().get(i).getStatus(), specificProject.getIssues().get(i).getTags().get(0), specificProject.getIssues().get(i).getPriority(), datetimeFormatted, specificProject.getIssues().get(i).getAssignee(), specificProject.getIssues().get(i).getCreatedBy());
                for (int j=1; j<specificProject.getIssues().get(i).getTags().size(); i++) {
                    System.out.format(multipleTagsAlign, specificProject.getIssues().get(i).getTags().get(j));
                }
                System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+");
            } else {
                System.out.format(alignFormatLeft, specificProject.getIssues().get(i).getId(), specificProject.getIssues().get(i).getTitle(), specificProject.getIssues().get(i).getStatus(), specificProject.getIssues().get(i).getTags().get(0), specificProject.getIssues().get(i).getPriority(), datetimeFormatted, specificProject.getIssues().get(i).getAssignee(), specificProject.getIssues().get(i).getCreatedBy());
                System.out.format("+----+-------------------------+-------------+----------+----------+------------------+----------+------------+");
            }
        }

        // Issue Dashboard inputs
        System.out.println("Enter selected issue ID to check issue");
        // return notimplementyet;
    }
}