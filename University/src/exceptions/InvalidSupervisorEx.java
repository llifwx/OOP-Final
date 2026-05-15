package exceptions;

public class InvalidSupervisorEx extends Exception {
    public InvalidSupervisorEx() {
        super("Supervisor must have h-index of at least 3");
    }

    public InvalidSupervisorEx(String message) {
        super(message);
    }
}