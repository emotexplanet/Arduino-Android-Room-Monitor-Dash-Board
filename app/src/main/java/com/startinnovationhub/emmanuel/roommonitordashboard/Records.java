package com.startinnovationhub.emmanuel.roommonitordashboard;

/**
 * Created by Emmanuel on 22/09/2017.
 */

public class Records {
    private String temperature;
    private String humidity;
    private String polutionLevel;
    private String status;
    private String date;


    public Records(String temperature, String humidity, String polutionLevel, String status, String date) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.polutionLevel = polutionLevel;
        this.status = status;
        this.date = date;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getPolutionLevel() {
        return polutionLevel;
    }

    public void setPolutionLevel(String polutionLevel) {
        this.polutionLevel = polutionLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
