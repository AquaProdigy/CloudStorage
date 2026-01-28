package org.example.cloudstorage.controller;


import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.model.dto.ResourceDTO;
import org.example.cloudstorage.repository.ResourceRepository;
import org.example.cloudstorage.security.UserDetails;
import org.example.cloudstorage.service.ResourceService;
import org.example.cloudstorage.util.PathUtil;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ResourceController {
    @PostMapping(
            value = "/resource"
    )
    public ResponseEntity<List<ResourceDTO>> uploadResource(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "path") String path,
            @RequestParam(name = "object") MultipartFile[] files
    ) {
        List<ResourceDTO> resourceDTOS = resourceService.uploadResources(userDetails.getUser().getId(), path, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceDTOS);
    }

    private final ResourceService resourceService;


    @GetMapping("/resource")
    public ResponseEntity<ResourceDTO> getResource(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "path") String path
    ) {
        ResourceDTO resourceDTO = resourceService.getInfoResource(userDetails.getUser().getId(), path);
        return ResponseEntity.ok(resourceDTO);
    }

    @DeleteMapping("/resource")
    public ResponseEntity<Void> deleteResource(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "path") String path
    ) {
        resourceService.deleteResource(userDetails.getUser().getId(), path);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/directory")
    public ResponseEntity<ResourceDTO> createDirectory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "path") String path
    ) {
        ResourceDTO resourceDTO = resourceService.createDirectory(userDetails.getUser().getId(), path);
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceDTO);
    }

    @GetMapping("/directory")
    public ResponseEntity<List<ResourceDTO>> getResources(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "path") String path
    ) {

        List<ResourceDTO> resourceDTOS = resourceService.listDirectories(userDetails.getUser().getId(), path);
        return ResponseEntity.ok(resourceDTOS);
    }

    @GetMapping("/resource/download")
    public ResponseEntity<StreamingResponseBody> download(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "path") String path
    ) {
        StreamingResponseBody stream = resourceService.downloadResource(userDetails.getUser().getId(), path);

        String filename = PathUtil.isDirectory(path) ?
                PathUtil.getFileName(path) + ".zip" :
                PathUtil.getFileName(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
    }


    @GetMapping("/resource/move")
    public ResponseEntity<ResourceDTO> moveOrRenameResource(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "from") String from,
            @RequestParam(name = "to") String to
    ) {
        ResourceDTO resourceDTO = resourceService.moveOrRename(
                userDetails.getUser().getId(),
                from,
                to
        );

        return ResponseEntity.ok(resourceDTO);
    }


    @GetMapping("/resource/search")
    public ResponseEntity<List<ResourceDTO>> search(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "query") String query
    ) {
        List<ResourceDTO> resourceDTOS = resourceService.searchResources(userDetails.getUser().getId(), query);
        return ResponseEntity.ok(resourceDTOS);
    }

}
