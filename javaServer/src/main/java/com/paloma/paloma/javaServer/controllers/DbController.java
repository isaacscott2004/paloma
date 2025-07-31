package com.paloma.paloma.javaServer.controllers;

import com.paloma.paloma.javaServer.services.DbClearService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for database operations.
 * This controller is intended for testing purposes only.
 */
@RestController
@RequestMapping("/db")
@RequiredArgsConstructor
public class DbController {

    private final DbClearService dbClearService;

    /**
     * Clears all data from the database.
     * This endpoint is intended for testing purposes only and should not be used in production.
     * No authentication is required for this endpoint.
     *
     * @return ResponseEntity with a success message
     */
    @DeleteMapping
    public ResponseEntity<String> clearAllData() {
        try {
            dbClearService.clearAllData();
            return ResponseEntity.ok("Database cleared successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to clear database: " + e.getMessage());
        }
    }
}