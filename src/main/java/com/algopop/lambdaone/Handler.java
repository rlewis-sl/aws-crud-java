package com.algopop.lambdaone;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Handler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final DynamoDbClientBuilder clientBuilder = DynamoDbClient.builder().region(Region.EU_WEST_1);
    private static final ScanRequest.Builder requestBuilder = ScanRequest.builder().tableName("CloudBill").limit(20);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        String httpMethod = request.getRequestContext().getHttp().getMethod();

        if (httpMethod.equals("GET")) {
            return handleGetRequest();
        }

        throw new IllegalArgumentException();  // should really return a 400 error of some kind
    }

    private APIGatewayV2HTTPResponse handleGetRequest() {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        CloudBillCollection collection = getItems();

        response.setStatusCode(200);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        response.setHeaders(headers);

        response.setBody(gson.toJson(collection));

        return response;
    }

    private CloudBillCollection getItems() {

        ScanRequest scanRequest = requestBuilder.build();

        final DynamoDbClient ddb = clientBuilder.build();
        ScanResponse scanResponse = ddb.scan(scanRequest);

        List<CloudBill> list = new ArrayList<>();
        if (scanResponse != null) {
            List<Map<String, AttributeValue>> items = scanResponse.items();

            if (items != null) {
                for (Map<String, AttributeValue> item : items) {
                    list.add(buildCloudBill(item));
                }
            }
        }

        return new CloudBillCollection(list);
    }

    private CloudBill buildCloudBill(Map<String, AttributeValue> item) {
        String eventDateTime = item.get("EventDateTime").s();
        float cost = Float.parseFloat(item.get("Cost").n());
        float usage = Float.parseFloat(item.get("Usage").n());

        return new CloudBill(eventDateTime, cost, usage);
    }

    private static class CloudBill {
        private final String service = "default";
        private final String eventDateTime;
        private final float cost;
        private final float usage;

        public CloudBill(String eventDateTime, float cost, float usage) {
            this.eventDateTime = eventDateTime;
            this.cost = cost;
            this.usage = usage;
        }

        public String getService() {
            return service;
        }

        public String getEventDateTime() {
            return eventDateTime;
        }

        public float getCost() {
            return cost;
        }

        public float getUsage() {
            return usage;
        }
    }

    private static class CloudBillCollection {
        private final List<CloudBill> items;

        public CloudBillCollection(List<CloudBill> items) {
            this.items = items;
        }
    }
}
