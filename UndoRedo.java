package DSPack;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "project_name",
    "issue_name",
    "previous_description",
    "edited_description",
    "comment_id",
    "previous_comment",
    "edited_comment",
    "time_edited"
})

public class UndoRedo {

    @JsonProperty("project_name")
    private String projectName;
    @JsonProperty("issue_name")
    private String issueName;
    @JsonProperty("previous_description")
    private String oldIssueDesc;
    @JsonProperty("edited_description")
    private String newIssueDesc;
    @JsonProperty("comment_id")
    private int commentId;
    @JsonProperty("previous_comment")
    private String oldComment;
    @JsonProperty("edited_comment")
    private String newComment;
    @JsonProperty("time_edited")
    private String time;

    public UndoRedo() {
    }

    public UndoRedo(String projectName, String issueName, String oldIssueDesc, String newIssueDesc, int commentId, String oldComment, String newComment, String time) {
        this.projectName = projectName;
        this.issueName = issueName;
        this.oldIssueDesc = oldIssueDesc;
        this.newIssueDesc = newIssueDesc;
        this.commentId = commentId;
        this.oldComment = oldComment;
        this.newComment = newComment;
        this.time = time;
    }
    
    public UndoRedo(String projectName, String issueName, String oldIssueDesc, String newIssueDesc, String time) {
        this.projectName = projectName;
        this.issueName = issueName;
        this.oldIssueDesc = oldIssueDesc;
        this.newIssueDesc = newIssueDesc;
        this.time = time;
    }

    public UndoRedo(String projectName, String issueName, int commentId, String oldComment, String newComment, String time) {
        this.projectName = projectName;
        this.issueName = issueName;
        this.commentId = commentId;
        this.oldComment = oldComment;
        this.newComment = newComment;
        this.time = time;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getIssueName() {
        return issueName;
    }

    public String getOldIssueDesc() {
        return oldIssueDesc;
    }

    public String getNewIssueDesc() {
        return newIssueDesc;
    }

    public int getCommentId() {
        return commentId;
    }

    public String getOldComment() {
        return oldComment;
    }

    public String getNewComment() {
        return newComment;
    }

    public String getTime() {
        return time;
    }

    public String toIssueString() {
        return "Project Name: " + getProjectName() + "\n"
                + "Issue: " + getIssueName() + "\n"
                + "Old Issue Description: " + getOldIssueDesc() + "\n"
                + "Edited Issue Description: " + getNewIssueDesc() + "\n"
                + "Time Edited: " + getTime() + "\n"
                + "-----------------------------------------------------------";
    }

    public String toCommentString() {
        return "Project Name: " + getProjectName() + "\n"
                + "Issue: " + getIssueName() + "\n"
                + "Old Comment: " + getOldComment() + "\n"
                + "Edited Comment: " + getNewComment() + "\n"
                + "Time Edited: " + getTime() + "\n"
                + "-----------------------------------------------------------";
    }
}
