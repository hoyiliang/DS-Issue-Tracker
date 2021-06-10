package DSPack;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UndoRedo {
    private int proId;
    private String projectName;
    private int issueId;
    private String issueName;
    private String oldIssueDesc;
    private String newIssueDesc;
    private int commentId;
    private String oldComment;
    private String newComment;
    private String time;


    public UndoRedo() {
    }

    public UndoRedo(int proId, String projectName, int issueId, String issueName, String oldIssueDesc, String newIssueDesc, String time) {
        this.proId = proId;
        this.projectName = projectName;
        this.issueId = issueId;
        this.issueName = issueName;
        this.oldIssueDesc = oldIssueDesc;
        this.newIssueDesc = newIssueDesc;
        this.time = time;
    }

    public UndoRedo(int proId, String projectName, int issueId, String issueName, int commentId, String oldComment, String newComment, String time) {
        this.proId = proId;
        this.projectName = projectName;
        this.issueId = issueId;
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
        return "Project Name: " + getProjectName() + "\n" +
                "Issue: " + getIssueName() + "\n" +
                "Old Issue Description: " + getOldIssueDesc() + "\n" +
                "Edited Issue Description: " + getNewIssueDesc() + "\n" +
                "Time Edited: " + getTime() + "\n" +
                "-----------------------------------------------------------";
    }

    public String toCommentString() {
        return "Project Name: " + getProjectName() + "\n" +
                "Issue: " + getIssueName() + "\n" +
                "Old Comment: " + getOldComment() + "\n" +
                "Edited Comment: " + getNewComment() + "\n" +
                "Time Edited: " + getTime() + "\n" +
                "-----------------------------------------------------------";
    }
}
