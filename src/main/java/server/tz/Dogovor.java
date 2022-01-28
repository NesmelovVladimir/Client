package server.tz;

import javafx.scene.control.CheckBox;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

public class Dogovor implements Serializable {
    private UUID dogovorId;
    private String dogNo;
    private Timestamp dogDate;
    private Timestamp updateTime;
    private boolean check;
    private CheckBox checkBox;

    public Dogovor() {
    }

    public UUID getDogovorId() {
        return dogovorId;
    }

    public void setDogovorId(UUID dogovorId) {
        this.dogovorId = dogovorId;
    }

    public String getDogNo() {
        return dogNo;
    }

    public void setDogNo(String dogNo) {
        this.dogNo = dogNo;
    }

    public Timestamp getDogDate() {
        return dogDate;
    }

    public void setDogDate(Timestamp dogDate) {
        this.dogDate = dogDate;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public CheckBox getCheck() {
        checkBox = new CheckBox();
        checkBox.selectedProperty().setValue(checkActulalDate());
        return checkBox;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public boolean checkActulalDate() {
        long updateTimeEpoch = updateTime.toInstant().getEpochSecond();
        long currentTimeEpoch = System.currentTimeMillis() / 1000;
        long difference = currentTimeEpoch - updateTimeEpoch;
        return difference < 5184000L;
    }
}
