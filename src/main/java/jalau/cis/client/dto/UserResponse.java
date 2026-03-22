package jalau.cis.client.dto;

public class UserResponse {
    private String id;
    private String name;
    private String login;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    @Override
    public String toString() {
        return String.format("Id: %s, Name: %s, Login: %s", id, name, login);
    }
}
