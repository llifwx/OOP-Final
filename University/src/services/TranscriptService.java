package services;

import model.academic.Mark;
import model.academic.Transcript;
import model.users.Admin;
import model.users.Manager;
import model.users.Student;
import model.users.Teacher;
import model.users.User;

public class TranscriptService {
    private static TranscriptService instance;
    private final AuthService authService;

    public TranscriptService(AuthService authService) {
        this.authService = authService;
    }

    public static TranscriptService getInstance() {
        if (instance == null) instance = new TranscriptService(null);
        return instance;
    }

    public void printTranscript(Transcript transcript) {
        requireTranscriptAccess(transcript);
        if (transcript == null) {
            System.out.println("Transcript is not available.");
            return;
        }
        System.out.println(transcript);
        for (Mark mark : transcript.getMarks()) {
            System.out.println(mark);
        }
    }

    private void requireTranscriptAccess(Transcript transcript) {
        User current = authService == null ? null : authService.getCurrentUser();
        if (current == null) {
            throw new SecurityException("[TranscriptService] Access denied: no user is logged in.");
        }
        if (transcript == null) return;
        Student owner = transcript.getStudent();
        if (current instanceof Admin || current instanceof Manager || current instanceof Teacher || current.equals(owner)) {
            return;
        }
        throw new SecurityException("[TranscriptService] Access denied: cannot view this transcript.");
    }
}
