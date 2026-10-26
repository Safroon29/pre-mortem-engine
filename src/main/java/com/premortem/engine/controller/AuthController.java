package com.premortem.engine.controller;

import com.premortem.engine.service.RegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final RegistrationService registration;
    public AuthController(RegistrationService registration) { this.registration = registration; }

    @GetMapping("/")
    public String root() { return "redirect:/dashboard"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String displayName, @RequestParam String email,
                           @RequestParam String password, Model model) {
        try {
            registration.register(displayName, email, password);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("displayName", displayName);
            model.addAttribute("email", email);
            return "register";
        }
    }
}
