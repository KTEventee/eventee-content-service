package eventee.server.content.domain.config;

import eventee.server.common.jwt.JwtValidator;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  @Bean
  public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilter(JwtValidator jwtValidator) {
    FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new JwtAuthFilter(jwtValidator));
    registration.addUrlPatterns("/members/*");
    registration.setOrder(1);
    return registration;
  }
}
