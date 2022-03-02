package server.tz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.postgis.MultiPolygon;

import java.util.List;
import java.util.UUID;

public class Client {

    ObservableList<Robject> items = FXCollections.observableArrayList();
    public List<Robject> robjects;
    private GetData getData;
    private UpdateData updateData;
    private String[] result;
    private Thread thread;

    @FXML
    public CheckBox checkBox;

    @FXML
    public Label Text;

    @FXML
    private Button getInfo;

    @FXML
    private Button updateInfo;

    @FXML
    private ProgressIndicator process;

    @FXML
    private TableView<Robject> table;

    @FXML
    private TableColumn<Robject, UUID> objectId;

    @FXML
    private TableColumn<Robject, String> coordinates;

    @FXML
    private TableColumn<Robject, MultiPolygon> geom;

    @FXML
    private TableColumn<Robject, MultiPolygon> oldGeom;

    /**
     * Описание события кнопки "Получить данные"
     * Получение данных из базы
     */
    @FXML
    protected void onButtonClickGet() {

        getInfo.setDisable(true);
        updateInfo.setDisable(true);
        checkBox.setDisable(true);
        for (int i = 0; i < table.getItems().size(); i++) {
            table.getItems().clear();
        }
        objectId.setCellValueFactory(new PropertyValueFactory<>("objectId"));
        coordinates.setCellValueFactory(new PropertyValueFactory<>("coordinates"));
        geom.setCellValueFactory(new PropertyValueFactory<>("geom"));
        oldGeom.setCellValueFactory(new PropertyValueFactory<>("oldGeom"));

        getData = new GetData(checkBox.isSelected());

        Text.textProperty().bind(getData.messageProperty());

        process.progressProperty().bind(getData.progressProperty());

        getData.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                robjects = getData.getValue();
                Text.textProperty().unbind();
                Text.setText("Загружено: " + robjects.size() + " Записи(-ей)");
                items.addAll(robjects);
                table.setItems(items);
                getInfo.setDisable(false);
                checkBox.setDisable(false);
                if (robjects.size() == 0) {
                    process.setVisible(false);
                    updateInfo.setDisable(true);
                } else {
                    updateInfo.setDisable(false);
                }
            }
        });
        getData.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                Text.textProperty().unbind();
                Text.setText("Ошибка:" + getData.getException());
                getInfo.setDisable(false);
                checkBox.setDisable(false);
            }
        });

        getData.addEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                process.setVisible(true);
            }
        });
        thread = new Thread(getData);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Описание события кнопки "Отправить данные"
     * Обновление поля GEOM в базе
     */
    @FXML
    protected void onButtonClickUpdate() {
        getInfo.setDisable(true);
        updateInfo.setDisable(true);
        checkBox.setDisable(true);
        updateData = new UpdateData(robjects);

        Text.textProperty().bind(updateData.messageProperty());
        process.progressProperty().bind(updateData.progressProperty());

        updateData.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                result = updateData.getValue();
                Text.textProperty().unbind();
                Text.setText("Обновлено: " + result[1] + " Записи(-ей)" + "\n" + result[0]);

                getInfo.setDisable(false);
                checkBox.setDisable(false);
                updateInfo.setDisable(true);
            }
        });
        updateData.addEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                process.setVisible(true);
            }
        });
        updateData.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                Text.textProperty().unbind();
                Text.setText("Ошибка:" + updateData.getException());
                getInfo.setDisable(false);
                checkBox.setDisable(false);
                updateInfo.setDisable(false);
            }
        });
        thread = new Thread(updateData);
        thread.setDaemon(true);
        thread.start();
    }
}


