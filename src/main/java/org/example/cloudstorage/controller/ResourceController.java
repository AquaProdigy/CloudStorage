package org.example.cloudstorage.controller;


import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.model.dto.ResourceDTO;
import org.example.cloudstorage.repository.ResourceRepository;
import org.example.cloudstorage.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;


    @GetMapping
    public ResponseEntity<ResourceDTO> getResource(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(name = "path") String path
    ) {
        ResourceDTO resourceDTO = resourceService.getInfoResource(user.getUsername(), path);
        return ResponseEntity.ok(resourceDTO);
    }

    @DeleteMapping
    public void deleteResource(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(name = "path") String path
    ) {
        resourceService.deleteResource(user.getUsername(), path);
    }
}
