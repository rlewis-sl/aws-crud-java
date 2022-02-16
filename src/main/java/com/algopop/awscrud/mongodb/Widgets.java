package com.algopop.awscrud.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.algopop.awscrud.ItemNotFoundException;
import com.algopop.awscrud.MongoDb;
import com.algopop.awscrud.model.Widget;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class Widgets {
    public static final String WIDGETS_DEMO_DB = "widgets-demo";
    public static final String WIDGET_COLLECTION = "widget";

    private Widgets() {}

    public static List<Widget> getWidgets() {
        final String connectionString = MongoDb.getConnectionString();
        MongoClient mongoClient = MongoDb.getClient(connectionString);
        try {
            MongoCollection<Document> collection = MongoDb.getCollection(mongoClient, WIDGETS_DEMO_DB, WIDGET_COLLECTION);
            Iterable<Document> widgetCursor = collection.find();

            List<Widget> widgets = new ArrayList<>();

            for (Document doc : widgetCursor) {
                widgets.add(buildWidget(doc));
            }

            return widgets;
        } finally {
            mongoClient.close();
            mongoClient = null;
        }
    }

    public static Widget getWidget(String id) throws ItemNotFoundException {
        final String connectionString = MongoDb.getConnectionString();
        MongoClient mongoClient = MongoDb.getClient(connectionString);

        try {
            MongoCollection<Document> collection = MongoDb.getCollection(mongoClient, WIDGETS_DEMO_DB, WIDGET_COLLECTION);

            Bson filter = Filters.eq("_id", new ObjectId(id));
            Iterable<Document> widgetCursor = collection.find(filter);
            Document doc = widgetCursor.iterator().next();
        
            return buildWidget(doc);

        } catch (NoSuchElementException ex) {
            throw new ItemNotFoundException();
            
        } finally {
            mongoClient.close();
        }
    }

    public static String createWidget(Widget widget) {
        Document doc = widgetToDocument(widget);

        final String connectionString = MongoDb.getConnectionString();
        MongoClient mongoClient = MongoDb.getClient(connectionString);
        try {
            MongoCollection<Document> collection = MongoDb.getCollection(mongoClient, WIDGETS_DEMO_DB, WIDGET_COLLECTION);
            InsertOneResult result = collection.insertOne(doc);

            return result.getInsertedId().asObjectId().getValue().toHexString();

        } finally {
            mongoClient.close();
        }
    }

    public static Widget updateWidget(String id, Widget widget) {
        String widgetId = widget.getId();
        if (widgetId.isBlank()) {
            widget.setId(id);
        } else if (!widgetId.equals(id)) {
            throw new IllegalArgumentException();  // should really return a 400 error of some kind
        }

        Document doc = widgetToDocument(widget);

        final String connectionString = MongoDb.getConnectionString();
        MongoClient mongoClient = MongoDb.getClient(connectionString);
        try {
            MongoCollection<Document> collection = MongoDb.getCollection(mongoClient, WIDGETS_DEMO_DB, WIDGET_COLLECTION);
            Bson filter = Filters.eq("_id", new ObjectId(id));
            collection.replaceOne(filter, doc);

            return widget;

        } finally {
            mongoClient.close();
        }
    }

    public static void deleteWidget(String id) {
        final String connectionString = MongoDb.getConnectionString();
        MongoClient mongoClient = MongoDb.getClient(connectionString);
        try {
            MongoCollection<Document> collection = MongoDb.getCollection(mongoClient, WIDGETS_DEMO_DB, WIDGET_COLLECTION);
            Bson filter = Filters.eq("_id", new ObjectId(id));
            collection.deleteOne(filter);
        } finally {
            mongoClient.close();
        }
    }

    public static Widget buildWidget(Document doc) {
        ObjectId objectId = doc.getObjectId("_id");
        String id = objectId.toString();
        String name = doc.getString("name");
        Float cost = Float.parseFloat(doc.getDouble("cost").toString());
        Float weight = Float.parseFloat(doc.getDouble("weight").toString());

        return new Widget(id, name, cost, weight);
    }

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
