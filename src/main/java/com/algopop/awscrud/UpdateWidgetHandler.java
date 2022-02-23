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

import static com.algopop.awscrud.singular.Widgets.updateWidget;

public class UpdateWidgetHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        String httpMethod = request.getRequestContext().getHttp().getMethod();

        if (httpMethod.equals("PUT")) {
            return handleUpdateWidgetRequest(request);
        }

        throw new IllegalArgumentException(); // should really return a 400 error of some kind
    }

    private APIGatewayV2HTTPResponse handleUpdateWidgetRequest(APIGatewayV2HTTPEvent request) {
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

        String id = request.getPathParameters().get("id");
        try {
            ensureCorrectId(widget, id);
            Widget updatedWidget = updateWidget(widget);

            response.setStatusCode(200);

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            response.setHeaders(headers);

            response.setBody(gson.toJson(updatedWidget));

        } catch (Exception ex) {
            response.setStatusCode(500);
            response.setHeaders(new HashMap<>());
        }

        return response;
    }

    private void ensureCorrectId(Widget widget, String id) {
        String widgetId = widget.getId();
        if (widgetId.isBlank()) {
            widget.setId(id);
        } else if (!widgetId.equals(id)) {
            throw new IllegalArgumentException(); // should really return a 400 error of some kind
        }
    }
}
