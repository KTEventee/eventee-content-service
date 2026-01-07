package eventee.server.content.domain.config;

import eventee.server.common.jwt.JwtPrincipal;
import eventee.server.common.jwt.JwtValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtValidator jwtValidator;

  public JwtAuthFilter(JwtValidator jwtValidator) {
    this.jwtValidator = jwtValidator;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String authorization = request.getHeader("Authorization");

    if (authorization != null && authorization.startsWith("Bearer ")) {
      String token = authorization.substring(7);
      JwtPrincipal principal = jwtValidator.validate(token);


      UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                      principal.memberId(),      // principal = 식별자
                      null,
                      List.of()
              );

      SecurityContextHolder.getContext()
              .setAuthentication(authentication);

      request.setAttribute("memberId", principal.memberId());
      request.setAttribute("tokenType", principal.tokenType());
    }

    filterChain.doFilter(request, response);
  }
}
