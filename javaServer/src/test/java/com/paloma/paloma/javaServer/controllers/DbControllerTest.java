package com.paloma.paloma.javaServer.controllers;

import com.paloma.paloma.javaServer.services.DbClearService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for the DbController class.
 */
class DbControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DbClearService dbClearService;

    @InjectMocks
    private DbController dbController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(dbController).build();
    }

    @Test
    void testClearAllDataSuccess() throws Exception {
        // Test successful database clearing
        doNothing().when(dbClearService).clearAllData();

        mockMvc.perform(delete("/db")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Database cleared successfully"));

        verify(dbClearService, times(1)).clearAllData();
    }

    @Test
    void testClearAllDataFailure() throws Exception {
        // Test database clearing failure
        doThrow(new RuntimeException("Test exception")).when(dbClearService).clearAllData();

        mockMvc.perform(delete("/db")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to clear database: Test exception"));

        verify(dbClearService, times(1)).clearAllData();
    }
}