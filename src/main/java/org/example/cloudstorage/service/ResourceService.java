package org.example.cloudstorage.service;


import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.model.dto.ResourceDTO;
import org.example.cloudstorage.model.entity.User;
import org.example.cloudstorage.model.enums.TypeResource;
import org.example.cloudstorage.model.exception.ResourceNotFoundException;
import org.example.cloudstorage.repository.Impl.UserRepository;
import org.example.cloudstorage.repository.ResourceRepository;
import org.example.cloudstorage.util.PathUtil;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ResourceService {
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ResourceDTO getInfoResource(String username, String path) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Username not found: %s".formatted(username)));

        String fullUserPath = PathUtil.buildUserFullPath(user.getId(), path);

        if (!resourceRepository.isFilePathExists(fullUserPath)) {
            throw new ResourceNotFoundException("File not found: %s".formatted(path));
        }

        boolean isDirectory = PathUtil.isDirectory(fullUserPath);
        Long size = isDirectory ? null
                : resourceRepository.checkObjectSize(fullUserPath);


        return ResourceDTO.builder()
                .path(PathUtil.getParentPath(path))
                .name(PathUtil.getFileName(path))
                .size(size)
                .type(isDirectory ? TypeResource.DIRECTORY : TypeResource.FILE)
                .build();
    }

    public void deleteResource(String username, String path) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Username not found: %s".formatted(username)));

        String fullUserPath = PathUtil.buildUserFullPath(user.getId(), path);

        if (!resourceRepository.isFilePathExists(fullUserPath)) {
            throw new ResourceNotFoundException("File not found: %s".formatted(path));
        }

        boolean isDirectory = PathUtil.isDirectory(fullUserPath);

        resourceRepository.deleteDirectory(fullUserPath);

    }

}
