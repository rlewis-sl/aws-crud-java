package com.algopop.awscrud;

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
import java.util.List;
import java.util.Map;


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

        Widget widget = getWidget(id);

        response.setStatusCode(200);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        response.setHeaders(headers);

        response.setBody(gson.toJson(widget));

        return response;
    }

    private Widget getWidget(String id) {
        Map<String, AttributeValue> keyAttributes = keyAttributes(id);
        GetItemRequest getItemRequest = GetItemRequest.builder().tableName(TABLE_NAME).key(keyAttributes).build();

        final DynamoDbClient ddb = clientBuilder.build();
        GetItemResponse getItemResponse = ddb.getItem(getItemRequest);
        return buildWidget(getItemResponse.item());
    }

    private Map<String, AttributeValue> keyAttributes(String id) {
        Map<String, AttributeValue> attributes = new HashMap<>();
        AttributeValue idValue = AttributeValue.builder().s(id).build();

        attributes.put("Id", idValue);
        return attributes;
    }

    private Widget buildWidget(Map<String, AttributeValue> item) {
        String id = item.get("Id").s();
        String name = item.get("Name").s();
        float cost = Float.parseFloat(item.get("Cost").n());
        float weight = Float.parseFloat(item.get("Weight").n());

        return new Widget(id, name, cost, weight);
    }

    private static class Widget {
        private final String id;
        private final String name;
        private final float cost;
        private final float weight;

        public Widget(String id, String name, float cost, float weight) {
            this.id = id;
            this.name = name;
            this.cost = cost;
            this.weight = weight;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public float getCost() {
            return cost;
        }

        public float getWeight() {
            return weight;
        }
    }

    private static class WidgetCollection {
        private final List<Widget> items;

        public WidgetCollection(List<Widget> items) {
            this.items = items;
        }
    }
}
