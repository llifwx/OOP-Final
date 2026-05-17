package services;

import model.academic.Mark;
import model.academic.Transcript;

public class TranscriptService {
    private static TranscriptService instance;

    private TranscriptService() {
    }

    public static TranscriptService getInstance() {
        if (instance == null) instance = new TranscriptService();
        return instance;
    }

    public void printTranscript(Transcript transcript) {
        if (transcript == null) {
            System.out.println("Transcript is not available.");
            return;
        }
        System.out.println(transcript);
        for (Mark mark : transcript.getMarks()) {
            System.out.println(mark);
        }
    }
}
