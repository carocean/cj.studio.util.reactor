package cj.studio.util.reactor;

public class CombineException extends Exception {
    public CombineException() {
        super();
    }

    public CombineException(String message) {
        super(message);
    }

    public CombineException(String message, Throwable cause) {
        super(message, cause);
    }

    public CombineException(Throwable cause) {
        super(cause);
    }

    protected CombineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
