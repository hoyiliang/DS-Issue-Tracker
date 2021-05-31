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
        "comment_id",
        "text",
        "react",
        "timestamp",
        "user"
})
public class Comment {

    @JsonProperty("comment_id")
    private long commentId;
    @JsonProperty("text")
    private String text;
    @JsonProperty("react")
    private JSONArray reactArr = null;
    @JsonProperty("timestamp")
    private long timestampUndated;
    private Date timestamp;
    @JsonProperty("user")
    private String user;
    private List<React> react = new ArrayList<>();

    /**
     * No args constructor for use in serialization
     *
     */
    public Comment() {
    }

    /**
     *
     * @param commentId
     * @param reactArr
     * @param text
     * @param user
     * @param timestamp
     */
    public Comment(long commentId, String text, JSONArray reactArr, long timestamp, String user) {
        this.commentId = commentId;
        this.text = text;
        this.reactArr = reactArr;
        this.timestampUndated = timestamp;
        this.timestamp = new Date(timestamp*1000);
        this.user = user;
        this.react = react;

        for (int i=0; i<reactArr.size(); i++) {
            JSONObject reactIndex = (JSONObject) reactArr.get(i);

            String reactReaction = (String) reactIndex.get("reaction");
            long reactCount = (long) reactIndex.get("count");

            react.add(new React(reactReaction, reactCount));
        }
    }

    public long getCommentId() {
        return commentId;
    }

    public void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public JSONArray getReactArr() {
        return reactArr;
    }

    public void setReactArr(JSONArray reactArr) {
        this.reactArr = reactArr;
    }

    public long getTimestampUndated() {
        return timestampUndated;
    }

    public void setTimestampUndated(long timestampUndated) {
        this.timestampUndated = timestampUndated;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getTimestamp() { return timestamp; }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public List<React> getReact() { return react; }

    public void setReact(List<React> react) { this.react = react; }
}
