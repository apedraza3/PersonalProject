package com.example.portfolio.controllers.ApiController;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CsrfController {

    /**
     * Endpoint to get CSRF token
     * Spring Security will automatically add the token to the response cookie
     * Frontend just needs to call this endpoint to initialize the token
     */
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}
