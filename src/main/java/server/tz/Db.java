package server.tz;

import org.postgis.MultiPolygon;

import java.io.FileInputStream;
import java.sql.*;
import java.util.*;


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

    public Map<String, String> getCoodrinateSystem() throws Exception {
        ResultSet resultSet;
        Connection connection = connect();
        Statement statement = connection.createStatement();
        resultSet = statement.executeQuery(
                "select value from oconstant o " +
                        "where o.constant_id = 'c8ffd4e7-576f-473d-ae92-de29e743c463'");
        resultSet.next();
        String[] coord = resultSet.getString("value").split(",");
        Map<String, String> coordinateSystems = new HashMap<>();
        for(String tmp : coord) {
            coordinateSystems.put(tmp.split("-")[0].trim(),tmp.split("-")[1].trim());
        }
        connection.close();
        return coordinateSystems;
    }
}
