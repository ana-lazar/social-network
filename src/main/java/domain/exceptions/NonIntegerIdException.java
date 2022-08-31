package domain.exceptions;

public class NonIntegerIdException extends ValidationException {
    static String message = "ID must be a numeric value";

    public NonIntegerIdException() {
        super(message);
    }

    public NonIntegerIdException(String message) {
        super(NonIntegerIdException.message);
    }

    public NonIntegerIdException(String message, Throwable cause) {
        super(NonIntegerIdException.message, cause);
    }

    public NonIntegerIdException(Throwable cause) {
        super(NonIntegerIdException.message);
    }

    public NonIntegerIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(NonIntegerIdException.message, cause, enableSuppression, writableStackTrace);
    }
}
