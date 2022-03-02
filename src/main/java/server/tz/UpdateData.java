package server.tz;

import javafx.concurrent.Task;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Задача на получения данных из базы
 */
public class UpdateData extends Task<String[]> {

    public Db db = new Db();
    public Connection connection;
    public List<Robject> robjects;


    public UpdateData(List<Robject> robjects) {
        this.robjects = robjects;
    }

    @Override
    protected String[] call() throws Exception {
        StringBuilder errors = new StringBuilder();
        connection = db.connect();
        int countErrors = countGeom(robjects);
        for (int i = 0; i < robjects.size(); i++) {
            if (!Objects.equals(robjects.get(i).getGeom(), "")) {
                updateGeometry(robjects.get(i).getObjectId(), robjects.get(i).getGeom());
                this.message(i, robjects.size());
                this.updateProgress(i, robjects.size() - countErrors - 1);
            } else {
                errors.append("'").append(robjects.get(i).getObjectId().toString()).append("', \n");
            }
        }
        if (countErrors > 0) {
            FileWriter writer = new FileWriter("log.txt");
            writer.write(errors.toString());
            writer.flush();
        }

        String[] result = new String[2];
        result[0] = "Ошибочных объекты: " + countErrors + "\nСписок ошибочных объектов записан в файл log.txt в папке с программой";
        countErrors = robjects.size() - countErrors;
        result[1] = Integer.toString(countErrors);
        connection.close();
        return result;
    }

    private void message(int currentState, int allInfo) {
        this.updateMessage("Обновлено: " + currentState + " из " + allInfo);
    }

    /**
     * Метод обновления геометрии в базу данных
     */
    public void updateGeometry(UUID objectId, String geom) throws Exception {

        String SQL = String.format("update robject SET geom=ST_GeomFromText('%1$s', 4326) " + "WHERE object_id= '%2$s' ", geom, objectId.toString());

        PreparedStatement prepareStatement = connection.prepareStatement(SQL);

        prepareStatement.executeUpdate();

    }

    /**
     * Метод получения количества объектов с геометрией
     */
    public int countGeom(List<Robject> robjects) {
        int countErrors = 0;
        for (Robject robject : robjects) {
            if (Objects.equals(robject.getGeom(), "")) {
                countErrors++;
            }
        }
        return countErrors;
    }
}

