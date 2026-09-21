package iwish.server;

/** A rule violation whose message is safe and useful to show to the user. */
public class BusinessException extends Exception {
    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}
