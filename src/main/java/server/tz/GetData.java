package server.tz;

import javafx.concurrent.Task;
import org.postgis.MultiPolygon;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Задача на получения данных из базы
 */
public class GetData extends Task<List<Robject>> {

    public boolean check;
    public Db db = new Db();
    public Statement statement;
    public Connection connection;
    List<Robject> robjects = new ArrayList<>();

    public GetData(boolean check) {
        this.check = check;
    }

    @Override
    protected List<Robject> call() throws Exception {

        try {
            ResultSet resultSet;
            connection = db.connect();
            statement = connection.createStatement();


            if (check) {
                resultSet = statement.executeQuery("SELECT object_id, coordinates, ST_AsText(geom) as geom, (SELECT count(object_id) as count FROM robject WHERE coordinates is not null) as count FROM robject WHERE coordinates is not null");
            } else {
                resultSet = statement.executeQuery("SELECT object_id, coordinates, ST_AsText(geom) as geom, (SELECT count(object_id) as count FROM robject WHERE coordinates is not null and geom is null) as count FROM robject WHERE coordinates is not null and geom is null");
            }
            int i = 1;
            while (resultSet.next()) {
                Robject robject = new Robject();
                robject.setObjectId(UUID.fromString(resultSet.getString("object_id")));
                robject.setCoordinates(resultSet.getString("coordinates"));
                if (resultSet.getObject("geom") != null) {
                    robject.setOldGeom(new MultiPolygon(resultSet.getString("geom")));
                } else {
                    robject.setOldGeom(new MultiPolygon());
                }
                robject.setGeom(new MultiPolygon());
                this.message(i, resultSet.getInt("count"));
                this.updateProgress(i, resultSet.getInt("count"));
                i++;
                robjects.add(robject);
            }
            connection.close();
            return robjects;
        }
        catch (Exception e) {
        throw new Exception("\nПроизошла ошибка при получении данных из базы: " + e);
        }
    }

    private void message(int currentState, int allInfo) throws InterruptedException {
        this.updateMessage("Загружено: " + currentState + " из " + allInfo);
        //Thread.sleep(500);
    }

}
