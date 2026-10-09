package github.atharv3221.airhealth.controllers;

import github.atharv3221.airhealth.config.SecurityConfig;
import github.atharv3221.airhealth.dto.CreateUserRequest;
import github.atharv3221.airhealth.dto.UserResponse;
import github.atharv3221.airhealth.entity.User;
import github.atharv3221.airhealth.security.JwtAuthFilter;
import github.atharv3221.airhealth.service.JwtService;
import github.atharv3221.airhealth.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtService jwtService;

  @MockitoBean
  private UserService userService;

  private String tokenFor(long id, String email) {
    User user = new User();
    user.setEmail(email);
    ReflectionTestUtils.setField(user, "id", id);
    return jwtService.generateToken(user);
  }

  @Test
  void createReturns201WithoutAuth() throws Exception {
    when(userService.create(new CreateUserRequest("Asha", "a@x.com", "secret")))
        .thenReturn(new UserResponse(1L, "Asha", "a@x.com"));

    mockMvc.perform(post("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Asha\",\"email\":\"a@x.com\",\"password\":\"secret\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("a@x.com"))
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
  }

  @Test
  void createReturns409ForDuplicateEmail() throws Exception {
    when(userService.create(any())).thenThrow(
        new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered"));

    mockMvc.perform(post("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Asha\",\"email\":\"a@x.com\",\"password\":\"secret\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void meReturnsCurrentUserForValidToken() throws Exception {
    when(userService.get(7L)).thenReturn(new UserResponse(7L, "Asha", "a@x.com"));

    mockMvc.perform(get("/user/me")
            .header("Authorization", "Bearer " + tokenFor(7L, "a@x.com")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.name").value("Asha"));
  }

  @Test
  void meReturns401WithoutToken() throws Exception {
    mockMvc.perform(get("/user/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void meReturns401ForInvalidToken() throws Exception {
    mockMvc.perform(get("/user/me").header("Authorization", "Bearer not-a-jwt"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void meReturns401ForTokenSignedWithOtherKey() throws Exception {
    JwtService other = new JwtService("a-completely-different-secret-of-32-bytes", 60000);
    User user = new User();
    ReflectionTestUtils.setField(user, "id", 7L);

    mockMvc.perform(get("/user/me")
            .header("Authorization", "Bearer " + other.generateToken(user)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void meReturns404WhenUserDeleted() throws Exception {
    when(userService.get(7L)).thenThrow(
        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    mockMvc.perform(get("/user/me")
            .header("Authorization", "Bearer " + tokenFor(7L, "a@x.com")))
        .andExpect(status().isNotFound());
  }
}
