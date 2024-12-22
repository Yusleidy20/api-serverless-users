package org.example;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDBClient {
    private static final DynamoDbClient client = DynamoDbClient.builder()
            .region(Region.US_EAST_2) // Cambia la región si es necesario
            .build();

    public static DynamoDbClient getClient() {
        return client;
    }
}
