package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.Map;

public class UpdateUserHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            System.out.println("Received event: " + event);

            Map<String, String> pathParameters = (Map<String, String>) event.get("pathParameters");
            String userId = pathParameters != null ? pathParameters.get("id") : null;
            if (userId == null || userId.isEmpty()) {
                return createResponse(400, "ID is required in pathParameters");
            }

            String body = (String) event.get("body");
            if (body == null || body.isEmpty()) {
                return createResponse(400, "Body is required");
            }

            ObjectMapper mapper = new ObjectMapper();
            Usuario usuario = mapper.readValue(body, Usuario.class);

            usuario.setId(userId);

            UpdateItemRequest request = UpdateItemRequest.builder()
                    .tableName("usuarios")
                    .key(Map.of("id", AttributeValue.builder().s(usuario.getId()).build()))
                    .updateExpression("SET #userName = :userName, email = :email")
                    .expressionAttributeNames(Map.of("#userName", "userName"))
                    .expressionAttributeValues(Map.of(
                            ":userName", AttributeValue.builder().s(usuario.getUserName()).build(),
                            ":email", AttributeValue.builder().s(usuario.getEmail()).build()
                    ))
                    .build();

            DynamoDBClient.getClient().updateItem(request);

            return createResponse(200, "Updated: " + usuario.getId());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();

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
