package com.chat.bot.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

/**
 * Unit tests for the CustomErrorController class.
 */
public class CustomErrorControllerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private Model model;

    @InjectMocks
    private CustomErrorController customErrorController;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests the handleError method to ensure it sets the correct attributes in the model and returns the 'error' view.
     */
    @Test
    void testHandleError() {
        when(request.getAttribute("jakarta.servlet.error.status_code")).thenReturn(404);
        when(request.getAttribute("jakarta.servlet.error.message")).thenReturn("Not Found");

        String viewName = customErrorController.handleError(request, model);

        verify(model).addAttribute("statusCode", 404);
        verify(model).addAttribute("errorMessage", "Not Found");
        assertEquals("error", viewName, "The view name should be 'error'.");
    }

    /**
     * Tests the handleError method when there is no error message to ensure it sets a default error message in the model and returns the 'error' view.
     */
    @Test
    void testHandleErrorWithNoMessage() {
        when(request.getAttribute("jakarta.servlet.error.status_code")).thenReturn(500);
        when(request.getAttribute("jakarta.servlet.error.message")).thenReturn(null);

        String viewName = customErrorController.handleError(request, model);

        verify(model).addAttribute("statusCode", 500);
        verify(model).addAttribute("errorMessage", "An unexpected error occurred.");
        assertEquals("error", viewName, "The view name should be 'error'.");
    }
}
