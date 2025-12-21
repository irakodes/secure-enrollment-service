package online.eracodes.secureenrollmentservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.eracodes.protobuf.enrollment.EnrollmentProto;
import online.eracodes.secureenrollmentservice.service.IUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RegistrationWebController {

    private final IUserService userService;

    @GetMapping("/register")
    public String registrationForm() {
        return "register";
    }

    @PostMapping("/create-user")
    public String createUser(
            @RequestParam String name,
            @RequestParam String email,
            Model model) {
        try {
            var request = EnrollmentProto.CreateUserRequest.newBuilder()
                    .setName(name)
                    .setEmail(email)
                    .build();

            var response = userService.createUser(request);
            
            model.addAttribute("createSuccess", true);
            model.addAttribute("registrationCode", response.getRegistrationCode());
            model.addAttribute("userId", response.getUserId());
            
        } catch (Exception e) {
            log.error("User creation failed", e);
            model.addAttribute("createError", e.getMessage());
        }
        
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String email,
            @RequestParam String registrationCode,
            Model model) {
        try {
            var request = EnrollmentProto.RegisterUserRequest.newBuilder()
                    .setEmail(email)
                    .setRegistrationCode(registrationCode)
                    .build();

            var response = userService.registerUser(request);
            
            model.addAttribute("registerSuccess", true);
            model.addAttribute("authToken", response.getAuthToken());
            model.addAttribute("username", response.getUsername());
            
        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("registerError", e.getMessage());
        }
        
        return "register";
    }
}

