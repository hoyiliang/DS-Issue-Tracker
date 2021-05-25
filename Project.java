import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Project {

    private long id;
    private String name;
    private JSONArray issuesArr= null;
    private ArrayList<Issue> issues = new ArrayList<>();

    /**
     * No args constructor for use in serialization
     *
     */
    public Project() {
    }

    /**
     *
     * @param name
     * @param id
     * @param issuesArr
     */
    public Project(long id, String name, JSONArray issuesArr) {
        this.id = id;
        this.name = name;
        this.issuesArr = issuesArr;
        this.issues = issues;

        for (int i=0; i< issuesArr.size(); i++) {
            JSONObject issueIndex = (JSONObject) issuesArr.get(i);

            long issueID = (long) issueIndex.get("id");
            String issueTitle = (String) issueIndex.get("title");
            long issuePriority = (long) issueIndex.get("priority");
            String issueStatus = (String) issueIndex.get("status");
            JSONArray issueTag = (JSONArray) issueIndex.get("tag");
            String issueDesc = (String) issueIndex.get("descriptionText");
            String issueOwner = (String) issueIndex.get("createdBy");
            String issueAssignee = (String) issueIndex.get("assignee");
            long issueTimestamp = (long) issueIndex.get("timestamp");
            JSONArray issueCommentsArr = (JSONArray) issueIndex.get("comments");

            issues.add(new Issue(issueID, issueTitle, issuePriority, issueStatus, issueTag, issueDesc, issueOwner, issueAssignee, issueTimestamp, issueCommentsArr));
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONArray getIssuesArr() {
        return issuesArr;
    }

    public void setIssuesArr(JSONArray issuesArr) {
        this.issuesArr = issuesArr;
    }

    public ArrayList<Issue> getIssues() {
        return issues;
    }

    public void setIssues(ArrayList<Issue> issues) {
        this.issues = issues;
    }
}