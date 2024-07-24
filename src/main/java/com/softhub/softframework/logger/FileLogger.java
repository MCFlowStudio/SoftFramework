package com.softhub.softframework.logger;

import com.softhub.softframework.task.SimpleAsync;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileLogger {

    @Getter
    private JavaPlugin instance;
    @Getter
    private String fileName;
    private File logFile;

    public FileLogger(JavaPlugin instance, String fileName) {
        this.instance = instance;
        this.fileName = fileName.endsWith(".log") ? fileName : fileName + ".log";
        createLogFile();
    }

    private void createLogFile() {
        try {
            File dataFolder = instance.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            logFile = new File(dataFolder, fileName);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String message) {
        SimpleAsync.async(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(message);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
