package mate.jdbc.util;

import java.io.InputStream;

public enum PropertiesLoader {

    INSTANCE;

    private final java.util.Properties data;

    PropertiesLoader() {
        this.data = loadProperties();
    }

    /**
     * Fetches property value by name
     *
     * @param name name of property
     * @return value
     */
    public String get(String name) {
        return data.getProperty(name);
    }

    /**
     * Loads properties from resource
     *
     * @return {@link java.util.Properties} object
     */
    private java.util.Properties loadProperties() {
        try (InputStream inputStream = PropertiesLoader.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            java.util.Properties props = new java.util.Properties();
            props.load(inputStream);
            return props;
        } catch (Exception e) {
            System.err.println("PropertiesLoader error: " + e.getMessage());
            return new java.util.Properties();
        }
    }
}
