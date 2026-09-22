package iwish.common;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Serializable data;
    private final boolean sessionExpired;

    private Response(boolean success, String message, Serializable data, boolean sessionExpired) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.sessionExpired = sessionExpired;
    }

    public static Response ok(String message) {
        return new Response(true, message, null, false);
    }

    public static Response ok(String message, Serializable data) {
        return new Response(true, message, data, false);
    }

    public static Response error(String message) {
        return new Response(false, message, null, false);
    }

    //The connection has no signed-in user
    public static Response sessionExpired() {
        return new Response(false, "Please sign in again.", null, true);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Serializable getData() {
        return data;
    }

    public boolean isSessionExpired() {
        return sessionExpired;
    }
}
