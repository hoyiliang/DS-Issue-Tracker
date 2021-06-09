package DS;

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
    private String userName;

    public UndoRedo() {
    }

    public UndoRedo(int proId, String projectName, int issueId, String issueName, String oldIssueDesc, String newIssueDesc) {
        this.proId = proId;
        this.projectName = projectName;
        this.issueId = issueId;
        this.issueName = issueName;
        this.oldIssueDesc = oldIssueDesc;
        this.newIssueDesc = newIssueDesc;
    }

    public UndoRedo(int proId, String projectName, int issueId, String issueName, int commentId, String oldComment, String newComment) {
        this.proId = proId;
        this.projectName = projectName;
        this.issueId = issueId;
        this.issueName = issueName;
        this.commentId = commentId;
        this.oldComment = oldComment;
        this.newComment = newComment;
    }

    public int getProId() {
        return proId;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getIssueId() {
        return issueId;
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

    public String toIssueString() {
        return "Project Name: " + getProjectName() + "\n" +
                "Issue: " + getIssueName() + "\n" +
                "Old Issue Description: " + getOldIssueDesc() + "\n" +
                "Edited Issue Description: " + getNewIssueDesc() + "\n" +
                "-----------------------------------------------------------";
    }

    public String toCommentString() {
        return "Project Name: " + getProjectName() + "\n" +
                "Issue: " + getIssueName() + "\n" +
                "Old Comment: " + getOldComment() + "\n" +
                "Edited Comment: " + getNewComment() + "\n" +
                "-----------------------------------------------------------";
    }
}
