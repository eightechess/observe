package com.mydemo.observe.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

public class OpenTelemetryRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private static final TextMapSetter<HttpRequest> HEADER_SETTER =
            (request, key, value) -> request.getHeaders().set(key, value);

    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    public OpenTelemetryRestTemplateInterceptor(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer("com.mydemo.observability.http-client");
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        URI uri = request.getURI();
        String method = request.getMethod().name();
        String host = uri.getHost() == null ? "unknown-service" : uri.getHost();

        Span span = tracer.spanBuilder(method + " " + host)
                .setSpanKind(SpanKind.CLIENT)
                .setParent(Context.current())
                .startSpan();

        span.setAttribute("http.request.method", method);
        span.setAttribute("server.address", host);
        span.setAttribute("dependency.name", host);

        if (uri.getPort() != -1) {
            span.setAttribute("server.port", uri.getPort());
        }

        Context contextWithSpan = Context.current().with(span);

        try (Scope ignored = contextWithSpan.makeCurrent()) {
            openTelemetry.getPropagators()
                    .getTextMapPropagator()
                    .inject(contextWithSpan, request, HEADER_SETTER);

            ClientHttpResponse response = execution.execute(request, body);
            int status = response.getStatusCode().value();

            span.setAttribute("http.response.status_code", status);
            if (status >= 500) {
                span.setStatus(StatusCode.ERROR);
            }

            return response;
        } catch (IOException | RuntimeException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR,
                    ex.getMessage() == null ? "HTTP client failure" : ex.getMessage());
            throw ex;
        } finally {
            span.end();
        }
    }
}
