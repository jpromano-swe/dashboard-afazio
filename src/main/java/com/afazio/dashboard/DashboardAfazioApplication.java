package com.afazio.dashboard;

import com.afazio.dashboard.reporting.application.TeacherProperties;
import com.afazio.dashboard.shared.config.CorsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({TeacherProperties.class, CorsProperties.class})
public class DashboardAfazioApplication {

  public static void main(String[] args) {
    SpringApplication.run(DashboardAfazioApplication.class, args);
  }

}
