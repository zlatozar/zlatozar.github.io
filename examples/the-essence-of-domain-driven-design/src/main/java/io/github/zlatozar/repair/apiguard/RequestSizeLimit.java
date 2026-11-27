package io.github.zlatozar.repair.apiguard;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Refuses a large body before anyone reads it. A chunked body declares no length; the Jackson limit
 * in {@code application.properties} stops it.
 */
@Component
class RequestSizeLimit implements WebMvcConfigurer, HandlerInterceptor {

  private final long maxBytes;

  RequestSizeLimit(@Value("${repair.max-request-bytes}") long maxBytes) {
    this.maxBytes = maxBytes;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(this);
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (request.getContentLengthLong() > maxBytes) {
      throw new ResponseStatusException(
          HttpStatus.CONTENT_TOO_LARGE, "A request body may have at most " + maxBytes + " bytes");
    }
    return true;
  }
}
