package com.algopop.awscrud.dynamodb;

import java.util.Random;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

public class DynamoDb {
    private static final DynamoDbClientBuilder clientBuilder = DynamoDbClient.builder().region(Region.EU_WEST_1);
    private static final DynamoDbClient ddb = clientBuilder.build();
    private static final Random random = new Random();
    
    private DynamoDb() {}

    public static DynamoDbClient client() {
        return ddb;
    }

    public static String generateId() {
        int size = 12;
        StringBuilder idBuilder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            idBuilder.append(randomChar());
        }

        return idBuilder.toString();
    }

    private static char randomChar() {
        int bigA = 65;
        int smallA = 97;
        int toUpperCase = bigA - smallA;

        boolean upperCase = false;
        int rand = random.nextInt(52);
        if (rand >= 26) {
            upperCase = true;
            rand -= 26;
        }

        int codePoint = smallA + rand + (upperCase ? toUpperCase : 0);
        return (char) codePoint;
    }

}
