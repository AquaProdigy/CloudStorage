package org.example.cloudstorage.repository;

import org.example.cloudstorage.model.exception.FileStorageException;
import org.example.cloudstorage.model.exception.ResourceAlreadyExistsException;
import org.example.cloudstorage.model.exception.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface ResourceRepository {
    boolean isFilePathExists(String path) throws FileStorageException;
    void assertExists(String path) throws ResourceNotFoundException;
    void assertNotExists(String path) throws ResourceAlreadyExistsException;
    long checkObjectSize(String path) throws FileStorageException;
    void deleteDirectory(String path) throws FileStorageException;
    void deleteFile(String path) throws FileStorageException;
    void createDirectory(String path) throws FileStorageException;
    List<String> getFilesFromDirectory(String path, Boolean recursive) throws FileStorageException;
    InputStream getObject(String path) throws FileStorageException;
    void copyObject(String from, String to) throws FileStorageException;
    void putResource(MultipartFile file, String path) throws FileStorageException;
}
