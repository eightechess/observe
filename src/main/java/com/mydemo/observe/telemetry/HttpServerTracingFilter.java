package com.mydemo.observe.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class HttpServerTracingFilter extends OncePerRequestFilter {

    private static final TextMapGetter<HttpServletRequest> HEADER_GETTER =
            new TextMapGetter<>() {
                @Override
                public Iterable<String> keys(HttpServletRequest request) {
                    return request == null
                            ? Collections.emptyList()
                            : Collections.list(request.getHeaderNames());
                }

                @Override
                public String get(HttpServletRequest request, String key) {
                    return request == null ? null : request.getHeader(key);
                }
            };

    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    public HttpServerTracingFilter(OpenTelemetry openTelemetry, Tracer tracer) {
        this.openTelemetry = openTelemetry;
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Context extractedParent = openTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), request, HEADER_GETTER);

        String spanName = request.getMethod() + " " + request.getRequestURI();

        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(SpanKind.SERVER)
                .setParent(extractedParent)
                .startSpan();

        span.setAttribute("http.request.method", request.getMethod());
        span.setAttribute("url.path", request.getRequestURI());

        try (Scope ignored = span.makeCurrent()) {
            response.setHeader("X-Trace-Id", span.getSpanContext().getTraceId());
            filterChain.doFilter(request, response);

            int status = response.getStatus();
            span.setAttribute("http.response.status_code", status);
            if (status >= 500) {
                span.setStatus(StatusCode.ERROR);
            }
        } catch (IOException | ServletException | RuntimeException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR,
                    ex.getMessage() == null ? "Request failed" : ex.getMessage());
            throw ex;
        } finally {
            span.end();
        }
    }
}
