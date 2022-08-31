package domain.exceptions;

public class InexistentIdException extends ValidationException {
    static String message = "ID does not exist in the network";

    public InexistentIdException() {
        super(message);
    }

    public InexistentIdException(String message) {
        super(DuplicatedIdException.message);
    }

    public InexistentIdException(String message, Throwable cause) {
        super(DuplicatedIdException.message, cause);
    }

    public InexistentIdException(Throwable cause) {
        super(DuplicatedIdException.message);
    }

    public InexistentIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(DuplicatedIdException.message, cause, enableSuppression, writableStackTrace);
    }
}
