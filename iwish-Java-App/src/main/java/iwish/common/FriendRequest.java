package iwish.common;

import java.io.Serializable;
import java.util.Objects;

/** A pending friend request as seen by the receiver. */
public class FriendRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final User sender;

    public FriendRequest(int id, User sender) {
        this.id = id;
        this.sender = sender;
    }

    public int getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FriendRequest)) {
            return false;
        }
        FriendRequest r = (FriendRequest) o;
        return id == r.id && Objects.equals(sender, r.sender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sender);
    }
}
