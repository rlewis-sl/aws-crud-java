package com.algopop.awscrud;

import java.io.InputStream;
import java.util.Properties;

import com.algopop.awscrud.model.Widget;

import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoDb {
    private MongoDb() {}

    static String getMongoConfigConnectionString() {
        Properties mongoProps = new Properties();
        InputStream propsStream = MongoDb.class.getClassLoader().getResourceAsStream("mongodb.properties");
        try {
            mongoProps.load(propsStream);
        } catch (Exception ex) {
            // TODO: Throw an appropriate exception.
            throw new RuntimeException("MongoDB user credentials not configured");
        }
        
        String connectionString = mongoProps.getProperty("mongodb.connectionString");
        if (connectionString == null || connectionString.isBlank()) {
            // TODO: Throw an appropriate exception.
            throw new RuntimeException("Couldn't get mongodb.connectionString property");
        }

        return connectionString;
    }

    static Widget buildWidgetMongoDb(Document doc) {
        ObjectId objectId = doc.getObjectId("_id");
        String id = objectId.toString();
        String name = doc.getString("name");
        Float cost = Float.parseFloat(doc.getDouble("cost").toString());
        Float weight = Float.parseFloat(doc.getDouble("weight").toString());

        return new Widget(id, name, cost, weight);
    }
}
