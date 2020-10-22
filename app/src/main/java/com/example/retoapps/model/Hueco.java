package com.example.retoapps.model;

public class Hueco {
    private String id;
    private String direction;
    private double lati;
    private double longi;
    private String estado;

    public Hueco() {
    }

    public Hueco(String id, String direction, double lati, double longi, String estado) {
        this.id = id;
        this.direction = direction;
        this.lati = lati;
        this.longi = longi;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

}
