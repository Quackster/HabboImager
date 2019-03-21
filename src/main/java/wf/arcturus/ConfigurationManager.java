package wf.arcturus;

import java.io.*;
import java.sql.*;
import java.util.*;

public class ConfigurationManager
{
    public boolean loaded;
    public boolean isLoading;
    private final Properties properties;
    
    public ConfigurationManager(final String path) throws Exception {
        this.loaded = false;
        this.isLoading = false;
        this.properties = new Properties();
        this.reload();
    }
    
    public void reload() throws Exception {
        this.isLoading = true;
        this.properties.clear();
        InputStream input = null;
        try {
            final File f = new File("config_imager.ini");
            input = new FileInputStream(f);
            this.properties.load(input);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("[Configuration Manager][CRITICAL] FAILED TO LOAD CONFIG.INI FILE!");
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
        if (this.loaded) {
            this.loadFromDatabase();
        }
        this.isLoading = false;
        System.out.println("[Configuration Manager] Loaded!");
    }
    
    public void loadFromDatabase() {
        System.out.println("[Configuration Manager] Loading configuration from database...");
        final long millis = System.currentTimeMillis();
        try (final Connection connection = Imaging.database.dataSource().getConnection();
             final Statement statement = connection.createStatement()) {
            if (statement.execute("SELECT * FROM settings")) {
                try (final ResultSet set = statement.getResultSet()) {
                    while (set.next()) {
                        this.properties.put(set.getString("key"), set.getString("value"));
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("[Configuration Manager] Loaded! (" + (System.currentTimeMillis() - millis) + " MS)");
    }
    
    public String value(final String key) {
        return this.value(key, "");
    }
    
    public String value(final String key, final String defaultValue) {
        if (this.isLoading) {
            return defaultValue;
        }
        if (!this.properties.containsKey(key)) {
            System.out.println("[CONFIG] Key not found: " + key);
        }
        return this.properties.getProperty(key, defaultValue);
    }
    
    public boolean bool(final String key) {
        return this.bool(key, false);
    }
    
    public boolean bool(final String key, final boolean defaultValue) {
        if (this.isLoading) {
            return defaultValue;
        }
        try {
            return this.value(key, "0").equals("1") || this.value(key, "false").equals("true");
        }
        catch (Exception e) {
            System.out.println("Failed to parse key " + key + " with value " + this.value(key) + " to type boolean.");
            return defaultValue;
        }
    }
    
    public int integer(final String key) {
        return this.integer(key, 0);
    }
    
    public int integer(final String key, final Integer defaultValue) {
        if (this.isLoading) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(this.value(key, defaultValue.toString()));
        }
        catch (Exception e) {
            System.out.println("Failed to parse key " + key + " with value " + this.value(key) + " to type integer.");
            return defaultValue;
        }
    }
    
    public double getDouble(final String key) {
        return this.getDouble(key, 0.0);
    }
    
    public double getDouble(final String key, final Double defaultValue) {
        if (this.isLoading) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(this.value(key, defaultValue.toString()));
        }
        catch (Exception e) {
            System.out.println("Failed to parse key " + key + " with value " + this.value(key) + " to type double.");
            return defaultValue;
        }
    }
    
    public void update(final String key, final String value) {
        this.properties.setProperty(key, value);
    }
}
