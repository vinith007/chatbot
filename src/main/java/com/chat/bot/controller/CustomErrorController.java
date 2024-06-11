package com.chat.bot.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller class for handling errors.
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * Handles errors and displays the error page.
     *
     * @param request the HTTP request that resulted in an error
     * @param model   the model to which attributes are added
     * @return the name of the view to be rendered
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("jakarta.servlet.error.message");

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", errorMessage != null ? errorMessage : "An unexpected error occurred.");

        return "error";
    }
}
