package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;

import java.util.Map;

public class DeleteUserHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            String userId = ((Map<String, String>) event.get("pathParameters")).get("id");
            if (userId == null) {
                return createResponse(400, "ID is required");
            }

            DeleteItemRequest request = DeleteItemRequest.builder()
                    .tableName("usuarios")
                    .key(Map.of("id", AttributeValue.builder().s(userId).build()))
                    .build();

            DynamoDBClient.getClient().deleteItem(request);
            return createResponse(200, "Deleted: " + userId);
        } catch (Exception e) {
            return createResponse(500, "Error: " + e.getMessage());
        }
    }

    private Map<String, Object> createResponse(int statusCode, String message) {
        return Map.of(
                "statusCode", statusCode,
                "headers", Map.of("Content-Type", "application/json"),
                "body", String.format("{\"message\": \"%s\"}", message)
        );
    }
}
