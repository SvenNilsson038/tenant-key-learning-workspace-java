package com.propertylearning.tenantkeys;

/** Keeps credential and user removal in one ordered business action. */
public final class TenantOffboardingService {
    private final TenantControlPlane controlPlane;

    public TenantOffboardingService(TenantControlPlane controlPlane) {
        this.controlPlane = controlPlane;
    }

    public void offboard(IssuedTenantCredential credential) throws Exception {
        controlPlane.revokeKey(credential.keyId());
        controlPlane.deleteUser(credential.userId());
    }
}
