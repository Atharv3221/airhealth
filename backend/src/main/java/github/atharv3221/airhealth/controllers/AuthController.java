package github.atharv3221.airhealth.controllers;


import github.atharv3221.airhealth.dto.LoginRequest;
import github.atharv3221.airhealth.dto.LoginResponse;
import github.atharv3221.airhealth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest request) {
    return authService.login(request);
  }
}
