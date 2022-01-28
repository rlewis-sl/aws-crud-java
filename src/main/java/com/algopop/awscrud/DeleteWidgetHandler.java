package com.algopop.awscrud;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import static com.algopop.awscrud.dynamodb.Widgets.keyAttributes;

import java.util.Map;


public class DeleteWidgetHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final String TABLE_NAME = "Widget";

    private static final DynamoDbClientBuilder clientBuilder = DynamoDbClient.builder().region(Region.EU_WEST_1);


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
        Map<String, AttributeValue> keyAttributes = keyAttributes(id);
        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder().tableName(TABLE_NAME).key(keyAttributes).build();

        final DynamoDbClient ddb = clientBuilder.build();
        ddb.deleteItem(deleteItemRequest);
    }
}
