package com.algopop.awscrud.mongodb;

import java.util.HashMap;
import java.util.Map;

import com.algopop.awscrud.model.Widget;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Widgets {
    private Widgets() {}

    public static Document widgetToDocument(Widget widget) {
        Map<String, Object> attributeMap = getAttributeMap(widget);
        return new Document(attributeMap);
    }

    private static Map<String, Object> getAttributeMap(Widget widget) {
        Map<String, Object> attributeMap = new HashMap<>();
        if (widget.getId() != null) {
            attributeMap.put("_id", new ObjectId(widget.getId()));
        }

        attributeMap.put("name", widget.getName());
        attributeMap.put("cost", widget.getCost());
        attributeMap.put("weight", widget.getWeight());

        return attributeMap;
    }
}
