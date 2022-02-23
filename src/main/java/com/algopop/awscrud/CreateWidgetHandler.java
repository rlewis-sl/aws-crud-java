package com.algopop.awscrud;

import com.algopop.awscrud.json.WidgetParsing;
import com.algopop.awscrud.model.Widget;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import static com.algopop.awscrud.dynamodb.Widgets.createWidget;
import static com.algopop.awscrud.dynamodb.Widgets.getWidget;

public class CreateWidgetHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        String httpMethod = request.getRequestContext().getHttp().getMethod();

        if (httpMethod.equals("POST")) {
            return handleCreateWidgetRequest(request);
        }

        throw new IllegalArgumentException(); // should really return a 400 error of some kind
    }

    private APIGatewayV2HTTPResponse handleCreateWidgetRequest(APIGatewayV2HTTPEvent request) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        final String body = request.getBody();
        Widget widget;

        try {
            widget = WidgetParsing.getWidget(body);
        } catch (Exception ex) {
            response.setStatusCode(400);

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "text/plain");
            response.setHeaders(headers);

            response.setBody("Invalid JSON [" + ex.toString() + "]");
            return response;
        }

        String id = createWidget(widget);
        try {
            Widget retrievedWidget = getWidget(id, true);

            response.setStatusCode(201);

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Location", "/widgets/" + id);
            response.setHeaders(headers);

            response.setBody(gson.toJson(retrievedWidget));
        } catch (Exception ex) {
            // TODO: Log exception
            response.setStatusCode(204); // new resource not included in body
            response.setHeaders(new HashMap<>());
        }

        return response;
    }
}
