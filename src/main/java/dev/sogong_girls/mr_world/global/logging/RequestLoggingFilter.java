package dev.sogong_girls.mr_world.global.logging;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 모든 요청이 끝날 때 한 줄 로그를 남긴다.
 *
 * <pre>
 * GET /api/tours -> 200 OK (12ms)
 * GET /api/tours/99 -> 404 NOT_FOUND (3ms) | cause: BusinessException: 대상을 찾을 수 없습니다.
 * </pre>
 *
 * 2xx·3xx는 INFO, 4xx는 WARN, 5xx는 ERROR(스택 트레이스 포함)로 남긴다.
 * Spring Security보다 먼저 실행되도록 가장 높은 우선순위로 등록해 401·403 응답도 기록한다.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final String ERROR_ATTRIBUTE = RequestLoggingFilter.class.getName() + ".ERROR";

    /**
     * 예외 처리기에서 호출해 응답의 원인이 된 예외를 요청 로그에 함께 남긴다.
     */
    public static void recordError(HttpServletRequest request, Throwable error) {
        request.setAttribute(ERROR_ATTRIBUTE, error);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        boolean unhandled = false;
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException e) {
            // 예외 처리기를 거치지 않고 올라온 예외: 이후 서블릿 컨테이너가 500으로 응답한다
            unhandled = true;
            recordError(request, e);
            throw e;
        } finally {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            int status = unhandled && response.getStatus() < 400
                    ? HttpStatus.INTERNAL_SERVER_ERROR.value()
                    : response.getStatus();
            logRequest(request, status, elapsedMs);
        }
    }

    private void logRequest(HttpServletRequest request, int status, long elapsedMs) {
        HttpStatus httpStatus = HttpStatus.resolve(status);
        StringBuilder line = new StringBuilder()
                .append(request.getMethod()).append(' ').append(requestPath(request))
                .append(" -> ").append(status);
        if (httpStatus != null)
            line.append(' ').append(httpStatus.name());
        line.append(" (").append(elapsedMs).append("ms)");

        Throwable error = (Throwable) request.getAttribute(ERROR_ATTRIBUTE);
        if (error != null)
            line.append(" | cause: ").append(describe(error));

        if (status >= 500)
            log.error(line.toString(), error);
        else if (status >= 400)
            log.warn(line.toString());
        else
            log.info(line.toString());
    }

    private static String requestPath(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
    }

    private static String describe(Throwable error) {
        if (error instanceof MethodArgumentNotValidException e) {
            // 어떤 필드가 왜 잘못됐는지 남긴다
            String fields = e.getBindingResult().getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + "=" + fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return e.getClass().getSimpleName() + ": " + fields;
        }

        String description = error.getClass().getSimpleName() + ": " + error.getMessage();
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(error);
        if (rootCause != error)
            description += " (root: " + rootCause.getClass().getSimpleName() + ": " + rootCause.getMessage() + ")";
        return description;
    }
}
