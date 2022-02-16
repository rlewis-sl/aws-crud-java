package com.algopop.awscrud;

import com.algopop.awscrud.dynamodb.Widgets;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import static com.algopop.awscrud.MongoDb.WIDGETS_DEMO_DB;
import static com.algopop.awscrud.MongoDb.WIDGET_COLLECTION;


public class DeleteWidgetHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final boolean IS_MONGODB = true;

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        String httpMethod = request.getRequestContext().getHttp().getMethod();

        if (httpMethod.equals("DELETE")) {
            return handleDeleteRequest(request);
        }

        throw new IllegalArgumentException();  // should really return a 400 error of some kind
    }

    private APIGatewayV2HTTPResponse handleDeleteRequest(APIGatewayV2HTTPEvent request) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        String id = request.getPathParameters().get("id");

        deleteWidget(id);

        response.setStatusCode(204); // no content

        return response;
    }

    private void deleteWidget(String id) {
        if (IS_MONGODB) {
            deleteWidgetMongoDb(id);
        } else {
            Widgets.deleteWidget(id);
        }
    }

    private void deleteWidgetMongoDb(String id) {
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
}
