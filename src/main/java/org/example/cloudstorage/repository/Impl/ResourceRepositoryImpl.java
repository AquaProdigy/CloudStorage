package org.example.cloudstorage.repository.Impl;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.model.exception.ResourceNotFoundException;
import org.example.cloudstorage.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ResourceRepositoryImpl implements ResourceRepository {
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    public boolean isFilePathExists(String path) throws ResourceNotFoundException {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                    .build());
            return results.iterator().hasNext();
        } catch (Exception ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        }
    }

    @Override
    public long checkObjectSize(String path) throws ResourceNotFoundException {
        try {
            StatObjectResponse statObject = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
            return statObject.size();
        } catch (Exception ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        }
    }

    @Override
    public void deleteDirectory(String path) throws ResourceNotFoundException {
        try {
            Iterable<Result<Item>> results =  minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(path)
                    .recursive(true)
                    .build());

           List<DeleteObject> deleteObjects = new ArrayList<>();


           for (Result<Item> result : results) {
               Item item = result.get();
               System.out.println(item.objectName());
               deleteObjects.add(new DeleteObject(item.objectName()));
           }

            Iterable<Result<DeleteError>> errors = minioClient.removeObjects(RemoveObjectsArgs.builder()
                   .bucket(bucketName)
                   .objects(deleteObjects)
                   .build());

            for (Result<DeleteError> error : errors) {
                DeleteError e = error.get();
                throw new RuntimeException("Failed to delete " + e.objectName() + ": " + e.message());
            }
        } catch (Exception ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        }
    }



}
