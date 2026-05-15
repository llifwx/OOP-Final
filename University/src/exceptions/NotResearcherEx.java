package exceptions;

public class NotResearcherEx extends Exception {
    public NotResearcherEx() {
        super("Only researchers can perform this action");
    }

    public NotResearcherEx(String message) {
        super(message);
    }
}
