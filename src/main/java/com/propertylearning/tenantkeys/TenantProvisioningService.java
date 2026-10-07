package com.propertylearning.tenantkeys;

import java.util.List;
import java.util.UUID;

/** Issues a scoped key only after the tenant's owning user exists. */
public final class TenantProvisioningService {
    private final TenantControlPlane controlPlane;

    public TenantProvisioningService(TenantControlPlane controlPlane) {
        this.controlPlane = controlPlane;
    }

    public IssuedTenantCredential issue(TenantLearningWorkspace workspace, String email) throws Exception {
        String requestId = UUID.randomUUID().toString();
        String userId = controlPlane.createUser(email, workspace.tenantId() + " manager", workspace.tenantId(), requestId);
        IssuedKey key = controlPlane.createKey(
                workspace.tenantId(),
                workspace.tenantId() + " property workspace",
                List.of("maintenance:read", "documents:read", "inspections:read"),
                requestId);
        return new IssuedTenantCredential(workspace.tenantId(), userId, key.id(), key.plaintextValue());
    }
}
