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
import java.util.NoSuchElementException;

import static com.algopop.awscrud.MongoDb.WIDGETS_DEMO_DB;
import static com.algopop.awscrud.MongoDb.WIDGET_COLLECTION;


public class GetWidgetHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final boolean IS_MONGODB = true;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        String httpMethod = request.getRequestContext().getHttp().getMethod();

        if (httpMethod.equals("GET")) {
            return handleGetRequest(request);
        }

        throw new IllegalArgumentException();  // should really return a 400 error of some kind
    }

    private APIGatewayV2HTTPResponse handleGetRequest(APIGatewayV2HTTPEvent request) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        String id = request.getPathParameters().get("id");

        try {
            Widget widget = getWidget(id);
            response.setStatusCode(200);

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");

            response.setHeaders(headers);

            response.setBody(gson.toJson(widget));

        } catch (ItemNotFoundException ex) {
            response.setStatusCode(404);
        }
        
        return response;
    }

    private Widget getWidget(String id) throws ItemNotFoundException {
        if (IS_MONGODB) {
            return getWidgetMongoDb(id);
        }

        return Widgets.getWidget(id);
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
