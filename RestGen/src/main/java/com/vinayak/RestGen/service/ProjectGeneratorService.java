package com.vinayak.RestGen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinayak.RestGen.Dto.ApiRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ProjectGeneratorService {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public ProjectGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public ByteArrayOutputStream generateProject(ApiRequest request) throws Exception {
        // Step 1: Build prompt
        String prompt = buildPrompt(request);

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        // Step 2: Call Gemini
        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Step 3: Parse Gemini response → Map<path, content>
        Map<String, String> files = parseGeneratedCode(response);

        // Step 4: Create zip from files
        return createZip(files);
    }

    private Map<String, String> parseGeneratedCode(String response) {
        try {
            JsonNode root = mapper.readTree(response);
            String rawText = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Clean markdown fences
            String cleaned = rawText.replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            JsonNode filesNode = mapper.readTree(cleaned).path("files");

            Map<String, String> files = new HashMap<>();
            for (JsonNode file : filesNode) {
                files.put(file.get("path").asText(), file.get("content").asText());
            }
            return files;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }

    private ByteArrayOutputStream createZip(Map<String, String> files) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, String> entry : files.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue().getBytes());
                zos.closeEntry();
            }
        }
        return baos;
    }

    private String buildPrompt(ApiRequest request) {
        return "Generate a Spring Boot REST API project as JSON.\n"
                + "Return only valid JSON with structure:\n"
                + "{ \"files\": [ {\"path\": \"...\", \"content\": \"...\"} ] }\n"
                + "Project name: " + request.getProjectName() + "\n"
                + "Table name: " + request.getTableName() + "\n"
                + "Fields: " + request.getFields().toString() + "\n"
                + "\n"
                + "Requirements:\n"
                + "1. Analyze the fields and generate a proper JPA Entity class with all necessary imports and packages.\n"
                + "2. Create MVC layers (Controller, Service, Repository) for the entity with correct packages, imports, and annotations.\n"
                + "3. Connect the layers so that Controller calls Service and Service calls Repository.\n"
                + "4. Only create files inside three folders: 'controller', 'service', and 'repository'.\n"
                + "5. Include necessary Spring Boot dependencies in pom.xml for JPA, Lombok, and Web etc.\n"
                + "6. Also include any additional dependencies required by annotations or features used in Controller, Service, Repository, or Entity.\n"
                + "7. Only return JSON with file paths and file content, no markdown, no explanations, no extra text.\n"
                + "8. Use proper RESTful conventions for endpoints (CRUD operations).\n"
                + "9. Include package statements and all necessary imports in each file.\n"
                + "10. Add comments in each file explaining the purpose of the class and its methods.";
    }


}
