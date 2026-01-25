package org.example.cloudstorage.service;


import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.model.dto.ResourceDTO;
import org.example.cloudstorage.model.entity.User;
import org.example.cloudstorage.model.enums.TypeResource;
import org.example.cloudstorage.model.exception.InvalidPathResourceException;
import org.example.cloudstorage.model.exception.ResourceAlreadyExistsException;
import org.example.cloudstorage.model.exception.ResourceNotFoundException;
import org.example.cloudstorage.repository.Impl.UserRepository;
import org.example.cloudstorage.repository.ResourceRepository;
import org.example.cloudstorage.util.PathUtil;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
@Service
public class ResourceService {
    private final ResourceRepository resourceRepository;
    private static final int BUFFER_SIZE = 1024;


    private ResourceDTO toResourceDTO(String fullUserPath, Long userId) {
        boolean isDirectory = PathUtil.isDirectory(fullUserPath);

        return ResourceDTO
                .builder()
                .path(PathUtil.getParentPath(PathUtil.removeRootPath(fullUserPath, userId)))
                .name(PathUtil.getFileName(fullUserPath))
                .size(isDirectory ? null : resourceRepository.checkObjectSize(fullUserPath))
                .type(isDirectory ? TypeResource.DIRECTORY : TypeResource.FILE)
                .build();
    }

    public ResourceDTO getInfoResource(Long userId, String path) {
        String fullUserPath = PathUtil.buildUserFullPath(userId, path);

        resourceRepository.assertExists(fullUserPath);

        return toResourceDTO(fullUserPath, userId);
    }

    public void deleteResource(Long userId, String path) {
        String fullUserPath = PathUtil.buildUserFullPath(userId, path);

        resourceRepository.assertExists(fullUserPath);

        if (PathUtil.isDirectory(fullUserPath)) {
            resourceRepository.deleteDirectory(fullUserPath);
        } else {
            resourceRepository.deleteFile(fullUserPath);
        }

    }

    public void createRootDirectory(Long userId) {
        String rootUserPath = PathUtil.buildRootPath(userId);
        resourceRepository.assertNotExists(rootUserPath);

        resourceRepository.createDirectory(rootUserPath);
    }

    public ResourceDTO createDirectory(Long userId, String path) {
        String fullUserPath = PathUtil.buildUserFullPath(userId, path);
        if (!PathUtil.isDirectory(fullUserPath)) {
            throw new InvalidPathResourceException("Invalid path");
        }

        resourceRepository.assertExists(PathUtil.getParentPath(fullUserPath));
        resourceRepository.assertNotExists(fullUserPath);

        resourceRepository.createDirectory(fullUserPath);

        return toResourceDTO(fullUserPath, userId);
    }


    public List<ResourceDTO> listDirectories(Long userId, String path) {
        String fullUserPath = PathUtil.buildUserFullPath(userId, path);

        boolean isDirectory = PathUtil.isDirectory(fullUserPath);
        if (!isDirectory) {
            throw new InvalidParameterException("Not valid directory: %s".formatted(path));
        }

        resourceRepository.assertExists(fullUserPath);

        List<String> results = resourceRepository.getFilesFromDirectory(fullUserPath, false);
        List<ResourceDTO> resourceDTOS = new ArrayList<>();

        for (String file : results) {
            resourceDTOS.add(toResourceDTO(file, userId));
        }

        return resourceDTOS;
    }

    private StreamingResponseBody downloadFile(String path) {
        return outputStream -> {
            try (InputStream is = resourceRepository.getObject(path)) {
                is.transferTo(outputStream);
            }
        };
    }

    private StreamingResponseBody downloadDirectory(String path) {
        List<String> allObjects = resourceRepository.getFilesFromDirectory(path, true);

        return outputStream -> {
          try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
              for (String file : allObjects) {

                  String zipEntryName = file.substring(path.length());

                  if (PathUtil.isDirectory(file)) {
                      zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
                      zipOutputStream.closeEntry();
                      continue;
                  }

                  zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
                  try (InputStream inputStream = resourceRepository.getObject(file)) {
                      inputStream.transferTo(zipOutputStream);
                  }
                  zipOutputStream.closeEntry();
              }
          }
        };

    }

    public StreamingResponseBody downloadResource(Long userId, String path) {
        String fullUserPath = PathUtil.buildUserFullPath(userId, path);
        resourceRepository.assertExists(fullUserPath);

        if (PathUtil.isDirectory(fullUserPath)) {
            return downloadDirectory(fullUserPath);
        }
        return downloadFile(fullUserPath);

    }

    private ResourceDTO renameResource(String from, String to, Long userId) {
        resourceRepository.copyObject(from, to);
        resourceRepository.deleteFile(from);
        return toResourceDTO(to, userId);
    }

    private void moveResource(String from, String to, Long userId) {

    }

    public ResourceDTO moveOrRename(Long userId, String from, String to) {
        String fullUserFromPath = PathUtil.buildUserFullPath(userId, from);
        String fullUserToPath = PathUtil.buildUserFullPath(userId, to);

        resourceRepository.assertExists(fullUserFromPath);
        resourceRepository.assertNotExists(fullUserToPath);

        if (PathUtil.isRenameAction(from, to)) {
            return renameResource(fullUserFromPath, fullUserToPath, userId);
        }

        return toResourceDTO(fullUserToPath, userId);
    }
}
