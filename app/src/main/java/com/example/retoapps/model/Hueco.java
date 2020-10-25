package com.example.retoapps.model;

import java.util.UUID;

public class Hueco {
    private String name;
    private String direction;
    private boolean confirmado;
    private double lat;
    private double lng;
    private String id;

    public Hueco() {
    }

    public Hueco(String name, String direction, boolean confirm, double lat,  double lng, String id) {
        this.name = name;
        this.direction = direction;
        this.confirmado = confirm;
        this.lat = lat;
        this.lng = lng;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }



    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

}
