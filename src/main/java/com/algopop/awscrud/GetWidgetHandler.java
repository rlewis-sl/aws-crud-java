package com.algopop.awscrud;

import com.algopop.awscrud.model.Widget;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.algopop.awscrud.dynamodb.Widgets.buildWidget;
import static com.algopop.awscrud.dynamodb.Widgets.keyAttributes;


public class GetWidgetHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final boolean IS_MONGODB = true;
    private static final String TABLE_NAME = "Widget";

    private static final DynamoDbClientBuilder clientBuilder = DynamoDbClient.builder().region(Region.EU_WEST_1);
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

        Map<String, AttributeValue> keyAttributes = keyAttributes(id);
        GetItemRequest getItemRequest = GetItemRequest.builder().tableName(TABLE_NAME).key(keyAttributes).build();

        final DynamoDbClient ddb = clientBuilder.build();
        GetItemResponse getItemResponse = ddb.getItem(getItemRequest);
        if (!getItemResponse.hasItem()) {
            throw new ItemNotFoundException(id);
        }
        return buildWidget(getItemResponse.item());
    }

    private Widget getWidgetMongoDb(String id) throws ItemNotFoundException {
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

            Bson filter = Filters.eq("_id", new ObjectId(id));
            Iterable<Document> widgetCursor = collection.find(filter);
            Document doc = widgetCursor.iterator().next();
        
            return MongoDb.buildWidgetMongoDb(doc);

        } catch (NoSuchElementException ex) {
            throw new ItemNotFoundException();
            
        } finally {
            mongoClient.close();
        }
    }
}
