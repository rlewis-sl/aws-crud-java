package com.algopop.awscrud.model;

public class Widget {
    private String id;
    private final String name;
    private final Float cost;
    private final Float weight;

    public Widget(String id, String name, Float cost, Float weight) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.weight = weight;
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

    public Float getCost() {
        return cost;
    }

    public Float getWeight() {
        return weight;
    }
}
