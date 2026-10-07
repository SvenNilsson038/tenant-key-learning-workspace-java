package com.propertylearning.tenantkeys;

import java.util.List;

/** Narrow boundary needed by the tenant workflow. */
public interface TenantControlPlane {
    String createUser(String email, String name, String tenantId, String idempotencyKey) throws Exception;

    IssuedKey createKey(String tenantId, String name, List<String> scopes, String idempotencyKey) throws Exception;

    void revokeKey(String keyId) throws Exception;

    void deleteUser(String userId) throws Exception;
}

record IssuedKey(String id, String plaintextValue) {
}
