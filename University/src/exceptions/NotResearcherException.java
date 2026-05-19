package exceptions;

public class NotResearcherException extends RuntimeException {
    public NotResearcherException() {
        super("Only researchers can perform this action");
    }

    public NotResearcherException(String message) {
        super(message);
    }
}
