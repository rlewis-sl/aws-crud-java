package com.algopop.awscrud.json;

import com.algopop.awscrud.model.Widget;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WidgetParsing {
    private static final Gson gson = new GsonBuilder().create();
    
    public static Widget getWidget(String jsonText) {
        WidgetParsing parsing = gson.fromJson(jsonText, WidgetParsing.class);
        return parsing.getWidget();
    }

    private String id;
    private String name;
    private String cost;
    private String weight;

    public WidgetParsing(String id, String name, String cost, String weight) {
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

    public String getCost() {
        return cost;
    }

    public String getWeight() {
        return weight;
    }

    public Widget getWidget() {
        fixEmptyCost();
        fixEmptyWeight();
        return new Widget(id, name, Float.parseFloat(cost), Float.parseFloat(weight));
    }

    private void fixEmptyCost() {
        if (this.cost.isBlank()) {
            this.cost = "0";
        }
    }

    private void fixEmptyWeight() {
        if (this.weight.isBlank()) {
            this.weight = "0";
        }
    }
}
