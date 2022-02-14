package com.algopop.awscrud;

import com.algopop.awscrud.dynamodb.Widgets;
import com.algopop.awscrud.model.Widget;
import com.algopop.awscrud.model.WidgetCollection;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GetListHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final boolean IS_MONGODB = true;
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

        return new WidgetCollection(Widgets.getWidgets());
    }

    private WidgetCollection getItemsMongoDb() {
        final String configConnectionString = MongoDb.getMongoConfigConnectionString();
        final ConnectionString connectionString = new ConnectionString(configConnectionString);

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
                widgets.add(MongoDb.buildWidgetMongoDb(doc));
            }

            return new WidgetCollection(widgets);
        } finally {
            mongoClient.close();
            mongoClient = null;
        }
    }
}
