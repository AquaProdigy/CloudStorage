package org.example.cloudstorage.initializer;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.api.ApiErrors;
import org.example.cloudstorage.model.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioInitializer {
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void init() {
        createBucketIfNotExists();
    }

    private void createBucketIfNotExists() {
        try {
            boolean isCreated = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!isCreated) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket {} created", bucketName);
            }
            log.info("Bucket {} exists", bucketName);
        } catch (Exception e) {
            log.error("Error creating bucket {}", bucketName, e);
            throw new FileStorageException(ApiErrors.UNEXPECTED_EXCEPTION.getMessage(), e);
        }
    }
}
