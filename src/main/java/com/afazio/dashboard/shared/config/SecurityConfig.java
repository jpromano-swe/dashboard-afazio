package com.afazio.dashboard.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  @Order(1)
  SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
      .securityMatcher("/api/**", "/actuator/**")
      .cors(Customizer.withDefaults())
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health").permitAll()
        .anyRequest().authenticated()
      )
      .httpBasic(Customizer.withDefaults())
      .build();
  }

  @Bean
  @Order(2)
  SecurityFilterChain oauthSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
      .securityMatcher("/oauth2/**", "/login/**", "/google/**")
      .cors(Customizer.withDefaults())
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth -> auth
        .anyRequest().authenticated()
      )
      .oauth2Login(oauth -> oauth
        .defaultSuccessUrl("/actuator/health", true)
      )
      .build();
  }



  @Bean
  InMemoryUserDetailsManager userDetailsService() {
    UserDetails user = User.withUsername("admin")
      .password("{noop}admin123")
      .roles("ADMIN")
      .build();

    return new InMemoryUserDetailsManager(user);
  }
}
