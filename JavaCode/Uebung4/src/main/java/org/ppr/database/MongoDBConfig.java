package org.ppr.database;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Class for managing properties for connection to a MongoDB
 * @author Original template by Giuseppe Abrami, edited by Philipp.
 */
public class MongoDBConfig extends Properties {

    /**
     * Constructor with the path of the Config-File.
     * @param filePath file path
     * @throws IOException exception
     */
    public MongoDBConfig(String filePath) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
        this.load(reader);
        reader.close();
    }

    /**
     * Method for the hostname.
     * @return hostname
     */
    public String getMongoHostname(){
        return getProperty("remote_host", "127.0.0.1");

    }

    /**
     * Method for the username.
     * @return username
     */
    public String getMongoUsername(){
        return getProperty("remote_user", "user");

    }

    /**
     * Method for the password.
     * @return password
     */
    public String getMongoPassword(){
        return getProperty("remote_password", "password");
    }

    /**
     * Method for the port.
     * @return port
     */
    public int getMongoPort(){
        return Integer.parseInt(getProperty("remote_port", "27017"));
    }


    /**
     * Method for the database name.
     * @return database name
     */
    public String getMongoDatabase(){
        return getProperty("remote_database", "database");
    }
}
