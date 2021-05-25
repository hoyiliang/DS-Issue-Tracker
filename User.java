import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "userid",
        "username",
        "password"
})

public class User {

    @JsonProperty("userid")
    private Integer userid;
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;

    /**
     * No args constructor for use in serialization
     *
     */
    public User() {
    }

    /**
     *
     * @param password
     * @param userid
     * @param username
     */
    public User(Integer userid, String username, String password) {
        super();
        this.userid = userid;
        this.username = username;
        this.password = password;
    }

    @JsonProperty("userid")
    public Integer getUserid() {
        return userid;
    }

    @JsonProperty("userid")
    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public User withUserid(Integer userid) {
        this.userid = userid;
        return this;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    public User withUsername(String username) {
        this.username = username;
        return this;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    public User withPassword(String password) {
        this.password = password;
        return this;
    }

}