package eventee.server.content.domain.config;

import eventee.server.common.jwt.JwtTokenProvider;
import eventee.server.common.jwt.JwtValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  @Bean
  public JwtValidator jwtValidator(
      @Value("${jwt.secret}") String secret
  ) {
    return new JwtTokenProvider(secret);
  }
}
