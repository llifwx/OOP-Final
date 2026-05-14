import enums.Language;
import model.users.TechSupportSpecialist;
import storage.Database;
import storage.FileStorage;
import app.ConsoleApplication;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        Database db = FileStorage.load();
        Database.setInstance(db);

        new ConsoleApplication().run();
    }
}
