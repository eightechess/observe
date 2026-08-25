# Spring Boot OpenTelemetry → New Relic Observability Demo

## Overview

This project is a standard Spring Boot demo used to validate deeper OpenTelemetry tracing in New Relic without relying on a Java agent.

The application uses a shared `RestTemplate` with an OpenTelemetry interceptor. Each outbound HTTP request creates a `CLIENT` child span, allowing New Relic to show which downstream dependency is contributing to request latency.

The demo calls two public services:

- **Open-Meteo** — current weather data
- **Frankfurter** — currency exchange rates

## Goal

Instead of seeing only a single application span such as:

```text
GET /api/demo                         2.5s
```

the goal is to produce trace depth similar to:

```text
GET /api/demo                         2.5s
│
└── observability-demo
    │
    ├── GET api.open-meteo.com       350ms
    │
    └── GET api.frankfurter.dev      180ms
```

This makes it easier to determine whether latency originates in the application or in a downstream dependency.

## Technology

- Java 21
- Spring Boot 4.1.1
- Gradle
- Spring Web MVC
- OpenTelemetry
- OTLP
- New Relic
- RestTemplate

## Important Components

### `OpenTelemetryConfig`

Uses the Spring Boot-managed `OpenTelemetry` instance and exposes a `Tracer` for application instrumentation.

Spring Boot should manage the OpenTelemetry SDK and OTLP exporter through `spring-boot-starter-opentelemetry` rather than manually constructing `OtlpGrpcSpanExporter`.

### `RestTemplateConfig`

Creates the application's shared `RestTemplate` and registers the OpenTelemetry interceptor.

### `OpenTelemetryRestTemplateInterceptor`

Automatically creates an OpenTelemetry `CLIENT` span for every outbound HTTP request made through the shared `RestTemplate`.

The interceptor can capture information such as:

- HTTP method
- Downstream hostname
- HTTP response status
- Exceptions/errors
- Request duration through the span duration

It also propagates the current OpenTelemetry context to downstream services.

### `ObservabilityDemoController`

Provides the main endpoint used to generate a trace containing multiple downstream calls.

## New Relic Configuration

Use a New Relic **Ingest - License** key.

Do not use a User API key, Browser key, or Mobile token for OTLP ingestion.

Keep the license key outside source control, for example:

```bash
export NEW_RELIC_LICENSE_KEY="YOUR_NEW_RELIC_LICENSE_KEY"
```

Configure Spring Boot/OpenTelemetry to export traces to the appropriate New Relic OTLP endpoint for your account/region.

A typical US OTLP/HTTP trace endpoint is:

```text
https://otlp.nr-data.net/v1/traces
```

Pass the New Relic license key using the OTLP `api-key` header.

## Running the Application

Build the application:

```bash
./gradlew clean build
```

Run it:

```bash
./gradlew bootRun
```

By default, Spring Boot runs on port `8080`.

## Browser Test Links

No Postman is required. Open the following URLs directly in a browser.

### 1. Full Observability Demo — Recommended

Runs both the weather and currency downstream calls using default values. This is the easiest endpoint to repeatedly refresh while viewing traces in New Relic.

```text
http://localhost:8080/api/demo
```

Default values:

```text
Latitude:  32.7357
Longitude: -97.1081
Amount:    100.00
From:      USD
To:        EUR
```

### 2. Weather + USD → GBP

Runs the weather request and converts USD 250 to GBP.

```text
http://localhost:8080/api/demo?latitude=32.7357&longitude=-97.1081&amount=250&from=USD&to=GBP
```

### 3. Weather + USD → EUR

Runs the weather request and converts USD 100 to EUR.

```text
http://localhost:8080/api/demo?latitude=32.7357&longitude=-97.1081&amount=100&from=USD&to=EUR
```

### 4. Amount Override Only

Uses the default weather location and currency pair but changes the amount to 500.

```text
http://localhost:8080/api/demo?amount=500
```

### 5. Currency Pair Override Only

Uses the default weather location and amount but changes the currency conversion to USD → GBP.

```text
http://localhost:8080/api/demo?from=USD&to=GBP
```

## Expected New Relic Trace

After calling `/api/demo`, New Relic should eventually show a trace resembling:

```text
GET /api/demo
│
└── observability-demo
    │
    ├── GET api.open-meteo.com
    │
    └── GET api.frankfurter.dev
```

The duration of each child span makes it possible to identify slow downstream dependencies.

For example:

```text
GET /api/demo                         1.45s
│
└── observability-demo               1.40s
    │
    ├── GET api.open-meteo.com       1.10s  <-- slow dependency
    │
    └── GET api.frankfurter.dev      220ms
```

## Why Instrument the Shared RestTemplate?

The application does not need tracing code in every service/client method.

Instead:

```text
Controller
    │
    ▼
Service
    │
    ▼
Shared RestTemplate
    │
    ▼
OpenTelemetryRestTemplateInterceptor
    │
    ├── Weather CLIENT span
    └── Currency CLIENT span
```

Any new downstream service that uses the shared `RestTemplate` automatically receives the same outbound tracing behavior.

## Recommended Span Naming

For the POC, use:

```text
HTTP_METHOD + HOST
```

Examples:

```text
GET api.open-meteo.com
GET api.frankfurter.dev
```

Avoid using raw URLs containing IDs, order numbers, SKUs, query parameters, or other high-cardinality values as span names.

## Troubleshooting

### HTTP 400 from `/api/demo`

Make request parameter names explicit in the controller:

```java
@RequestParam(name = "latitude", defaultValue = "32.7357") double latitude
```

Do the same for `longitude`, `amount`, `from`, and `to`.

### No trace depth in New Relic

Verify that the outbound `RestTemplate` spans use the current OpenTelemetry context as their parent.

The desired relationship is:

```text
Incoming SERVER span
        │
        └── CLIENT span: weather
        │
        └── CLIENT span: currency
```

If the `CLIENT` spans appear as separate traces, investigate whether the incoming request span is active in `Context.current()` when `RestTemplate` executes.

### New Relic receives no traces

Verify:

- The correct New Relic OTLP endpoint is configured.
- An **Ingest - License** key is being used.
- The key is sent as the OTLP `api-key` header.
- Network access to the New Relic OTLP endpoint is available.
- Trace sampling is enabled for the POC.

## POC Success Criteria

The demo is successful when:

1. `/api/demo` completes successfully.
2. Open-Meteo and Frankfurter are called through the shared `RestTemplate`.
3. Each outbound call creates a separate `CLIENT` span.
4. The spans are children of the application/request trace.
5. New Relic displays the latency of each downstream dependency.
6. Errors and HTTP status information can be associated with the appropriate dependency span.
7. No New Relic Java agent is required for the POC.
