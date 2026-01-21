package org.example.cloudstorage.repository;

import org.example.cloudstorage.model.exception.ResourceNotFoundException;

public interface ResourceRepository {
    public boolean isFilePathExists(String path) throws ResourceNotFoundException;
    public long checkObjectSize(String path) throws ResourceNotFoundException;
    public void deleteDirectory(String path) throws ResourceNotFoundException;
//    public void deleteFile(String path) throws ResourceNotFoundException;
}
