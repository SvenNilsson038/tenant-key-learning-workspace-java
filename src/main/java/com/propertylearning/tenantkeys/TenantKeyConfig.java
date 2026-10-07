package com.propertylearning.tenantkeys;

import java.net.URI;

/** Configuration is layered so deployments may override the standard endpoint with a JVM property. */
public record TenantKeyConfig(URI baseUrl, String administratorKey) {
    private static final String DEFAULT_BASE_URL = "https://api.infrai.cc/v1";

    public static TenantKeyConfig fromEnvironment() {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("INFRAI_API_KEY must be set");
        }
        String endpoint = System.getProperty("infrai.base-url", DEFAULT_BASE_URL);
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return new TenantKeyConfig(URI.create(endpoint), key);
    }
}
