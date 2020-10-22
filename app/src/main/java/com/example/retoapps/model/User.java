package com.example.retoapps.model;

public class User {

    private PositionContainer container;
    private String name;

    public User() {

    }

    public User(String name, PositionContainer cont) {
        this.name = name;
        this.container = cont;
    }

    public PositionContainer getContainer() {
        return container;
    }

    public void setContainer(PositionContainer container) {
        this.container = container;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
