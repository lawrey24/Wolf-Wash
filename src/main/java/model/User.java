package model;

public class User {

    private final String id;
    private final String name;
    private final boolean admin;

    public User(String id, String name, boolean admin) {
        this.id = id;
        this.name = name;
        this.admin = admin;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAdmin() {
        return admin;
    }
}
