package com.algopop.awscrud;

import com.algopop.awscrud.model.Widget;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.algopop.awscrud.dynamodb.Widgets.buildWidget;


public class GetListHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final boolean IS_MONGODB = true;
    private static final DynamoDbClientBuilder clientBuilder = DynamoDbClient.builder().region(Region.EU_WEST_1);
    private static final ScanRequest.Builder requestBuilder = ScanRequest.builder().tableName("Widget").limit(20);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        String httpMethod = request.getRequestContext().getHttp().getMethod();

        if (httpMethod.equals("GET")) {
            return handleGetRequest();
        }

        throw new IllegalArgumentException();  // should really return a 400 error of some kind
    }

    private APIGatewayV2HTTPResponse handleGetRequest() {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        WidgetCollection collection = getItems();

        response.setStatusCode(200);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        response.setHeaders(headers);

        response.setBody(gson.toJson(collection));

        return response;
    }

    private WidgetCollection getItems() {
        if (IS_MONGODB) {
            return getItemsMongoDb();
        }

        ScanRequest scanRequest = requestBuilder.build();
        final DynamoDbClient ddb = clientBuilder.build();
        ScanResponse scanResponse = ddb.scan(scanRequest);

        List<Widget> list = new ArrayList<>();
        if (scanResponse != null) {
            List<Map<String, AttributeValue>> items = scanResponse.items();

            if (items != null) {
                for (Map<String, AttributeValue> item : items) {
                    list.add(buildWidget(item));
                }
            }
        }

        return new WidgetCollection(list);
    }

    private WidgetCollection getItemsMongoDb() {
        final String configConnectionString = getMongoConfigConnectionString();

        String rawConnectionString;
        if (configConnectionString != null && !configConnectionString.isBlank()) {
            rawConnectionString = configConnectionString;
        } else {
            throw new RuntimeException("Couldn't get connection string property");
        }
        final ConnectionString connectionString = new ConnectionString(rawConnectionString);

        MongoClientSettings clientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .serverApi(ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build())
            .build();
        
        MongoClient mongoClient = MongoClients.create(clientSettings);

        try {
            MongoDatabase database = mongoClient.getDatabase("widgets-demo"); // redundant? since connection string contains database name

            MongoCollection<Document> collection = database.getCollection("widget");

            FindIterable<Document> widgetCursor = collection.find();

            List<Widget> widgets = new ArrayList<>();

            for (Document doc : widgetCursor) {
                widgets.add(buildWidgetMongoDb(doc));
            }

            return new WidgetCollection(widgets);
        } finally {
            mongoClient.close();
            mongoClient = null;
        }
    }

    private String getMongoConfigConnectionString() {
        Properties mongoProps = new Properties();
        InputStream propsStream = getClass().getClassLoader().getResourceAsStream("mongodb.properties");
        try {
            mongoProps.load(propsStream);
        } catch (Exception ex) {
            // TODO: Throw an appropriate exception.
            throw new RuntimeException("MongoDB user credentials not configured");
        }
        
        return mongoProps.getProperty("mongodb.connectionString");
    }

    private Widget buildWidgetMongoDb(Document doc) {
        ObjectId objectId = doc.getObjectId("_id");
        String id = objectId.toString();
        String name = doc.getString("name");
        Float cost = Float.parseFloat(doc.getDouble("cost").toString());
        Float weight = Float.parseFloat(doc.getDouble("weight").toString());

        return new Widget(id, name, cost, weight);
    }

    private static class WidgetCollection {
        private final List<Widget> items;

        public WidgetCollection(List<Widget> items) {
            this.items = items;
        }
    }
}
