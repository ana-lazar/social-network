package config;

import config.ApplicationContext;

public class DatabaseProperties {
    private static final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
    private static final String user = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.user");
    private static final String password = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.password");

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static String getPassword() {
        return password;
    }
}
