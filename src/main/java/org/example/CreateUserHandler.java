package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;
import java.util.UUID;

public class CreateUserHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private static final String QUEUE_URL = System.getenv("USER_CREATION_QUEUE_URL");

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            context.getLogger().log("Received event: " + event);

            String body = (String) event.get("body");
            if (body == null || body.isEmpty()) {
                return createResponse(400, "Body is required");
            }

            ObjectMapper mapper = new ObjectMapper();
            Usuario usuario = mapper.readValue(body, Usuario.class);
            usuario.setId(usuario.getId() == null ? UUID.randomUUID().toString() : usuario.getId());

            SqsClient sqsClient = SqsClient.create();
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(QUEUE_URL)
                    .messageBody(mapper.writeValueAsString(usuario))
                    .build();
            sqsClient.sendMessage(sendMessageRequest);

            context.getLogger().log("Message sent to SQS: " + usuario.getId());
            return createResponse(200, "User created and message sent to SQS: " + usuario.getId());
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
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
