package server.tz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.postgis.MultiPolygon;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Client {

    ObservableList<Robject> items = FXCollections.observableArrayList();
    public List<Robject> robjects;
    public Db db = new Db();

    @FXML
    public CheckBox checkBox;

    @FXML
    private Label Text;

    @FXML
    private TableView<Robject> table;

    @FXML
    private TableColumn<Robject, UUID> objectId;

    @FXML
    private TableColumn<Robject, String> coordinates;

    @FXML
    private TableColumn<Robject, MultiPolygon> geom;


    @FXML
    protected void onButtonClickGet() {
        for (int i = 0; i < table.getItems().size(); i++) {
            table.getItems().clear();
        }
        objectId.setCellValueFactory(new PropertyValueFactory<>("objectId"));
        coordinates.setCellValueFactory(new PropertyValueFactory<>("coordinates"));
        geom.setCellValueFactory(new PropertyValueFactory<>("geom"));

        try {
            robjects = db.getConnect(checkBox.isSelected());
            items.addAll(robjects);
            table.setItems(items);
        } catch (Exception e) {
            Text.setText("Ошибка при получении данных из базы:" + e);
        }
    }

    @FXML
    protected void onButtonClickUpdate() throws IOException {
        if (robjects != null && robjects.size() > 0) {
            String errors = "";
            Integer countErrors = 0;
            try {
                for (int i = 0; i < robjects.size(); i++) {
                    if (!Objects.equals(robjects.get(i).getGeom(), "")) {
                        db.updateGeometry(robjects.get(i).getObjectId(), robjects.get(i).getGeom());
                    } else {
                        countErrors++;
                        errors = errors + "'" + robjects.get(i).getObjectId().toString() + "'; \n";
                    }
                }
            } catch (Exception e) {
                Text.setText("Ошибка отправки данных:" + e);
            }
            if (countErrors > 0) {
                FileWriter writer = new FileWriter("log.txt");
                writer.write(errors);
                writer.flush();
            }
            Text.setText("Полигоны успешно загружены\nОшибочных объекты: " + countErrors + "\nСписок ошибочных объектов записан в файл log.txt в папке с программой");
        }
        else {
            Text.setText("Нет данных для конвертации");
        }
    }
}


