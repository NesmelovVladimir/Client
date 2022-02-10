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
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class Client {

    ObservableList<Dogovor> items = FXCollections.observableArrayList();
    public List<Dogovor> dogovors;

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
    private TableColumn<Dogovor, String> coordinates;

    @FXML
    private TableColumn<Dogovor, MultiPolygon> geom;


    @FXML
    protected void onButtonClickGet() {
        dogovorId.setCellValueFactory(new PropertyValueFactory<>("dogovorId"));
        dogNo.setCellValueFactory(new PropertyValueFactory<>("dogNo"));
        dogDate.setCellValueFactory(new PropertyValueFactory<>("dogDate"));
        updateTime.setCellValueFactory(new PropertyValueFactory<>("updateTime"));
        check.setCellValueFactory(new PropertyValueFactory<>("check"));
        coordinates.setCellValueFactory(new PropertyValueFactory<>("coordinates"));
        geom.setCellValueFactory(new PropertyValueFactory<>("geom"));

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            URL url = new URL("http://localhost:8089/");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String result = reader.readLine();

            TypeReference<List<Dogovor>> dogovorsType = new TypeReference<>() {
            };

            dogovors = objectMapper.readValue(result, dogovorsType);
            items.addAll(dogovors);

            table.setItems(items);

        } catch (Exception ec) {
            welcomeText.setText("Error connectiong to server:" + ec.toString());
        }
    }
    @FXML
    protected void onButtonClickUpdate() {
       /* try {
            ObjectMapper objectMapper = new ObjectMapper();
            URL url = new URL("http://localhost:8089/"+dogovors.get(0).getDogovorId().toString());
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(
                    httpCon.getOutputStream());
            JsonGenerator jsonGenerator = null;
            out.write(objectMapper.writeValue(jsonGenerator,dogovors.get(0)));
            out.close();
            httpCon.getInputStream();
        } catch (Exception e) {
            welcomeText.setText("Error connectiong to server:" + e.toString());
        }*/
    }
}


