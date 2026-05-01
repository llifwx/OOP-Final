import storage.Database;
import storage.FileStorage;

import javax.xml.crypto.Data;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        Database db = FileStorage.load();
        Database.setInstance(db);


        FileStorage.save(Database.getInstance());
    }
}
