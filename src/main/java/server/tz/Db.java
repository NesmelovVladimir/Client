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

    /**
     * Метод получения записей из базы данных
     */
    public List<Robject> getConnect(boolean check) throws Exception {
            ResultSet resultSet;
            List<Robject> robjects = new ArrayList<Robject>();
            Connection connection = connect();
            Statement statement = connection.createStatement();

            if (check) {
                resultSet = statement.executeQuery("SELECT object_id, coordinates, ST_AsText(geom) as geom FROM robject WHERE coordinates is not null");
            } else {
                resultSet = statement.executeQuery("SELECT object_id, coordinates, ST_AsText(geom) as geom FROM robject WHERE coordinates is not null and geom is null");
            }
            while (resultSet.next()) {
                Robject robject = new Robject();
                robject.setObjectId(UUID.fromString(resultSet.getString("object_id")));
                robject.setCoordinates(resultSet.getString("coordinates"));
                if (resultSet.getObject("geom") != null) {
                    robject.setGeom(new MultiPolygon(resultSet.getString("geom")));
                } else {
                    robject.setGeom(new MultiPolygon());
                }
                robjects.add(robject);
            }
            connection.close();
            return robjects;

    }


    /**
     * Метод обновления геометрии в базу данных
     */
    public void updateGeometry(UUID objectId, String geom) throws Exception {

        String SQL = String.format("update robject SET geom=ST_GeomFromText('%1$s', 4326) " + "WHERE object_id= '%2$s' ", geom, objectId.toString());

        Connection connection = connect();
        PreparedStatement prepareStatement = connection.prepareStatement(SQL);

        prepareStatement.executeUpdate();
    }
}
