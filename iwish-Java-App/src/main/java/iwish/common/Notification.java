package iwish.common;

import java.io.Serializable;
import java.util.Objects;

/** A message for a user, e.g. "a gift was fully funded". */
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String message;
    private final boolean read;
    private final long createdAt; // epoch milliseconds

    public Notification(int id, String message, boolean read, long createdAt) {
        this.id = id;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notification)) {
            return false;
        }
        Notification n = (Notification) o;
        return id == n.id && read == n.read && createdAt == n.createdAt
                && Objects.equals(message, n.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, read, createdAt);
    }
}
