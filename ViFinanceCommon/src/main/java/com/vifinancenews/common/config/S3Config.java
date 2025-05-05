package com.vifinancenews.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3Config {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String ACCESS_KEY = dotenv.get("AWS_ACCESS_KEY");
    private static final String SECRET_KEY = dotenv.get("AWS_SECRET_KEY");
    private static final String REGION = dotenv.get("AWS_REGION");

    public static S3Client getClient() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);

        return S3Client.builder()
                .region(Region.of(REGION))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    public static String getBucketName() {
        return dotenv.get("AWS_BUCKET_NAME");
    }

    public static String getBaseUrl() {
        return "https://" + dotenv.get("AWS_BUCKET_NAME") + ".s3." + REGION + ".amazonaws.com/";
    }
}
