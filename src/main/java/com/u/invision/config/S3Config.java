package com.u.invision.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

	@Value("${aws.access-key}")
	private String accessKey;

	@Value("${aws.secret-key}")
	private String secretKey;

	@Value("${aws.region}")
	private String region;

	@Bean
	public S3Client s3Client() {
		String ak = accessKey.strip();
		String sk = secretKey.strip();
		AwsBasicCredentials creds = AwsBasicCredentials.create(ak, sk);

		return S3Client.builder()
				.credentialsProvider(StaticCredentialsProvider.create(creds))
				.region(Region.of(region.strip()))
				.build();
	}
}