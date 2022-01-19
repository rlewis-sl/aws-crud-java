package com.algopop.lambdaone;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Handler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        response.setStatusCode(200);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        response.setHeaders(headers);

        List<CloudBill> list = List.of(new CloudBill(), new CloudBill(), new CloudBill());
        CloudBillCollection collection = new CloudBillCollection(list);

        response.setBody(gson.toJson(collection));

        return response;
    }

    private static class CloudBill {
        private final String eventDateTime;
        private final float cost;
        private final float usage;

        public CloudBill() {
            this.eventDateTime = "2022-01-19T14:00:00Z";
            this.cost = 0.0f;
            this.usage = 100.0f;
        }
        
    }

    private static class CloudBillCollection {
        private final List<CloudBill> list;

        public CloudBillCollection(List<CloudBill> list) {
            this.list = list;
        }
    }
}
