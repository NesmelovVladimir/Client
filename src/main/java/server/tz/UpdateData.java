package server.tz;

import javafx.concurrent.Task;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Задача на получения данных из базы
 */
public class UpdateData extends Task<String[]> {

    public boolean check;
    public Db db = new Db();
    public Statement statement;
    public Connection connection;
    public List<Robject> robjects;


    public UpdateData(List<Robject> robjects) {
        this.robjects = robjects;
    }

    @Override
    protected String[] call() throws Exception {
        String errors = "";

        Integer countErrors = countGeom(robjects);
        for (int i = 0; i < robjects.size(); i++) {
            if (!Objects.equals(robjects.get(i).getGeom(), "")) {
                updateGeometry(robjects.get(i).getObjectId(), robjects.get(i).getGeom());
                this.message(i, robjects.size());
                this.updateProgress(i, countErrors);
            } else {
                errors = errors + "'" + robjects.get(i).getObjectId().toString() + "'; \n";
            }
        }
        if (countErrors > 0) {
            FileWriter writer = new FileWriter("log.txt");
            writer.write(errors);
            writer.flush();
        }

        String[] result = new String [2];
        result[0] = "Ошибочных объекты: " + countErrors + "\nСписок ошибочных объектов записан в файл log.txt в папке с программой";
        countErrors = robjects.size()-countErrors;
        result[1]=countErrors.toString();
        return result;
    }

    private void message(int currentState, int allInfo) throws InterruptedException {
        this.updateMessage("Обновлено: " + currentState + " из " + allInfo);
        Thread.sleep(500);
    }

    /**
     * Метод обновления геометрии в базу данных
     */
    public void updateGeometry(UUID objectId, String geom) throws Exception {

        String SQL = String.format("update robject SET geom=ST_GeomFromText('%1$s', 4326) " + "WHERE object_id= '%2$s' ", geom, objectId.toString());

        connection = db.connect();
        PreparedStatement prepareStatement = connection.prepareStatement(SQL);

        prepareStatement.executeUpdate();
    }

    /**
     *Метод получения количества объектов с геометрией
     */
    public int countGeom(List<Robject> robjects) {
        int countErrors = 0;
        for (int i = 0; i < robjects.size(); i++) {
            if (Objects.equals(robjects.get(i).getGeom(), "")) {
                countErrors++;
            }
        }
        return countErrors;
    }
}

