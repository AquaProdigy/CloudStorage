package org.example.cloudstorage.repository.Impl;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.api.ApiErrors;
import org.example.cloudstorage.model.exception.FileStorageException;
import org.example.cloudstorage.model.exception.ResourceAlreadyExistsException;
import org.example.cloudstorage.model.exception.ResourceNotFoundException;
import org.example.cloudstorage.repository.ResourceRepository;
import org.example.cloudstorage.util.PathUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceRepositoryImpl implements ResourceRepository {
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    public boolean isFilePathExists(String path) throws FileStorageException {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                    .build());
            return results.iterator().hasNext();
        } catch (Exception ex) {
            throw new FileStorageException(ApiErrors.UNEXPECTED_EXCEPTION.getMessage(), ex);
        }
    }

    @Override
    public void assertExists(String path) throws ResourceNotFoundException {
        if (!isFilePathExists(path)) {
            throw new ResourceNotFoundException(ApiErrors.RESOURCE_NOT_FOUND.getMessage().formatted(path));
        }
    }

    @Override
    public void assertNotExists(String path) throws ResourceAlreadyExistsException {
        if (isFilePathExists(path)) {
            throw new ResourceAlreadyExistsException(ApiErrors.RESOURCE_ALREADY_EXISTS.getMessage().formatted(path));
        }
    }

    @Override
    public long checkObjectSize(String path) throws FileStorageException {
        try {
            StatObjectResponse statObject = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
            return statObject.size();
        } catch (Exception ex) {
            throw new FileStorageException(ApiErrors.UNEXPECTED_EXCEPTION.getMessage(), ex);
        }
    }

    @Override
    public void deleteDirectory(String path) throws FileStorageException {
        try {
            Iterable<Result<Item>> results =  minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(path)
                    .recursive(true)
                    .build());

           List<DeleteObject> deleteObjects = new ArrayList<>();


           for (Result<Item> result : results) {
               Item item = result.get();
               deleteObjects.add(new DeleteObject(item.objectName()));
           }

            Iterable<Result<DeleteError>> errors = minioClient.removeObjects(RemoveObjectsArgs.builder()
                   .bucket(bucketName)
                   .objects(deleteObjects)
                   .build());

            for (Result<DeleteError> error : errors) {
                DeleteError e = error.get();
                log.error(e.objectName(), e.message());
            }
        } catch (Exception ex) {
            throw new FileStorageException(ApiErrors.UNEXPECTED_EXCEPTION.getMessage(), ex);
        }
    }

    @Override
    public void deleteFile(String path) throws FileStorageException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                    .build());
        } catch (Exception ex) {
            throw new FileStorageException(ApiErrors.UNEXPECTED_EXCEPTION.getMessage(), ex);
        }
    }

    @Override
    public void createDirectory(String path) throws FileStorageException {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build()
            );

        } catch (Exception ex) {
            throw new FileStorageException(ApiErrors.UNEXPECTED_EXCEPTION.getMessage(), ex);
        }


    }

    @Override
    public List<String> getFilesFromDirectory(String path, Boolean recursive) throws RuntimeException {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                            .recursive(recursive)
                    .build());

            List<String> files = new ArrayList<>();
            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.objectName().equals(path)) {
                    files.add(item.objectName());
                }
            }

            return files;
        } catch (Exception ex) {
            throw new RuntimeException(ApiErrors.UNEXPECTED_EXCEPTION.getMessage(), ex);
        }
    }

    @Override
    public InputStream getObject(String path) throws FileStorageException {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                    .build());
        } catch (Exception ex) {
            throw new FileStorageException(ApiErrors.UNEXPECTED_EXCEPTION.getMessage(), ex);
        }
    }

    @Override
    public void copyObject(String from, String to) throws FileStorageException {
        try {
            minioClient.copyObject(CopyObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(to)
                    .source(CopySource
                            .builder()
                            .bucket(bucketName)
                            .object(from)
                            .build())
                    .build());
        }catch (Exception ex) {
            throw new FileStorageException(ApiErrors.UNEXPECTED_EXCEPTION.getMessage(), ex);
        }
    }



}
