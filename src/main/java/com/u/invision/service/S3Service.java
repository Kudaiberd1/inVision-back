package com.u.invision.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	@Value("${aws.region}")
	private String region;

	private final S3Client s3Client;

	public S3Service(S3Client s3Client) {
		this.s3Client = s3Client;
	}

	/**
	 * @param folder S3 prefix without leading slash, e.g. {@code cv}, {@code essay}, {@code videos}
	 */
	public String uploadFile(MultipartFile file, String folder) throws IOException {
		String dir = folder == null ? "" : folder.replaceAll("^/+|/+$", "");
		String safeName = sanitizeFilename(file.getOriginalFilename());
		String key = dir.isEmpty() ? UUID.randomUUID() + "_" + safeName : dir + "/" + UUID.randomUUID() + "_" + safeName;

		long contentLength = file.getSize();
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.contentType(file.getContentType())
				.contentLength(contentLength)
				.build();

		s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), contentLength));

		return publicObjectUrl(key);
	}

	public String uploadBytes(byte[] data, String originalFilename, String contentType, String folder) {
		String dir = folder == null ? "" : folder.replaceAll("^/+|/+$", "");
		String safeName = sanitizeFilename(originalFilename);
		String key = dir.isEmpty() ? UUID.randomUUID() + "_" + safeName : dir + "/" + UUID.randomUUID() + "_" + safeName;
		long contentLength = data.length;

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(key)
				.contentType(contentType != null ? contentType : "application/octet-stream")
				.contentLength(contentLength)
				.build();

		s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));

		return publicObjectUrl(key);
	}

	public String publicObjectUrl(String key) {
		String base = "https://%s.s3.%s.amazonaws.com/".formatted(bucketName, region);
		return base + UriUtils.encodePath(key, StandardCharsets.UTF_8);
	}

	/**
	 * Downloads an object whose {@linkplain #publicObjectUrl(String) public URL} is {@code urlString}.
	 * Path segments in the URL must match the configured bucket (virtual-hosted style).
	 */
	public byte[] downloadBytesFromPublicUrl(String urlString) {
		if (urlString == null || urlString.isBlank()) {
			throw new IllegalArgumentException("URL is blank");
		}
		URI uri = URI.create(urlString.trim());
		String path = uri.getPath();
		if (path == null || path.isEmpty() || "/".equals(path)) {
			throw new IllegalArgumentException("URL has no object key path");
		}
		String key = path.startsWith("/") ? path.substring(1) : path;
		key = URLDecoder.decode(key, StandardCharsets.UTF_8);

		GetObjectRequest req = GetObjectRequest.builder().bucket(bucketName).key(key).build();
		return s3Client.getObjectAsBytes(req).asByteArray();
	}

	private static String sanitizeFilename(String original) {
		if (original == null || original.isBlank()) {
			return "file";
		}
		String name = original.replace('\\', '/');
		int slash = name.lastIndexOf('/');
		if (slash >= 0) {
			name = name.substring(slash + 1);
		}
		return name.replaceAll("[^a-zA-Z0-9._-]", "_");
	}
}
