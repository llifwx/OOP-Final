package storage;

import java.io.*;

public class FileStorage {
    private static final String FILE_NAME = "database.ser";

    public static void save(Database db) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getDatabaseFile()))) {
            oos.writeObject(db);
        } catch (IOException e) {
            System.out.println("Error occurred while saving file: " + e.getMessage());
        }
    }

    public static Database load() {
        File file = getDatabaseFile();

        if (!file.exists()) {
            return Database.getInstance();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Database) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Problem occurred while loading database: " + e.getMessage());
            return Database.getInstance();
        }
    }

    private static File getDatabaseFile() {
        File moduleDatabase = new File("University", FILE_NAME);
        if (moduleDatabase.exists() || moduleDatabase.getParentFile().exists()) {
            return moduleDatabase;
        }
        return new File(FILE_NAME);
    }
}
