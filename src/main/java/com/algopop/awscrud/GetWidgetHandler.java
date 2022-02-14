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

import java.util.HashMap;
import java.util.Map;

import static com.algopop.awscrud.dynamodb.Widgets.buildWidget;
import static com.algopop.awscrud.dynamodb.Widgets.keyAttributes;


public class GetWidgetHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
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
        Map<String, AttributeValue> keyAttributes = keyAttributes(id);
        GetItemRequest getItemRequest = GetItemRequest.builder().tableName(TABLE_NAME).key(keyAttributes).build();

        final DynamoDbClient ddb = clientBuilder.build();
        GetItemResponse getItemResponse = ddb.getItem(getItemRequest);
        if (!getItemResponse.hasItem()) {
            throw new ItemNotFoundException(id);
        }
        return buildWidget(getItemResponse.item());
    }
}
