
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "reaction",
        "count"
})

public class React {

    @JsonProperty("reaction")
    private String reaction;
    @JsonProperty("count")
    private long count;

    /**
     * No args constructor for use in serialization
     *
     */
    public React() {
    }

    /**
     *
     * @param reaction
     * @param count
     */
    public React(String reaction, long count) {
        this.reaction = reaction;
        this.count = count;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}