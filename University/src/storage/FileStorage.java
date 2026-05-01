package storage;

import javax.xml.crypto.Data;
import java.io.*;

public class FileStorage {
    private static final String FILE_PATH = "database.ser";

    public static void save(Database db) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(db);
        } catch (IOException e) {
            System.out.println("Error occurred while saving file: " + e.getMessage());
        }
    }

    public static Database load() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return Database.getInstance();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            return (Database) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Problem occurred while loading database: " + e.getMessage());
            return Database.getInstance();
        }
    }
}
