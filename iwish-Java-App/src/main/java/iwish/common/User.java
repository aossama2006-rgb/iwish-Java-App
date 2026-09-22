package iwish.common;

import java.io.Serializable;
import java.util.Objects;

// Public view of a user
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String email;

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        User u = (User) o;
        return id == u.id && Objects.equals(name, u.name) && Objects.equals(email, u.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }
}
