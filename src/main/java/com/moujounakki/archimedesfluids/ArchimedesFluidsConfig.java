package com.moujounakki.archimedesfluids;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ArchimedesFluidsConfig {

    private static final String CONFIG_FILE = "config/archimedesfluids.properties";
    private final Properties configProperties = new Properties();

    // Single instance
    private static ArchimedesFluidsConfig instance;

    // Private constructor for singleton pattern
    private ArchimedesFluidsConfig() {
        loadProperties();
    }

    // Public method to get the instance
    public static synchronized ArchimedesFluidsConfig getInstance() {
        if (instance == null) {
            instance = new ArchimedesFluidsConfig();
        }
        return instance;
    }

    private void loadProperties() {
        File configFile = new File(CONFIG_FILE);

        if (!configFile.exists()) {
            setDefaultProperties();
            saveProperties();
        } else {
            try (FileInputStream input = new FileInputStream(configFile)) {
                configProperties.load(input);
            } catch (IOException e) {
                e.printStackTrace();
                setDefaultProperties(); // Load default properties in case of error
            }
        }
    }

    private void setDefaultProperties() {
        configProperties.setProperty("max_queue_size", "4096");
        configProperties.setProperty("queue_clean_interval", "256");
        configProperties.setProperty("max_updates_per_tick", "128");
    }

    private void saveProperties() {
        try (FileOutputStream output = new FileOutputStream(CONFIG_FILE)) {
            configProperties.store(output, "Archimedes Fluids Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getMaxQueueSize() {
        return Integer.parseInt(configProperties.getProperty("max_queue_size", "4096"));
    }

    public int getQueueCleanInterval() {
        return Integer.parseInt(configProperties.getProperty("queue_clean_interval", "256"));
    }

    public int getMaxUpdatesPerTick() {
        return Integer.parseInt(configProperties.getProperty("max_updates_per_tick", "512"));
    }
}
