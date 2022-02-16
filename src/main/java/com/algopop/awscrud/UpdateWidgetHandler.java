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

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

import static com.algopop.awscrud.MongoDb.WIDGETS_DEMO_DB;
import static com.algopop.awscrud.MongoDb.WIDGET_COLLECTION;
import static com.algopop.awscrud.mongodb.Widgets.widgetToDocument;


public class UpdateWidgetHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final boolean IS_MONGODB = true;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        String httpMethod = request.getRequestContext().getHttp().getMethod();

        if (httpMethod.equals("PUT")) {
            return handleUpdateWidgetRequest(request);
        }

        throw new IllegalArgumentException();  // should really return a 400 error of some kind
    }

    private APIGatewayV2HTTPResponse handleUpdateWidgetRequest(APIGatewayV2HTTPEvent request) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        final String body = request.getBody();
        Widget widget = gson.fromJson(body, Widget.class);

        String id = request.getPathParameters().get("id");
        try {
            Widget updatedWidget = updateWidget(id, widget);

            response.setStatusCode(200);
        
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            response.setHeaders(headers);

            response.setBody(gson.toJson(updatedWidget));

        } catch (Exception ex) {
            response.setStatusCode(500);
            response.setHeaders(new HashMap<>());
        }

        return response;
    }

    private Widget updateWidget(String id, Widget widget) {
        if (IS_MONGODB) {
            return updateWidgetMongoDb(id, widget);
        }

        String widgetId = widget.getId();
        if (widgetId.isBlank()) {
            widget.setId(id);
        } else if (!widgetId.equals(id)) {
            throw new IllegalArgumentException();  // should really return a 400 error of some kind
        }
        
        return Widgets.updateWidget(widget);
    }

    private Widget updateWidgetMongoDb(String id, Widget widget) {
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
}
