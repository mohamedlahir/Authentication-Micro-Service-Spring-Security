package com.authentication.service.auth.controllers;

import com.authentication.service.auth.models.AuthenticationModel;
import com.authentication.service.auth.models.Users;
import com.authentication.service.auth.repositories.AuthenticationModelRepository;
import com.authentication.service.auth.service.batchprocessing.UserProfileBatchProcessing;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class BatchUploadController {

    @Autowired
    private AuthenticationModelRepository authRepo;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    UserProfileBatchProcessing userFileBatchProcessing;

    //uploadExcel
    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            userFileBatchProcessing.uploadExcel(file);
            return ResponseEntity.ok("File processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing file: " + e.getMessage());
        }
    }
}
