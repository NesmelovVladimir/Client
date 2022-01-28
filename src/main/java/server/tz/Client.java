package server.tz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class Client {

    ObservableList<Dogovor> items = FXCollections.observableArrayList();

    @FXML
    private Label welcomeText;

    @FXML
    private TableView<Dogovor> table;

    @FXML
    private TableColumn<Dogovor, UUID> dogovorId;

    @FXML
    private TableColumn<Dogovor, String> dogNo;

    @FXML
    private TableColumn<Dogovor, Timestamp> dogDate;

    @FXML
    private TableColumn<Dogovor, Timestamp> updateTime;

    @FXML
    private TableColumn<Dogovor, Timestamp> check;

    @FXML
    protected void onHelloButtonClick() {
        dogovorId.setCellValueFactory(new PropertyValueFactory<>("dogovorId"));
        dogNo.setCellValueFactory(new PropertyValueFactory<>("dogNo"));
        dogDate.setCellValueFactory(new PropertyValueFactory<>("dogDate"));
        updateTime.setCellValueFactory(new PropertyValueFactory<>("updateTime"));
        check.setCellValueFactory(new PropertyValueFactory<>("check"));

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            URL url = new URL("http://localhost:8089/");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String result = reader.readLine();

            TypeReference<List<Dogovor>> dogovorsType = new TypeReference<>() {
            };

            List<Dogovor> dogovors = objectMapper.readValue(result, dogovorsType);
            items.addAll(dogovors);

            table.setItems(items);

        } catch (Exception ec) {
            welcomeText.setText("Error connectiong to server:" + ec.toString());
        }
    }

}


