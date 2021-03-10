package tech.itpark.di.exception;

public class DefinitionReadException extends DIException {
    public DefinitionReadException() {
        super();
    }

    public DefinitionReadException(String message) {
        super(message);
    }

    public DefinitionReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public DefinitionReadException(Throwable cause) {
        super(cause);
    }

    protected DefinitionReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
