package com.example.portfolio.controllers.PageControllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String index() {
        return "index"; // Landing page
    }

    @GetMapping("/home")
    public String home() {
        return "home"; // Old test console page
    }

    @GetMapping("/auth")
    public String auth() {
        return "auth"; // Login/Register page
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // Dashboard page
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy"; // Privacy Policy page
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms"; // Terms of Service page
    }
}