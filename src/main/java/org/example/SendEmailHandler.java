package org.example;



public class SendEmailHandler implements RequestHandler<Map<String, Object>, String> {
    private static final String SNS_TOPIC_ARN = System.getenv("SNS_TOPIC_ARN");

    @Override
    public String handleRequest(Map<String, Object> event, Context context) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String messageBody = mapper.writeValueAsString(event.get("Records"));

            SnsClient snsClient = SnsClient.create();
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(SNS_TOPIC_ARN)
                    .message(messageBody)
                    .build();

            snsClient.publish(publishRequest);
            return "Message published to SNS!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}