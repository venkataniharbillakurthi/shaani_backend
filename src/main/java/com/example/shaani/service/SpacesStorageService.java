package com.example.shaani.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.shaani.error.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class SpacesStorageService {

	private static final Logger log = LoggerFactory.getLogger(SpacesStorageService.class);
	private static final Set<String> TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String bucket;
	private final String region;
	private final String apiToken;
	private final Object lock = new Object();
	private String accessKey;
	private String secretKey;
	private S3Client client;

	public SpacesStorageService(
			@Value("${shaani.spaces.bucket:shaaniclothing}") String bucket,
			@Value("${shaani.spaces.region:sgp1}") String region,
			@Value("${shaani.spaces.token:}") String apiToken,
			@Value("${shaani.spaces.access-key:}") String accessKey,
			@Value("${shaani.spaces.secret-key:}") String secretKey) {
		this.bucket = bucket;
		this.region = region;
		this.apiToken = apiToken == null ? "" : apiToken.trim();
		this.accessKey = accessKey == null ? "" : accessKey.trim();
		this.secretKey = secretKey == null ? "" : secretKey.trim();
	}

	public String upload(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Choose a photo to upload.");
		}
		String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
		if (!TYPES.contains(contentType)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Upload a JPG, PNG, WEBP, or GIF photo.");
		}
		String extension = switch (contentType) {
			case "image/png" -> ".png";
			case "image/webp" -> ".webp";
			case "image/gif" -> ".gif";
			default -> ".jpg";
		};
		String objectKey = "products/" + UUID.randomUUID() + extension;
		try {
			byte[] bytes = file.getBytes();
			ensureClient().putObject(PutObjectRequest.builder()
					.bucket(bucket)
					.key(objectKey)
					.contentType(contentType)
					.acl(ObjectCannedACL.PUBLIC_READ)
					.build(), RequestBody.fromBytes(bytes));
		} catch (ApiException exception) {
			throw exception;
		} catch (Exception exception) {
			log.warn("Photo upload failed: {}", exception.getMessage());
			throw new ApiException(HttpStatus.BAD_GATEWAY, "The photo could not be stored. Try again.");
		}
		return "https://" + bucket + "." + region + ".digitaloceanspaces.com/" + objectKey;
	}

	private S3Client ensureClient() {
		if (client != null) {
			return client;
		}
		synchronized (lock) {
			if (client != null) {
				return client;
			}
			if (accessKey.isBlank() || secretKey.isBlank()) {
				readSavedKeys();
			}
			if (accessKey.isBlank() || secretKey.isBlank()) {
				createSpacesKey();
			}
			client = S3Client.builder()
					.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
					.endpointOverride(URI.create("https://" + region + ".digitaloceanspaces.com"))
					.region(Region.US_EAST_1)
					.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(false).build())
					.build();
			log.info("DigitalOcean Spaces upload is ready for {}", bucket);
			return client;
		}
	}

	private void readSavedKeys() {
		Path file = credentialsFile();
		if (file == null || !Files.exists(file)) {
			return;
		}
		try (var input = Files.newInputStream(file)) {
			Properties properties = new Properties();
			properties.load(input);
			String savedKey = properties.getProperty("shaani.spaces.access-key", "");
			String savedSecret = properties.getProperty("shaani.spaces.secret-key", "");
			if (!savedKey.isBlank() && !savedSecret.isBlank()) {
				accessKey = savedKey.trim();
				secretKey = savedSecret.trim();
			}
		} catch (Exception exception) {
			log.warn("Could not read saved Spaces keys: {}", exception.getMessage());
		}
	}

	private void createSpacesKey() {
		if (apiToken.isBlank()) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Image upload is not configured.");
		}
		try {
			String keyName = "shaani-upload-" + UUID.randomUUID().toString().substring(0, 8);
			String payload = """
					{"name":"%s","grants":[{"bucket":"%s","permission":"readwrite"}]}
					""".formatted(keyName, bucket).trim();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://api.digitalocean.com/v2/spaces/keys"))
					.header("Authorization", "Bearer " + apiToken)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(payload))
					.build();
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 300) {
				log.warn("DigitalOcean Spaces key request failed with status {}", response.statusCode());
				throw new ApiException(HttpStatus.BAD_GATEWAY, "DigitalOcean did not accept the image storage setup.");
			}
			JsonNode key = objectMapper.readTree(response.body()).path("key");
			if (key.isMissingNode() || key.isNull()) {
				key = objectMapper.readTree(response.body()).path("spaces_key");
			}
			accessKey = text(key, "access_key");
			secretKey = text(key, "secret_key");
			if (secretKey.isBlank()) {
				secretKey = text(key, "secret");
			}
			if (accessKey.isBlank() || secretKey.isBlank()) {
				throw new ApiException(HttpStatus.BAD_GATEWAY, "DigitalOcean did not return storage keys.");
			}
			saveKeys();
		} catch (ApiException exception) {
			throw exception;
		} catch (Exception exception) {
			log.warn("DigitalOcean Spaces setup failed: {}", exception.getMessage());
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Image storage could not be prepared.");
		}
	}

	private void saveKeys() {
		Path file = credentialsFile();
		if (file == null) {
			log.warn("Spaces keys were created but the local config file was not found.");
			return;
		}
		try (var input = Files.newInputStream(file)) {
			Properties properties = new Properties();
			properties.load(input);
			properties.setProperty("shaani.spaces.access-key", accessKey);
			properties.setProperty("shaani.spaces.secret-key", secretKey);
			try (var output = Files.newOutputStream(file)) {
				properties.store(output, "Shaani local settings");
			}
		} catch (Exception exception) {
			log.warn("Could not save Spaces keys locally: {}", exception.getMessage());
		}
	}

	private Path credentialsFile() {
		Path direct = Path.of(".env");
		if (Files.exists(direct)) {
			return direct;
		}
		Path nested = Path.of("backend", ".env");
		if (Files.exists(nested)) {
			return nested;
		}
		return null;
	}

	private String text(JsonNode node, String field) {
		JsonNode value = node.path(field);
		return value.isMissingNode() || value.isNull() ? "" : value.asText("").trim();
	}
}
