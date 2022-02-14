package com.algopop.awscrud;

import java.io.InputStream;
import java.util.Properties;

import com.algopop.awscrud.model.Widget;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoDb {

    static final String WIDGETS_DEMO_DB = "widgets-demo";
    static final String WIDGET_COLLECTION = "widget";

    private MongoDb() {}

    static String getConnectionString() {
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

    static MongoClient getClient(String connectionString) {
        final ConnectionString validatedConnectionString = new ConnectionString(connectionString);

        MongoClientSettings clientSettings = MongoClientSettings.builder()
            .applyConnectionString(validatedConnectionString)
            .serverApi(ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build())
            .build();
        
        return MongoClients.create(clientSettings);
    }

    static MongoCollection<Document> getCollection(MongoClient mongoClient, String databaseName, String collectionName) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        return database.getCollection(collectionName);
    }

    static Widget buildWidget(Document doc) {
        ObjectId objectId = doc.getObjectId("_id");
        String id = objectId.toString();
        String name = doc.getString("name");
        Float cost = Float.parseFloat(doc.getDouble("cost").toString());
        Float weight = Float.parseFloat(doc.getDouble("weight").toString());

        return new Widget(id, name, cost, weight);
    }
}
