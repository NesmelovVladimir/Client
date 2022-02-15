package server.tz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.postgis.MultiPolygon;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public class Client {

    ObservableList<Robject> items = FXCollections.observableArrayList();
    public List<Robject> robjects;

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
        objectId.setCellValueFactory(new PropertyValueFactory<>("objectId"));
        coordinates.setCellValueFactory(new PropertyValueFactory<>("coordinates"));
        geom.setCellValueFactory(new PropertyValueFactory<>("geom"));

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            URL url = new URL("http://localhost:8091/");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String result = reader.readLine();

            TypeReference<List<Robject>> robjectType = new TypeReference<>() {
            };

            robjects = objectMapper.readValue(result, robjectType);
            items.addAll(robjects);

            table.setItems(items);

        } catch (Exception ec) {
            Text.setText("Error connectiong to server:" + ec.toString());
        }
    }

    @FXML
    protected void onButtonClickUpdate() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < robjects.size(); i++) {
                URL url = new URL("http://localhost:8091/" + robjects.get(i).getObjectId().toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("PUT");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                String object = objectMapper.writeValueAsString(robjects.get(i));
                outputStreamWriter.write(object);
                outputStreamWriter.close();
                urlConnection.getInputStream();
                if (urlConnection.getResponseCode() == 200) {
                    Text.setText("Полигоны успешно загружены");
                }
            }
        } catch (Exception e) {
            Text.setText("Error connectiong to server:" + e.toString());
        }
    }
}


