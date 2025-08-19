package coffeeshout.global.filter;

import coffeeshout.global.metric.HttpMetricService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class HttpMetricFilter implements Filter {

    private final HttpMetricService httpMetricService;

    public HttpMetricFilter(HttpMetricService httpMetricService) {
        this.httpMetricService = httpMetricService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        httpMetricService.incrementConcurrentRequests();
        try {
            chain.doFilter(request, response);
        } finally {
            httpMetricService.decrementConcurrentRequests();
        }
    }
}