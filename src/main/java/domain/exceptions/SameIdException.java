package domain.exceptions;

public class SameIdException extends ValidationException {
    public SameIdException() {
        super("Friends must have different IDs");
    }

    public SameIdException(String message) {
        super("Friends must have different IDs");
    }

    public SameIdException(String message, Throwable cause) {
        super("Friends must have different IDs", cause);
    }

    public SameIdException(Throwable cause) {
        super("Friends must have different IDs");
    }

    public SameIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super("Friends must have different IDs", cause, enableSuppression, writableStackTrace);
    }
}
