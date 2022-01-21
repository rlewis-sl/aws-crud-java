package com.algopop.awscrud.dynamodb;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

import com.algopop.awscrud.model.Widget;

public class Widgets {

    private Widgets() {}
    
    public static Widget buildWidget(Map<String, AttributeValue> item) {
        String id = item.get("Id").s();
        String name = item.get("Name").s();
        float cost = Float.parseFloat(item.get("Cost").n());
        float weight = Float.parseFloat(item.get("Weight").n());

        return new Widget(id, name, cost, weight);
    }

    public static Map<String, AttributeValue> keyAttributes(String id) {
        Map<String, AttributeValue> attributes = new HashMap<>();
        AttributeValue idValue = AttributeValue.builder().s(id).build();

        attributes.put("Id", idValue);
        return attributes;
    }
}
