package github.atharv3221.airhealth.controllers;

import github.atharv3221.airhealth.config.SecurityConfig;
import github.atharv3221.airhealth.dto.LoginRequest;
import github.atharv3221.airhealth.dto.LoginResponse;
import github.atharv3221.airhealth.security.JwtAuthFilter;
import github.atharv3221.airhealth.service.AuthService;
import github.atharv3221.airhealth.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthService authService;

  @Test
  void loginReturnsTokenForValidCredentials() throws Exception {
    when(authService.login(new LoginRequest("a@x.com", "secret")))
        .thenReturn(new LoginResponse("jwt-token"));

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"a@x.com\",\"password\":\"secret\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token"));
  }

  @Test
  void loginReturns401ForInvalidCredentials() throws Exception {
    when(authService.login(any())).thenThrow(
        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"a@x.com\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginReturns400ForMissingBody() throws Exception {
    mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void loginRejectsGet() throws Exception {
    mockMvc.perform(get("/api/auth/login"))
        .andExpect(status().isMethodNotAllowed());
  }
}
