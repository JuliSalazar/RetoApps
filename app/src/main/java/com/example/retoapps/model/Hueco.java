package com.example.retoapps.model;

public class Hueco {
    private String id;
    private String direction;
    private String lati;
    private String longi;
    private String username;

    public Hueco() {
    }

    public Hueco(String id, String direction, String lati, String longi, String username) {
        this.id = id;
        this.direction = direction;
        this.lati = lati;
        this.longi = longi;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getLati() {
        return lati;
    }

    public void setLati(String lati) {
        this.lati = lati;
    }

    public String getLongi() {
        return longi;
    }

    public void setLongi(String longi) {
        this.longi = longi;
    }
}
