package org.example.cloudstorage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.model.dto.ResourceDTO;
import org.example.cloudstorage.security.UserDetails;
import org.example.cloudstorage.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Directory")
@RestController
@RequestMapping("${api}")
@RequiredArgsConstructor
public class DirectoryController {
    private final ResourceService resourceService;

    @PostMapping("${directory}")
    public ResponseEntity<ResourceDTO> createDirectory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "path") String path
    ) {
        ResourceDTO resourceDTO = resourceService.createDirectory(userDetails.getUser().getId(), path);
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceDTO);
    }


    @GetMapping("${directory}")
    public ResponseEntity<List<ResourceDTO>> getResources(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "path", required = false, defaultValue = "") String path
    ) {

        List<ResourceDTO> resourceDTOS = resourceService.listDirectories(userDetails.getUser().getId(), path);
        return ResponseEntity.ok(resourceDTOS);
    }
}
