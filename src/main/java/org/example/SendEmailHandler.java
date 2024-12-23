package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.List;
import java.util.Map;

public class SendEmailHandler implements RequestHandler<Map<String, Object>, String> {
    private static final String SNS_TOPIC_ARN = System.getenv("SNS_TOPIC_ARN");

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        try {
            context.getLogger().log("Received event: " + event);

            List<Map<String, Object>> records = (List<Map<String, Object>>) event.get("Records");
            ObjectMapper mapper = new ObjectMapper();

            SnsClient snsClient = SnsClient.create();
            for (Map<String, Object> record : records) {
                String messageBody = (String) record.get("body");

                PublishRequest publishRequest = PublishRequest.builder()
                        .topicArn(SNS_TOPIC_ARN)
                        .message(messageBody)
                        .build();
                snsClient.publish(publishRequest);

                context.getLogger().log("Message sent to SNS: " + messageBody);
            }

            return "Messages processed successfully!";
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
