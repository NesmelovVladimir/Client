package server.tz;

import org.postgis.MultiPolygon;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Db {

    public Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://bdserv:5432/novouralskiy_1810", "postgres", "bLtybc84$");
    }

    public List<Robject> getConnect() throws Exception {
        ResultSet resultSet;
        List<Robject> robjects = new ArrayList<Robject>();
        Connection connection = connect();
        Statement statement = connection.createStatement();

        resultSet = statement.executeQuery("SELECT object_id, coordinates, ST_AsText(geom) as geom FROM robject WHERE coordinates is not null");
        while (resultSet.next()) {
            Robject robject = new Robject();
            robject.setObjectId(UUID.fromString(resultSet.getString("object_id")));
            robject.setCoordinates(resultSet.getString("coordinates"));
            try {
                robject.setGeom(new MultiPolygon(resultSet.getString("geom")));
            }
            catch (Exception e) {
                throw new Exception("Ошибка при создании геометрии: "+e);
            }
            robjects.add(robject);
        }
        connection.close();
        return robjects;
    }

    public void updateGeometry(UUID objectId, String geom) throws SQLException {

        String SQL = String.format("update robject SET geom=ST_GeomFromText('%1$s', 4326) " + "WHERE object_id= '%2$s' ", geom, objectId.toString());

        Connection connection = connect();
        PreparedStatement prepareStatement = connection.prepareStatement(SQL);

        prepareStatement.executeUpdate();
    }
}
