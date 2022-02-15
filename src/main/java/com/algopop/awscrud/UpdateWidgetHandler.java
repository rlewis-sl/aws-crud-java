package com.algopop.awscrud;

import com.algopop.awscrud.dynamodb.Widgets;
import com.algopop.awscrud.model.Widget;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import static com.algopop.awscrud.dynamodb.Widgets.buildWidget;
import static com.algopop.awscrud.dynamodb.Widgets.keyAttributes;


public class UpdateWidgetHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final String UPDATE_SCRIPT = "SET #name = :name, Cost = :cost, Weight = :weight";
    private static final Map<String, String> ATTRIBUTE_NAME_MAP = Map.of("#name", "Name");

    private static final DynamoDbClientBuilder clientBuilder = DynamoDbClient.builder().region(Region.EU_WEST_1);
    private static final DynamoDbClient ddb = clientBuilder.build();

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
        Widget updatedWidget = updateWidget(id, widget);

        response.setStatusCode(200);
    
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        response.setHeaders(headers);

        response.setBody(gson.toJson(updatedWidget));

        return response;
    }

    private Widget updateWidget(String id, Widget widget) {
        String widgetId = widget.getId();
        if (widgetId.isBlank()) {
            widget.setId(id);
        } else if (!widgetId.equals(id)) {
            throw new IllegalArgumentException();  // should really return a 400 error of some kind
        }
        
        
        Map<String, AttributeValue> keyAttributes = keyAttributes(id);
        Map<String, AttributeValue> updateValues = expressionAttributeValues(widget);

        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
            .tableName(Widgets.TABLE_NAME)
            .key(keyAttributes)
            .updateExpression(UPDATE_SCRIPT)
            .expressionAttributeNames(ATTRIBUTE_NAME_MAP)
            .expressionAttributeValues(updateValues)
            .returnValues(ReturnValue.ALL_NEW)
            .build();
        UpdateItemResponse updateItemResponse = ddb.updateItem(updateItemRequest);

        return buildWidget(updateItemResponse.attributes());
    }

    private Map<String, AttributeValue> expressionAttributeValues(Widget widget) {
        String name = widget.getName();
        String cost = widget.getCost().toString();
        String weight = widget.getWeight().toString();

        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":name", AttributeValue.builder().s(name).build());
        values.put(":cost", AttributeValue.builder().n(cost).build());
        values.put(":weight", AttributeValue.builder().n(weight).build());

        return values;
    }
}
