package com.authentication.service.auth.service.batchprocessing;

import com.authentication.service.auth.models.AuthenticationModel;
import com.authentication.service.auth.models.Users;
import com.authentication.service.auth.repositories.AuthenticationModelRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class UserProfileBatchProcessing {

    @Autowired
    private AuthenticationModelRepository authRepo;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        int successCount = 0;

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            boolean isHeader = true;
            for (Row row : sheet) {
                if (isHeader) { // skip header row
                    isHeader = false;
                    continue;
                }

                // 🔹 Read Excel columns by index
                String email = getStringValue(row.getCell(0));
                String password = getStringValue(row.getCell(1));
                String role = getStringValue(row.getCell(2));
                String firstName = getStringValue(row.getCell(3));
                String lastName = getStringValue(row.getCell(4));
                String ageStr = getStringValue(row.getCell(5));
                int age = 0;
                if (!ageStr.isEmpty()) {
                    try {
                        age = (int) Double.parseDouble(ageStr);
                    } catch (NumberFormatException e) {
                        System.err.println("⚠️ Invalid age format for value: " + ageStr + ", defaulting to 0");
                    }
                }

                // 🔹 Generate unique Profile ID
                String profileIDStr = UUID.randomUUID().toString();

                // 🔹 Save to AuthenticationModel (for auth DB)
                AuthenticationModel authModel = new AuthenticationModel();
                authModel.setEmail(email);
                authModel.setPassword(encoder.encode(password));
                authModel.setRole(role);
                authModel.setProfileID(profileIDStr);
                authRepo.save(authModel);

                // 🔹 Prepare Users object (to send via Kafka)
                Users usersData = new Users();
                usersData.setEmail(email);
                usersData.setPassword(encoder.encode(password)); // Plain password sent — or mask if needed
                usersData.setRole(role);
                usersData.setFirstName(firstName);
                usersData.setLastName(lastName);
                usersData.setAge(age);
                usersData.setProfileID(profileIDStr);

                // 🔹 Serialize and send to Kafka
                try {
                    ObjectMapper om = new ObjectMapper();
                    String payload = om.writeValueAsString(usersData);
                    kafkaTemplate.send("user-profile-creation", profileIDStr, payload);
                    successCount++;
                    System.out.println("✅ User published to Kafka: " + usersData.getEmail());
                } catch (Exception e) {
                    System.err.println("❌ Failed to send Kafka message for: " + email + " - " + e.getMessage());
                }
            }

            workbook.close();
            return ResponseEntity.ok("✅ Successfully processed " + successCount + " users!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("❌ Failed to process file: " + e.getMessage());
        }

    }
    private String getStringValue(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}