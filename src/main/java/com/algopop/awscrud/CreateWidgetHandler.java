package com.algopop.awscrud;

import com.algopop.awscrud.dynamodb.Widgets;
import com.algopop.awscrud.model.Widget;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class CreateWidgetHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final DynamoDbClientBuilder clientBuilder = DynamoDbClient.builder().region(Region.EU_WEST_1);
    private static final DynamoDbClient ddb = clientBuilder.build();

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Random random = new Random();


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
        Widget retrievedWidget = getWidget(id);

        response.setStatusCode(201);
    
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);

        response.setBody(gson.toJson(retrievedWidget));

        return response;
    }

    private String createWidget(Widget widget) {
        String id = generateId();
        widget.setId(id);
        Map<String, AttributeValue> item = getAttributes(widget);

        PutItemRequest putItemRequest = PutItemRequest.builder().tableName(Widgets.TABLE_NAME).item(item).build();
        ddb.putItem(putItemRequest);

        return id;
    }

    private Widget getWidget(String id) {
        Map<String, AttributeValue> keyAttributes = keyAttributes(id);
        GetItemRequest getItemRequest = GetItemRequest.builder().tableName(Widgets.TABLE_NAME).consistentRead(true).key(keyAttributes).build();

        GetItemResponse getItemResponse = ddb.getItem(getItemRequest);
        return buildWidget(getItemResponse.item());
    }

    private Map<String, AttributeValue> keyAttributes(String id) {
        Map<String, AttributeValue> attributes = new HashMap<>();
        AttributeValue idValue = AttributeValue.builder().s(id).build();

        attributes.put("Id", idValue);
        return attributes;
    }

    private Map<String, AttributeValue> getAttributes(Widget widget) {
        Map<String, AttributeValue> attributes = new HashMap<>();
        AttributeValue idValue = AttributeValue.builder().s(widget.getId()).build();
        AttributeValue nameValue = AttributeValue.builder().s(widget.getName()).build();
        AttributeValue costValue = AttributeValue.builder().n(widget.getCost().toString()).build();
        AttributeValue weightValue = AttributeValue.builder().n(widget.getWeight().toString()).build();

        attributes.put("Id", idValue);
        attributes.put("Name", nameValue);
        attributes.put("Cost", costValue);
        attributes.put("Weight", weightValue);

        return attributes;
    }

    private String generateId() {
        int size = 12;
        StringBuilder idBuilder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            idBuilder.append(randomChar());
        }

        return idBuilder.toString();
    }

    private char randomChar() {
        int bigA = 65;
        int smallA = 97;
        int toUpperCase = bigA - smallA;

        boolean upperCase = false;
        int rand = random.nextInt(52);
        if (rand >= 26) {
            upperCase = true;
            rand -= 26;
        }

        int codePoint = smallA + rand + (upperCase ? toUpperCase : 0);
        return (char) codePoint;
    }

    private Widget buildWidget(Map<String, AttributeValue> attributes) {
        String id = attributes.get("Id").s();
        String name = attributes.get("Name").s();
        float cost = Float.parseFloat(attributes.get("Cost").n());
        float weight = Float.parseFloat(attributes.get("Weight").n());

        return new Widget(id, name, cost, weight);
    }

}
