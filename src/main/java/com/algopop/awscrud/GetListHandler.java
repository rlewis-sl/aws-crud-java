package com.algopop.awscrud;

import com.algopop.awscrud.dynamodb.Widgets;
import com.algopop.awscrud.model.WidgetCollection;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;


public class GetListHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final boolean IS_MONGODB = true;
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

        WidgetCollection collection = getItems();

        response.setStatusCode(200);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        response.setHeaders(headers);

        response.setBody(gson.toJson(collection));

        return response;
    }

    private WidgetCollection getItems() {
        if (IS_MONGODB) {
            return new WidgetCollection(com.algopop.awscrud.mongodb.Widgets.getWidgets());
        }

        return new WidgetCollection(Widgets.getWidgets());
    }
}
