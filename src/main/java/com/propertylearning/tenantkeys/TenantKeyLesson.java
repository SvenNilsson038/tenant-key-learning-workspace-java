package com.propertylearning.tenantkeys;

import java.util.List;

/** Runnable entry point for a tenant credential lesson. */
public final class TenantKeyLesson {
    private TenantKeyLesson() {
    }

    public static void main(String[] args) throws Exception {
        TenantKeyConfig config = TenantKeyConfig.fromEnvironment();
        InfraiControlPlane controlPlane = new InfraiControlPlane(config);
        TenantProvisioningService provisioning = new TenantProvisioningService(controlPlane);

        String email = requiredEnvironment("TENANT_EMAIL");
        TenantLearningWorkspace workspace = new TenantLearningWorkspace(
                "harbor-house",
                new MaintenanceRequest("leaking tap", "open"),
                new TenantDocument("lease-renewal", "review"),
                new InspectionReminder("boiler inspection", "2026-10-01"));

        IssuedTenantCredential issued = provisioning.issue(workspace, email);
        System.out.println("Issued key " + issued.keyId() + " for " + issued.tenantId()
                + ". Store the plaintext credential in your secret manager now: " + issued.plaintextKey());
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be set");
        }
        return value;
    }
}
