package domain.exceptions;

public class EmptyFieldsException extends ValidationException {
    public EmptyFieldsException() {
        super("Fields must be not null");
    }

    public EmptyFieldsException(String message) {
        super("Fields must be not null");
    }

    public EmptyFieldsException(String message, Throwable cause) {
        super("Fields must be not null", cause);
    }

    public EmptyFieldsException(Throwable cause) {
        super("Fields must be not null");
    }

    public EmptyFieldsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super("Fields must be not null", cause, enableSuppression, writableStackTrace);
    }
}
