package github.atharv3221.airhealth.controllers;

import github.atharv3221.airhealth.dto.CreateUserRequest;
import github.atharv3221.airhealth.dto.UserResponse;
import github.atharv3221.airhealth.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse create(@RequestBody CreateUserRequest request) {
    return userService.create(request);
  }

  // principal is the user id set by JwtAuthFilter
  @GetMapping("/me")
  public UserResponse me(@AuthenticationPrincipal Long userId) {
    return userService.get(userId);
  }
}
