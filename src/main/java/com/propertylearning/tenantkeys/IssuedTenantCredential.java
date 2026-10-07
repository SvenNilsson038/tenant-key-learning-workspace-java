package com.propertylearning.tenantkeys;

/** The plaintext value is available only at issuance time. */
public record IssuedTenantCredential(String tenantId, String userId, String keyId, String plaintextKey) {
}
