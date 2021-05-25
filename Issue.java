import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "title",
        "priority",
        "status",
        "tag",
        "descriptionText",
        "createdBy",
        "assignee",
        "timestamp",
        "comments"
})
public class Issue {

    @JsonProperty("id")
    private long id;
    @JsonProperty("title")
    private String title;
    @JsonProperty("priority")
    private long priority;
    @JsonProperty("status")
    private String status;
    @JsonProperty("tag")
    private JSONArray tag;
    @JsonProperty("descriptionText")
    private String descriptionText;
    @JsonProperty("createdBy")
    private String createdBy;
    @JsonProperty("assignee")
    private String assignee;
    @JsonProperty("timestamp")
    private Date timestamp;
    private long timestampUndated;
    @JsonProperty("comments")
    private JSONArray commentsArr = null;
    private ArrayList<String> tags = new ArrayList<>();
    private ArrayList<Comment> comments = new ArrayList<>();

    /**
     * No args constructor for use in serialization
     *
     */
    public Issue() {
    }

    /**
     *
     * @param commentsArr
     * @param createdBy
     * @param id
     * @param tag
     * @param descriptionText
     * @param assignee
     * @param title
     * @param priority
     * @param status
     * @param timestamp
     */
    public Issue(long id, String title, long priority, String status, JSONArray tag, String descriptionText, String createdBy, String assignee, long timestamp, JSONArray commentsArr) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.tag = tag;
        this.descriptionText = descriptionText;
        this.createdBy = createdBy;
        this.assignee = assignee;
        this.timestampUndated = timestamp;
        this.timestamp = new Date(timestamp*1000);
        this.commentsArr = commentsArr;
        this.tags = tags;
        this.comments = comments;


        for (int i=0; i<tag.size(); i++) {
            tags.add((String) tag.get(i));
        }

        for (int i=0; i<commentsArr.size(); i++) {
            JSONObject commentIndex  = (JSONObject) commentsArr.get(i);

            long commentID = (long) commentIndex.get("comment_id");
            String commentText = (String) commentIndex.get("text");
            JSONArray commentReactArr = (JSONArray) commentIndex.get("react");
            long commentTimestamp = (long) commentIndex.get("timestamp");
            String commentUser = (String) commentIndex.get("user");

            comments.add(new Comment(commentID, commentText, commentReactArr, commentTimestamp, commentUser));
        }
    }

    // Constructors
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JSONArray getTag() {
        return tag;
    }

    public void setTag(JSONArray tag) {
        this.tag = tag;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public JSONArray getCommentsArr() {
        return commentsArr;
    }

    public void setCommentsArr(JSONArray commentsArr) {
        this.commentsArr = commentsArr;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public long getTimestampUndated() {
        return timestampUndated;
    }

    public void setTimestampUndated(long timestampUndated) {
        this.timestampUndated = timestampUndated;
    }
}