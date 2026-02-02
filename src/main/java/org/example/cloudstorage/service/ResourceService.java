package org.example.cloudstorage.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.api.ApiErrors;
import org.example.cloudstorage.model.dto.ResourceDTO;
import org.example.cloudstorage.model.entity.User;
import org.example.cloudstorage.model.enums.TypeResource;
import org.example.cloudstorage.model.exception.FileStorageException;
import org.example.cloudstorage.model.exception.InvalidPathResourceException;
import org.example.cloudstorage.model.exception.ResourceAlreadyExistsException;
import org.example.cloudstorage.model.exception.ResourceNotFoundException;
import org.example.cloudstorage.repository.Impl.UserRepository;
import org.example.cloudstorage.repository.ResourceRepository;
import org.example.cloudstorage.util.PathUtil;
import org.springframework.core.io.Resource;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
@Service
@Slf4j
public class ResourceService {
    private final ResourceRepository resourceRepository;

    private ResourceDTO toResourceDTO(String fullUserPath, Long userId) {
        boolean isDirectory = PathUtil.isDirectory(fullUserPath);
        String clearUserPath = PathUtil.removeRootPath(fullUserPath, userId);

        return ResourceDTO
                .builder()
                .path(PathUtil.getParentPath(clearUserPath))
                .name(isDirectory ? PathUtil.getFileName(clearUserPath) + "/" : PathUtil.getFileName(clearUserPath))
                .size(isDirectory ? null : resourceRepository.checkObjectSize(fullUserPath))
                .type(isDirectory ? TypeResource.DIRECTORY : TypeResource.FILE)
                .build();
    }

    public ResourceDTO getInfoResource(Long userId, String path) {
        String fullUserPath = PathUtil.buildUserFullPath(userId, path);
        log.info("Getting resource by path {}", fullUserPath);

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
        log.info("Creating root directory {}", rootUserPath);
        resourceRepository.assertNotExists(rootUserPath);

        resourceRepository.createDirectory(rootUserPath);
    }

    public ResourceDTO createDirectory(Long userId, String path) {
        String fullUserPath = PathUtil.buildUserFullPath(userId, path);
        log.info("Creating directory {}", fullUserPath);
        if (!PathUtil.isDirectory(fullUserPath)) {
            throw new InvalidPathResourceException(ApiErrors.INVALID_PATH.getMessage().formatted(path));
        }

        resourceRepository.assertExists(PathUtil.getParentPath(fullUserPath));
        resourceRepository.assertNotExists(fullUserPath);

        resourceRepository.createDirectory(fullUserPath);


        return toResourceDTO(fullUserPath, userId);
    }


    public List<ResourceDTO> listDirectories(Long userId, String path) {
        String fullUserPath = PathUtil.buildUserFullPath(userId, path);
        log.info("Listing directory {}", fullUserPath);
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
        log.info("Downloading file {}", path);
        return outputStream -> {
            try (InputStream is = resourceRepository.getObject(path)) {
                is.transferTo(outputStream);
            }
        };
    }

    private StreamingResponseBody downloadDirectory(String path) {
        List<String> allObjects = resourceRepository.getFilesFromDirectory(path, true);
        log.info("Downloading directory {}", path);
        return outputStream -> {
          try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
              for (String file : allObjects) {

                  String zipEntryName = file.substring(path.length());
                  System.out.println();
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

    private ResourceDTO moveOrRenameFile(String from, String to, Long userId) {
        resourceRepository.copyObject(from, to);
        resourceRepository.deleteFile(from);
        return toResourceDTO(to, userId);
    }

    private ResourceDTO moveResource(String from, String to, Long userId) {
        List<String> files = resourceRepository.getFilesFromDirectory(from, true);

        for (String file : files) {
            if (PathUtil.isDirectory(file)) {
                if (!resourceRepository.isFilePathExists(to + PathUtil.getFileName(file) + "/")) {
                    createDirectory(userId, to + PathUtil.getFileName(file) + "/");
                }
                continue;
            }
            resourceRepository.copyObject(file, file.replace(from, to));
        }
        deleteResource(userId, from);
        return toResourceDTO(to, userId);

    }

    public ResourceDTO moveOrRename(Long userId, String from, String to) {
        String fullUserFromPath = PathUtil.buildUserFullPath(userId, from);
        String fullUserToPath = PathUtil.buildUserFullPath(userId, to);

        resourceRepository.assertExists(fullUserFromPath);
        resourceRepository.assertNotExists(fullUserToPath);

        log.info("Moving file from {} to {}", fullUserFromPath, fullUserToPath);

        if (fullUserToPath.startsWith(fullUserFromPath)) {
            log.debug("Moving file from {} to {}", fullUserFromPath, fullUserToPath);
            throw new InvalidPathResourceException(ApiErrors.INVALID_PATH.getMessage());
        }

        boolean isDirectoryFromPath = PathUtil.isDirectory(fullUserFromPath);

        if (isDirectoryFromPath) {
            createDirectory(userId, fullUserToPath);
            return moveResource(fullUserFromPath, fullUserToPath, userId);
        }

        return moveOrRenameFile(fullUserFromPath, fullUserToPath, userId);
    }

    public List<ResourceDTO> uploadResources(Long userId, String path, MultipartFile[] files) {
        String fullUserPath = PathUtil.buildUserFullPath(userId, path);
        log.info("Uploading file: {}", fullUserPath);
        resourceRepository.assertExists(fullUserPath);

        List<MultipartFile> multipartFiles = Arrays.asList(files);
        List<ResourceDTO> resourceDTOS = new ArrayList<>();

        for (MultipartFile file : multipartFiles) {
            String fileNameOriginal = file.getOriginalFilename();
            resourceRepository.assertNotExists(fullUserPath + fileNameOriginal);

            if (fileNameOriginal.contains("/")) {
                String[] dirsPath = fileNameOriginal.substring(0, fileNameOriginal.lastIndexOf("/")).split("/");

                StringBuilder currentPath = new StringBuilder(fullUserPath);

                for (String dir : dirsPath) {
                    currentPath.append(dir).append("/");
                    if (!resourceRepository.isFilePathExists(currentPath.toString())) {
                        createDirectory(userId, currentPath.toString());
                    }
                }
            }
            resourceRepository.putResource(file, fullUserPath + fileNameOriginal);
            resourceDTOS.add(toResourceDTO(fullUserPath + fileNameOriginal, userId));
        }

        return resourceDTOS;
    }

    public List<ResourceDTO> searchResources(Long userId, String query) {
        if (query.isBlank()) {
            throw new IllegalArgumentException(ApiErrors.QUERY_IS_BLANK.getMessage());
        }
        String rootPath = PathUtil.buildRootPath(userId);
        List<String> files = resourceRepository.getFilesFromDirectory(rootPath, true);

        return files.stream()
                .filter(str -> PathUtil.getFileName(str).toLowerCase().contains(query.toLowerCase()))
                .map(str -> toResourceDTO(str, userId))
                .toList();
    }
}
