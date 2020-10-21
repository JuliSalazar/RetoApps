package com.example.retoapps.model;

public class PositionContainer {

    private Position location;

    public PositionContainer() {
    }

    public PositionContainer(Position location) {
        this.location = location;
    }

    public Position getLocation() {
        return location;
    }

    public void setLocation(Position location) {
        this.location = location;
    }
}
