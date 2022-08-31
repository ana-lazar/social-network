package domain.exceptions;

public class DuplicatedIdException extends ValidationException {
    static String message = "ID already exists in the network";

    public DuplicatedIdException() {
        super(message);
    }

    public DuplicatedIdException(String message) {
        super(DuplicatedIdException.message);
    }

    public DuplicatedIdException(String message, Throwable cause) {
        super(DuplicatedIdException.message, cause);
    }

    public DuplicatedIdException(Throwable cause) {
        super(DuplicatedIdException.message);
    }

    public DuplicatedIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(DuplicatedIdException.message, cause, enableSuppression, writableStackTrace);
    }
}
