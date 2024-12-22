package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.Map;

public class GetUserHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            String userId = ((Map<String, String>) event.get("pathParameters")).get("id");
            if (userId == null) {
                return createResponse(400, "ID is required");
            }

            GetItemRequest request = GetItemRequest.builder()
                    .tableName("usuarios")
                    .key(Map.of("id", AttributeValue.builder().s(userId).build()))
                    .build();

            Map<String, AttributeValue> item = DynamoDBClient.getClient().getItem(request).item();

            if (item == null || item.isEmpty()) {
                return createResponse(404, "User not found");
            }

            String responseBody = String.format(
                    "{\"id\": \"%s\", \"userName\": \"%s\", \"email\": \"%s\"}",
                    item.get("id").s(), item.get("userName").s(), item.get("email").s()
            );

            return createResponse(200, responseBody);
        } catch (Exception e) {
            return createResponse(500, "Error: " + e.getMessage());
        }
    }

    private Map<String, Object> createResponse(int statusCode, String message) {
        return Map.of(
                "statusCode", statusCode,
                "headers", Map.of("Content-Type", "application/json"),
                "body", message
        );
    }
}
