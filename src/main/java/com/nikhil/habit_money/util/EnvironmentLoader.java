package com.nikhil.habit_money.util;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class EnvironmentLoader {

    private EnvironmentLoader() {}

    public static void load() {
        File envFile = findEnvFile();
        if (envFile == null) return;

        Properties props = new Properties();
        try (FileReader reader = new FileReader(envFile)) {
            props.load(reader);
            props.forEach((key, value) -> System.setProperty((String) key, (String) value));
        } catch (Exception e) {
            System.err.println("Could not load .env file: " + e.getMessage());
        }
    }

    private static File findEnvFile() {
        File envFile = new File(".env");
        if (envFile.exists()) return envFile;

        envFile = new File("backend/habit-money/.env");
        if (envFile.exists()) return envFile;

        return null;
    }
}
