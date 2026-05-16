import enums.Language;
import factory.UserFactory;
import model.users.Admin;
import model.users.TechSupportSpecialist;
import storage.Database;
import storage.FileStorage;
import app.ConsoleApplication;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        Database db = FileStorage.load();
        Database.setInstance(db);

        if (db.findUserByUsername("admin") == null) {
            db.addUser(UserFactory.createAdmin(
                    "admin",
                    "admin123",
                    "System Admin",
                    "admin@university.kz",
                    Language.EN
            ));
            db.save();
        }

        new ConsoleApplication().run();
    }
}
