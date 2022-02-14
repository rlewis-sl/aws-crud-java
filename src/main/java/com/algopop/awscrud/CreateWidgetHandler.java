package com.algopop.awscrud;

import com.algopop.awscrud.dynamodb.Widgets;
import com.algopop.awscrud.model.Widget;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.algopop.awscrud.MongoDb.WIDGETS_DEMO_DB;
import static com.algopop.awscrud.MongoDb.WIDGET_COLLECTION;
import static com.algopop.awscrud.mongodb.Widgets.widgetToDocument;


public class CreateWidgetHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final boolean IS_MONGODB = true;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        String httpMethod = request.getRequestContext().getHttp().getMethod();

        if (httpMethod.equals("POST")) {
            return handleCreateWidgetRequest(request);
        }

        throw new IllegalArgumentException();  // should really return a 400 error of some kind
    }

    private APIGatewayV2HTTPResponse handleCreateWidgetRequest(APIGatewayV2HTTPEvent request) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        final String body = request.getBody();
        Widget widget = gson.fromJson(body, Widget.class);

        String id = createWidget(widget);
        try {
            Widget retrievedWidget = getWidget(id);

            response.setStatusCode(201);
        
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Location", "/widgets/" + id);
            response.setHeaders(headers);

            response.setBody(gson.toJson(retrievedWidget));
        } catch (Exception ex) {
            // TODO: Log exception
            response.setStatusCode(204); // new resource not included in body
            response.setHeaders(new HashMap<>());
        }

        return response;
    }

    private String createWidget(Widget widget) {
        if (IS_MONGODB) {
            return createWidgetMongoDb(widget);
        }

        return Widgets.createWidget(widget);
    }

    private String createWidgetMongoDb(Widget widget) {
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

    private Widget getWidget(String id) throws ItemNotFoundException {
        if (IS_MONGODB) {
            return getWidgetMongoDb(id);
        }

        return Widgets.getWidget(id, true);
    }

    private Widget getWidgetMongoDb(String id) throws ItemNotFoundException {
        final String connectionString = MongoDb.getConnectionString();
        MongoClient mongoClient = MongoDb.getClient(connectionString);

        try {
            MongoCollection<Document> collection = MongoDb.getCollection(mongoClient, WIDGETS_DEMO_DB, WIDGET_COLLECTION);

            Bson filter = Filters.eq("_id", new ObjectId(id));
            Iterable<Document> widgetCursor = collection.find(filter);
            Document doc = widgetCursor.iterator().next();
        
            return MongoDb.buildWidget(doc);

        } catch (NoSuchElementException ex) {
            throw new ItemNotFoundException();
            
        } finally {
            mongoClient.close();
        }
    }
}
