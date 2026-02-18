// src/main/java/com/vinayak/RestGen/Controller/ProjectController.java
package com.vinayak.RestGen.Controller;

import com.vinayak.RestGen.Dto.ApiRequest;
import com.vinayak.RestGen.service.ProjectGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")  // allow all origins for simplicity
public class ProjectController {

    private final ProjectGeneratorService projectGeneratorService;

    public ProjectController(ProjectGeneratorService projectGeneratorService) {
        this.projectGeneratorService = projectGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateProject(@RequestBody ApiRequest apiRequest) {
        try {
            ByteArrayOutputStream zipFile = projectGeneratorService.generateProject(apiRequest);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + apiRequest.getProjectName() + ".zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipFile.toByteArray());

        } catch (Exception e) {
            // Return JSON error instead of raw bytes
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
