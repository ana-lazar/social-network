package domain.exceptions;

public class DuplicatedUserNameException extends ValidationException {
    static String message = "Username already exists in the network";

    public DuplicatedUserNameException() {
        super(message);
    }

    public DuplicatedUserNameException(String message) {
        super(DuplicatedIdException.message);
    }

    public DuplicatedUserNameException(String message, Throwable cause) {
        super(DuplicatedIdException.message, cause);
    }

    public DuplicatedUserNameException(Throwable cause) {
        super(DuplicatedIdException.message);
    }

    public DuplicatedUserNameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(DuplicatedIdException.message, cause, enableSuppression, writableStackTrace);
    }
}
