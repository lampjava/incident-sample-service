package org.chiwooplatform.incident.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;

@Configuration
public class AwsConfiguration {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.kinesis.access.id}")
    private String accessKey;

    @Value("${aws.kinesis.access.secret}")
    private String secretKey;

    @Bean
    public Regions regions() {
        return Regions.fromName( region );
    }

    @Bean
    public AWSCredentialsProvider credentialsProvider() {
        return new AWSStaticCredentialsProvider( new BasicAWSCredentials( accessKey, secretKey ) );
        // return new AWSCredentialsProviderChain( new ProfileCredentialsProvider("tnt-dev") );
    }
}
