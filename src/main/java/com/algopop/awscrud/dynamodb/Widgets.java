package com.algopop.awscrud.dynamodb;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.algopop.awscrud.ItemNotFoundException;
import com.algopop.awscrud.model.Widget;
import com.algopop.awscrud.model.WidgetCollection;


public class Widgets {
    public static final String TABLE_NAME = "Widget";
    private static final ScanRequest.Builder requestBuilder = ScanRequest.builder().tableName(TABLE_NAME).limit(100);
    private static ScanRequest scanRequest = requestBuilder.build();
    private static final String UPDATE_SCRIPT = "SET #name = :name, Cost = :cost, Weight = :weight";
    private static final Map<String, String> ATTRIBUTE_NAME_MAP = Map.of("#name", "Name");

    private Widgets() {}

    public static WidgetCollection getWidgets() {
        ScanResponse scanResponse = DynamoDb.client().scan(scanRequest);
        
        List<Widget> widgets = new ArrayList<>();
        if (scanResponse != null) {
            List<Map<String, AttributeValue>> items = scanResponse.items();

            if (items != null) {
                for (Map<String, AttributeValue> item : items) {
                    widgets.add(buildWidget(item));
                }
            }
        }

        return new WidgetCollection(widgets);
    }

    public static Widget getWidget(String id) throws ItemNotFoundException {
        return getWidget(id, false);
    }

    public static Widget getWidget(String id, boolean consistentRead) throws ItemNotFoundException {
        Map<String, AttributeValue> keyAttributes = keyAttributes(id);
        GetItemRequest getItemRequest = GetItemRequest.builder()
            .tableName(Widgets.TABLE_NAME)
            .consistentRead(consistentRead)
            .key(keyAttributes)
            .build();

        GetItemResponse getItemResponse = DynamoDb.client().getItem(getItemRequest);
        if (!getItemResponse.hasItem()) {
            throw new ItemNotFoundException(id);
        }

        return buildWidget(getItemResponse.item());
    }

    public static String createWidget(Widget widget) {
        String id = DynamoDb.generateId();
        widget.setId(id);

        Map<String, AttributeValue> item = getAttributes(widget);

        PutItemRequest putItemRequest = PutItemRequest.builder().tableName(TABLE_NAME).item(item).build();
        DynamoDb.client().putItem(putItemRequest);

        return id;
    }

    public static Widget updateWidget(Widget widget) {
        Map<String, AttributeValue> keyAttributes = keyAttributes(widget.getId());
        Map<String, AttributeValue> updateValues = updateAttributeValues(widget);

        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
            .tableName(TABLE_NAME)
            .key(keyAttributes)
            .updateExpression(UPDATE_SCRIPT)
            .expressionAttributeNames(ATTRIBUTE_NAME_MAP)
            .expressionAttributeValues(updateValues)
            .returnValues(ReturnValue.ALL_NEW)
            .build();
        UpdateItemResponse updateItemResponse = DynamoDb.client().updateItem(updateItemRequest);

        return buildWidget(updateItemResponse.attributes());
    }

    public static void deleteWidget(String id) {
        Map<String, AttributeValue> keyAttributes = keyAttributes(id);
        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder().tableName(TABLE_NAME).key(keyAttributes).build();

        DynamoDb.client().deleteItem(deleteItemRequest);
    }
    
    private static Widget buildWidget(Map<String, AttributeValue> item) {
        String id = item.get("Id").s();
        String name = item.get("Name").s();
        float cost = Float.parseFloat(item.get("Cost").n());
        float weight = Float.parseFloat(item.get("Weight").n());

        return new Widget(id, name, cost, weight);
    }

    private static Map<String, AttributeValue> keyAttributes(String id) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("Missing 'id' argument");
        }
        
        Map<String, AttributeValue> attributes = new HashMap<>();
        AttributeValue idValue = AttributeValue.builder().s(id).build();

        attributes.put("Id", idValue);
        return attributes;
    }

    private static Map<String, AttributeValue> getAttributes(Widget widget) {
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

    private static Map<String, AttributeValue> updateAttributeValues(Widget widget) {
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
