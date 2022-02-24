package server.tz;

import org.postgis.MultiPolygon;

import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;


public class Db {

    /**
     * Метод для подключения к базе данных
     */
    public Connection connect() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream("app.properties"));
        try {
            return DriverManager.getConnection(properties.getProperty("url"), properties.getProperty("user"), properties.getProperty("password"));
        } catch (Exception e) {
            throw new Exception("Ошибка связи с базой данных: " + e);
        }
    }
}
