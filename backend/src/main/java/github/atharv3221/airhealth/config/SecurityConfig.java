package github.atharv3221.airhealth.config;

import github.atharv3221.airhealth.security.JwtAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // requests that skip auth; everything else needs a valid JWT
  private static final String[] PUBLIC_PATHS = {
      "/api/auth/**",  // login
      "/error"         // so errors on public endpoints keep their real status code
  };
  private static final String[] PUBLIC_POST_PATHS = {
      "/user"          // signup
  };
  // React app shell and its client-side routes (frontend/src/App.jsx); data still needs a JWT
  private static final String[] PUBLIC_GET_PATHS = {
      "/", "/index.html", "/assets/**", "/favicon.png",
      "/about", "/login", "/signup", "/app", "/app/subscriptions"
  };

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter)
      throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(PUBLIC_PATHS).permitAll()
            .requestMatchers(HttpMethod.POST, PUBLIC_POST_PATHS).permitAll()
            .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()
            .anyRequest().authenticated())
        .exceptionHandling(e -> e.authenticationEntryPoint(
            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  // JwtAuthFilter is a @Component, so Boot would also register it as a plain servlet filter;
  // it should only run inside the security chain
  @Bean
  public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
    FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

}
