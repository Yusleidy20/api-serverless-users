package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;
import java.util.UUID;

public class CreateUserHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            String body = (String) event.get("body");
            if (body == null || body.isEmpty()) {
                return createResponse(400, "Body is required");
            }

            ObjectMapper mapper = new ObjectMapper();
            Usuario usuario = mapper.readValue(body, Usuario.class);

            usuario.setId(usuario.getId() == null ? UUID.randomUUID().toString() : usuario.getId());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName("usuarios")
                    .item(Map.of(
                            "id", AttributeValue.builder().s(usuario.getId()).build(),
                            "userName", AttributeValue.builder().s(usuario.getUserName()).build(),
                            "email", AttributeValue.builder().s(usuario.getEmail()).build()
                    ))
                    .build();

            DynamoDBClient.getClient().putItem(request);
            return createResponse(200, "Created: " + usuario.getId());
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
