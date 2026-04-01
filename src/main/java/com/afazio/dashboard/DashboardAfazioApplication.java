package com.afazio.dashboard;

import com.afazio.dashboard.calendar.application.GoogleCalendarProperties;
import com.afazio.dashboard.reporting.application.TeacherProperties;
import com.afazio.dashboard.shared.config.AppTimeProperties;
import com.afazio.dashboard.shared.config.FrontProperties;
import com.afazio.dashboard.shared.config.CorsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
  TeacherProperties.class,
  CorsProperties.class,
  GoogleCalendarProperties.class,
  FrontProperties.class,
  AppTimeProperties.class
})
public class DashboardAfazioApplication {

  public static void main(String[] args) {
    SpringApplication.run(DashboardAfazioApplication.class, args);
  }

}
