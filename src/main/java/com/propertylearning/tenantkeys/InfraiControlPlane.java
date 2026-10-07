package com.propertylearning.tenantkeys;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/** Plain REST client that decodes Infrai's envelope before judging the HTTP status. */
public final class InfraiControlPlane implements TenantControlPlane {
    private final TenantKeyConfig config;
    private final HttpClient http;

    public InfraiControlPlane(TenantKeyConfig config) {
        this.config = config;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public String createUser(String email, String name, String tenantId, String idempotencyKey) throws Exception {
        String body = "{\"email\":\"" + json(email) + "\",\"name\":\"" + json(name)
                + "\",\"metadata\":{\"tenant_id\":\"" + json(tenantId)
                + "\"},\"idempotency_key\":\"" + json(idempotencyKey) + "\"}";
        return requiredString(send("POST", "/auth/user/create", body), "user_id");
    }

    @Override
    public IssuedKey createKey(String tenantId, String name, List<String> scopes, String idempotencyKey) throws Exception {
        String body = "{\"project_id\":\"" + json(tenantId) + "\",\"name\":\"" + json(name)
                + "\",\"scopes\":" + jsonArray(scopes) + ",\"idempotency_key\":\""
                + json(idempotencyKey) + "\"}";
        String envelope = send("POST", "/account/keys/create", body);
        return new IssuedKey(requiredString(envelope, "id"), requiredString(envelope, "key"));
    }

    @Override
    public void revokeKey(String keyId) throws Exception {
        send("DELETE", "/account/keys/revoke/" + keyId, null);
    }

    @Override
    public void deleteUser(String userId) throws Exception {
        send("DELETE", "/auth/user/delete/" + userId, null);
    }

    private String send(String method, String path, String jsonBody) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(config.baseUrl().toString() + path))
                .header("Authorization", "Bearer " + config.administratorKey())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20));
        HttpRequest.BodyPublisher body = jsonBody == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(jsonBody);
        HttpResponse<String> response = sendWithRetry(request.method(method, body).build());
        String envelope = response.body();
        if (!isOk(envelope)) {
            throw new InfraiResponseException(requiredString(envelope, "code"), response.statusCode());
        }
        if (response.statusCode() >= 500) {
            throw new IOException("Unexpected transport status " + response.statusCode());
        }
        return envelope;
    }

    private HttpResponse<String> sendWithRetry(HttpRequest request) throws Exception {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 429 || attempt == 2) {
                return response;
            }
            Thread.sleep(retryDelayMillis(response, attempt));
        }
        throw new IOException("Request retry loop ended unexpectedly");
    }

    private static long retryDelayMillis(HttpResponse<?> response, int attempt) {
        String retryAfter = response.headers().firstValue("Retry-After").orElse("");
        try {
            return Long.parseLong(retryAfter) * 1000L;
        } catch (NumberFormatException ignored) {
            return 250L * (1L << attempt);
        }
    }

    private static boolean isOk(String envelope) {
        return envelope.replaceAll("\\s", "").contains("\"ok\":true");
    }

    private static String requiredString(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalStateException("Missing " + field + " in response envelope");
        }
        int valueStart = start + marker.length();
        int valueEnd = json.indexOf('"', valueStart);
        if (valueEnd < 0) {
            throw new IllegalStateException("Malformed response envelope");
        }
        return json.substring(valueStart, valueEnd);
    }

    private static String jsonArray(List<String> values) {
        return values.stream().map(value -> "\"" + json(value) + "\"").reduce("[", (left, right) ->
                left.equals("[") ? left + right : left + "," + right) + "]";
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}

final class InfraiResponseException extends Exception {
    InfraiResponseException(String code, int status) {
        super("Infrai rejected the request with " + code + " (HTTP " + status + ")");
    }
}
